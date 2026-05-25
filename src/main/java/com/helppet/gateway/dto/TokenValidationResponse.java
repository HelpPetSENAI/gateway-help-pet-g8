package com.helppet.gateway.dto;

/**
 * Resposta da validacao de token JWT.
 */
public record TokenValidationResponse(
        boolean valid,
        String email,
        String userId,
        String role
) {}
