package com.helppet.gateway.dto;

/**
 * Resposta de autenticacao com token JWT.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String email,
        String userId,
        String role,
        String message
) {}
