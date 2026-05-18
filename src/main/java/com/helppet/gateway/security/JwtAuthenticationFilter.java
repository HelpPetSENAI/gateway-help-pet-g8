package com.helppet.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Filtro JWT reativo (WebFlux).
 * 
 * Responsabilidades:
 * 1. Extrair e validar o token Bearer do header Authorization
 * 2. Popular o SecurityContext com a autenticacao do usuario
 * 3. Injetar headers X-User-Id, X-User-Email e X-Request-Id para os microservicos downstream
 * 4. Rejeitar requisicoes com token invalido com 401
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${api.security.public-paths[0]:/auth/**}")
    private String publicPath0;
    @Value("${api.security.public-paths[1]:/api/v1/auth/**}")
    private String publicPath1;
    @Value("${api.security.public-paths[2]:/actuator/health}")
    private String publicPath2;
    @Value("${api.security.public-paths[3]:/api/health}")
    private String publicPath3;
    @Value("${api.security.public-paths[4]:/api/metrics/**}")
    private String publicPath4;

    @Value("${internal.service.token:CHANGE_ME_INTERNAL_TOKEN}")
    private String internalServiceToken;

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Rotas publicas: nao exigem autenticacao
        if (isPublicPath(exchange)) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Requisicao sem token JWT para path: {}", path);
            return unauthorized(exchange, "Token de autenticacao nao fornecido");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtProvider.isTokenValid(token)) {
            log.warn("Token JWT invalido ou expirado para path: {}", path);
            return unauthorized(exchange, "Token invalido ou expirado");
        }

        Claims claims = jwtProvider.extractAllClaims(token);
        String email = claims.getSubject();
        String userId = claims.get("userId", String.class);

        // Reutiliza requestId de entrada quando presente para correlacao ponta a ponta
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;

        log.debug("JWT valido | usuario: {} | userId: {} | requestId: {}", email, userId, requestId);

        // Monta a exchange com os headers injetados para os microservicos downstream
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(req -> req.headers(headers -> {
                    headers.set("X-User-Id", userId != null ? userId : "");
                    headers.set("X-User-Email", email != null ? email : "");
                    headers.set("X-Request-Id", finalRequestId);
                    headers.set("X-Internal-Token", internalServiceToken);
                }))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private boolean isPublicPath(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "";

        // Caminhos públicos: auth, metrics, health
        List<String> publicPaths = Arrays.asList(
                publicPath0, publicPath1, publicPath2, publicPath3, publicPath4
        );

        if (matchesAnyPattern(path, publicPaths)) {
            log.debug("Path público detectado: {}", path);
            return true;
        }

        // Permitir CORS OPTIONS preflight
        if ("OPTIONS".equals(method)) {
            log.debug("Requisição OPTIONS permitida para: {}", path);
            return true;
        }

        return false;
    }

    private boolean matchesAnyPattern(String path, List<String> patterns) {
        return patterns.stream()
                .filter(p -> p != null && !p.isEmpty())
                .anyMatch(pattern -> {
                    boolean matches = antPathMatcher.match(pattern, path);
                    if (matches) {
                        log.debug("Path {} matches pattern {}", path, pattern);
                    }
                    return matches;
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\":\"Nao autorizado\",\"message\":\"%s\"}", message);
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
