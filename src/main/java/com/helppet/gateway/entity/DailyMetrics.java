package com.helppet.gateway.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidade para armazenar métricas diárias de requisições e respostas.
 * Mantém histórico de 7 dias (rolling window) - dados anteriores são deletados automaticamente.
 */
@Entity
@Table(name = "daily_metrics")
public class DailyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate dateMetric;

    @Column(nullable = false)
    private Integer dayOfWeek; // 1=MONDAY, 7=SUNDAY

    @Column(nullable = false)
    private String dayName; // "Segunda", "Terça", etc

    @Column(nullable = false)
    private Long requestCount = 0L;

    @Column(nullable = false)
    private Long responseCount = 0L;

    @Column(nullable = false)
    private Long successCount = 0L; // Status 2xx

    @Column(nullable = false)
    private Long errorCount = 0L; // Status 4xx e 5xx

    public DailyMetrics() {}

    public DailyMetrics(LocalDate dateMetric, Integer dayOfWeek, String dayName) {
        this.dateMetric = dateMetric;
        this.dayOfWeek = dayOfWeek;
        this.dayName = dayName;
        this.requestCount = 0L;
        this.responseCount = 0L;
        this.successCount = 0L;
        this.errorCount = 0L;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateMetric() {
        return dateMetric;
    }

    public void setDateMetric(LocalDate dateMetric) {
        this.dateMetric = dateMetric;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(Long responseCount) {
        this.responseCount = responseCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public void incrementRequest() {
        this.requestCount++;
    }

    public void incrementResponse() {
        this.responseCount++;
    }

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementError() {
        this.errorCount++;
    }

    public Double getSuccessRate() {
        if (responseCount == 0) return 0.0;
        return (successCount * 100.0) / responseCount;
    }
}
