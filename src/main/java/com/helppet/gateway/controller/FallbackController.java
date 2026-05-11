package com.helppet.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller de fallback para quando um microservico esta indisponivel.
 * Acionado pelo CircuitBreaker configurado nas rotas do Gateway.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/service-unavailable")
    public ResponseEntity<Map<String, Object>> serviceUnavailable() {
        log.warn("Fallback acionado: microservico indisponivel");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Servico temporariamente indisponivel",
                        "message", "O servico esta fora do ar ou sobrecarregado. Tente novamente em instantes.",
                        "timestamp", Instant.now().toString()
                ));
    }
}
