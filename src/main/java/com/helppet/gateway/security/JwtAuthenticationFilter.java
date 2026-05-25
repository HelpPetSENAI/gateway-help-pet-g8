package com.helppet.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.ArrayList;
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
import org.springframework.http.server.reactive.ServerHttpRequest;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${api.security.public-paths:/auth/**,/api/v1/auth/**,/actuator/health,/actuator/health/**,/api/health}")
    private String[] publicPaths;

    @Value("${api.security.public-post-paths:/api/v1/users,/api/v1/users/login}")
    private String[] publicPostPaths;

    @Value("${api.security.internal-token}")
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
        String role = claims.get("role", String.class);

        // Reutiliza requestId de entrada quando presente para correlacao ponta a ponta
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;

        log.debug("JWT valido | usuario: {} | userId: {} | requestId: {}", email, userId, requestId);

        // Adiciona custom headers ao request instanciando um novo ServerHttpRequestDecorator
        ServerHttpRequest mutatedRequest = new org.springframework.http.server.reactive.ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.set("X-User-Id", userId != null ? userId : "");
                headers.set("X-User-Email", email != null ? email : "");
                headers.set("X-User-Role", role != null ? role : "USER");
                headers.set("X-Request-Id", finalRequestId);
                if (internalServiceToken != null) {
                    headers.set("X-Internal-Token", internalServiceToken);
                }
                return headers;
            }
        };

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null && !role.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, null, authorities);

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private boolean isPublicPath(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "";

        if (matchesAnyPattern(path, List.of(publicPaths))) {
            return true;
        }

        if ("POST".equals(method) && matchesAnyPattern(path, List.of(publicPostPaths))) {
            return true;
        }

        // Permitir CORS OPTIONS preflight
        if ("OPTIONS".equals(method)) {
            return true;
        }

        return false;
    }

    private boolean matchesAnyPattern(String path, List<String> patterns) {
        return patterns.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\":\"Nao autorizado\",\"message\":\"%s\"}", message);
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
