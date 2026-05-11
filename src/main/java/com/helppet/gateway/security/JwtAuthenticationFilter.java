package com.helppet.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
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

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Rotas publicas: nao exigem autenticacao
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

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

        // Gera um requestId unico para rastreamento distribuido
        String requestId = UUID.randomUUID().toString();

        log.debug("JWT valido | usuario: {} | userId: {} | requestId: {}", email, userId, requestId);

        // Monta a exchange com os headers injetados para os microservicos downstream
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(req -> req.headers(headers -> {
                    headers.set("X-User-Id", userId != null ? userId : "");
                    headers.set("X-User-Email", email != null ? email : "");
                    headers.set("X-Request-Id", requestId);
                }))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/actuator/")
                || path.equals("/actuator");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\":\"Nao autorizado\",\"message\":\"%s\"}", message);
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
