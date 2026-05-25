package com.helppet.gateway.controller;

import com.helppet.gateway.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<MetricsService.DailyMetrics>> getWeeklyMetrics() {
        return ResponseEntity.ok(metricsService.getLast7DaysMetrics());
    }
}
