package com.helppet.gateway.controller;

import com.helppet.gateway.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint para obter métricas de requisições e respostas.
 * Retorna dados dos últimos 7 dias divididos por dia da semana.
 * Dados antigos são automaticamente deletados.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * GET /api/metrics/daily
     * Retorna métricas dos últimos 7 dias.
     *
     * Exemplo de resposta:
     * {
     *   "current_week": [
     *     {
     *       "dayOfWeek": 1,
     *       "dayName": "Segunda",
     *       "date": "2026-05-19",
     *       "requests": 150,
     *       "responses": 140,
     *       "success": 130,
     *       "errors": 10,
     *       "successRate": 92.9
     *     },
     *     ...
     *   ],
     *   "totalRequests": 1050,
     *   "totalResponses": 980,
     *   "totalSuccess": 920,
     *   "totalErrors": 60,
     *   "weekAverageSuccessRate": 93.9,
     *   "lastUpdated": 1684444800000
     * }
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyMetrics() {
        Map<String, Object> metrics = metricsService.getLast7DaysMetrics();
        return ResponseEntity.ok(metrics);
    }
}
