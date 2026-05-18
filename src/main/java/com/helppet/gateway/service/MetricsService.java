package com.helppet.gateway.service;

import com.helppet.gateway.entity.DailyMetrics;
import com.helppet.gateway.repository.DailyMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para gerenciar métricas diárias de requisições e respostas.
 * Mantém rolling window de 7 dias e limpa dados antigos automaticamente.
 * Usa controle otimista de concorrência para evitar race conditions.
 */
@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);
    private static final int MAX_RETRIES = 3;

    private final DailyMetricsRepository metricsRepository;

    public MetricsService(DailyMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    /**
     * Incrementar contador de request para o dia atual.
     * Com retry em caso de conflito de versão (high concurrency).
     */
    @Transactional
    public void recordRequest() {
        retryOnOptimisticLock(() -> {
            DailyMetrics today = getOrCreateTodayMetrics();
            today.incrementRequest();
            metricsRepository.save(today);
            return null;
        });
    }

    /**
     * Incrementar contadores de response para o dia atual.
     * Com retry em caso de conflito de versão.
     */
    @Transactional
    public void recordResponse(int statusCode) {
        retryOnOptimisticLock(() -> {
            DailyMetrics today = getOrCreateTodayMetrics();
            today.incrementResponse();

            if (statusCode >= 200 && statusCode < 300) {
                today.incrementSuccess();
            } else {
                today.incrementError();
            }

            metricsRepository.save(today);
            return null;
        });
    }

    /**
     * Helper para retry com tratamento de OptimisticLockingFailureException.
     */
    private void retryOnOptimisticLock(java.util.function.Supplier<Void> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                operation.get();
                return;
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    log.error("Falha ao registrar métrica após {} tentativas", MAX_RETRIES, e);
                    throw e;
                }
                log.debug("Conflito de versão ao atualizar métricas. Tentativa {} de {}", attempt, MAX_RETRIES);
                // Pequeno delay antes de retry
                try {
                    Thread.sleep(10 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Obter ou criar métricas para hoje.
     */
    private DailyMetrics getOrCreateTodayMetrics() {
        LocalDate today = LocalDate.now();
        return metricsRepository.findByDateMetric(today)
                .orElseGet(() -> {
                    DayOfWeek dayOfWeek = today.getDayOfWeek();
                    String dayName = getDayName(dayOfWeek);
                    DailyMetrics metrics = new DailyMetrics(today, dayOfWeek.getValue(), dayName);
                    return metricsRepository.save(metrics);
                });
    }

    /**
     * Obter dados dos últimos 7 dias para exibição no gráfico.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLast7DaysMetrics() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6); // Últimos 7 dias incluindo hoje
        List<DailyMetrics> dailyMetrics = metricsRepository.findLast7Days(sevenDaysAgo);

        // Preencher dias faltantes com valores zeros
        Map<LocalDate, DailyMetrics> metricsMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            DailyMetrics metrics = dailyMetrics.stream()
                    .filter(m -> m.getDateMetric().equals(date))
                    .findFirst()
                    .orElse(createEmptyMetrics(date));
            metricsMap.put(date, metrics);
        }

        // Calcular totais
        long totalRequests = metricsMap.values().stream()
                .mapToLong(DailyMetrics::getRequestCount)
                .sum();

        long totalResponses = metricsMap.values().stream()
                .mapToLong(DailyMetrics::getResponseCount)
                .sum();

        long totalSuccess = metricsMap.values().stream()
                .mapToLong(DailyMetrics::getSuccessCount)
                .sum();

        double weekSuccessRate = totalResponses > 0
                ? (totalSuccess * 100.0) / totalResponses
                : 0.0;

        // Montar resposta
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("current_week", metricsMap.values().stream().map(m -> {
            Map<String, Object> day = new HashMap<>();
            day.put("dayOfWeek", m.getDayOfWeek());
            day.put("dayName", m.getDayName());
            day.put("date", m.getDateMetric());
            day.put("requests", m.getRequestCount());
            day.put("responses", m.getResponseCount());
            day.put("success", m.getSuccessCount());
            day.put("errors", m.getErrorCount());
            day.put("successRate", Math.round(m.getSuccessRate() * 10.0) / 10.0);
            return day;
        }).toList());

        response.put("totalRequests", totalRequests);
        response.put("totalResponses", totalResponses);
        response.put("totalSuccess", totalSuccess);
        response.put("totalErrors", totalResponses - totalSuccess);
        response.put("weekAverageSuccessRate", Math.round(weekSuccessRate * 10.0) / 10.0);
        response.put("lastUpdated", System.currentTimeMillis());

        return response;
    }

    /**
     * Criar métricas vazias para um dia específico.
     */
    private DailyMetrics createEmptyMetrics(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return new DailyMetrics(date, dayOfWeek.getValue(), getDayName(dayOfWeek));
    }

    /**
     * Converter DayOfWeek para nome em português.
     */
    private String getDayName(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terça";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    /**
     * Limpeza automática de dados com mais de 7 dias.
     * Executa diariamente à meia-noite (00:00).
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldMetrics() {
        LocalDate cutoffDate = LocalDate.now().minusDays(7);
        long deletedCount = metricsRepository.deleteByDateMetricBefore(cutoffDate);
        if (deletedCount > 0) {
            log.info("Removidas {} registros de métricas anteriores a {}", deletedCount, cutoffDate);
        }
    }
}
