package com.helppet.gateway.controller;

import com.helppet.gateway.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final JwtProvider jwtProvider;

    public DemoController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * Retorna um token de admin válido para fins de demonstração.
     * Este endpoint é público e não requer autenticação.
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> getDemoToken() {
        // Gera um token com role ADMIN válido por 24h
        String token = jwtProvider.generateToken("joao@example.com", "1", "ADMIN");

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("expiresIn", 86400); // 24 horas em segundos

        return ResponseEntity.ok(response);
    }
}
