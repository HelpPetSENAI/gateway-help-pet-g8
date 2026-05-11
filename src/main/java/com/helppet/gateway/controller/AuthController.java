package com.helppet.gateway.controller;

import com.helppet.gateway.dto.LoginRequest;
import com.helppet.gateway.dto.LoginResponse;
import com.helppet.gateway.dto.TokenValidationResponse;
import com.helppet.gateway.security.JwtProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller de autenticacao do Gateway.
 *
 * IMPORTANTE: Por enquanto o login delega a validacao de credenciais
 * para o microservico helppet via /api/v1/users/login.
 * Quando um auth-service dedicado for criado, este controller sera o ponto central.
 *
 * Endpoints publicos (nao exigem JWT):
 * - POST /auth/refresh  -> renova token valido
 * - GET  /auth/validate -> valida se um token e valido
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtProvider jwtProvider;

    public AuthController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * Renova um token JWT valido.
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<LoginResponse>> refresh(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.isTokenValid(token)) {
            log.warn("Tentativa de refresh com token invalido");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String email = jwtProvider.extractEmail(token);
        String userId = jwtProvider.extractUserId(token);
        String newToken = jwtProvider.generateToken(email, userId);

        log.info("Token renovado para usuario: {}", email);

        return Mono.just(ResponseEntity.ok(new LoginResponse(
                newToken, "Bearer", 86400L, email, userId, null
        )));
    }

    /**
     * Valida se um token JWT e valido e nao esta expirado.
     */
    @GetMapping("/validate")
    public Mono<ResponseEntity<TokenValidationResponse>> validate(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.ok(new TokenValidationResponse(false, null, null)));
        }

        String token = authHeader.substring(7);
        boolean valid = jwtProvider.isTokenValid(token);

        if (!valid) {
            return Mono.just(ResponseEntity.ok(new TokenValidationResponse(false, null, null)));
        }

        String email = jwtProvider.extractEmail(token);
        String userId = jwtProvider.extractUserId(token);

        log.debug("Validacao de token: valido=true, usuario={}", email);
        return Mono.just(ResponseEntity.ok(new TokenValidationResponse(true, email, userId)));
    }
}
