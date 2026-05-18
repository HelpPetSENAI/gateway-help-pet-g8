package com.helppet.gateway.repository;

import com.helppet.gateway.entity.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para acesso aos dados de métricas diárias.
 */
@Repository
public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, Long> {

    /**
     * Buscar métricas por data específica.
     */
    Optional<DailyMetrics> findByDateMetric(LocalDate dateMetric);

    /**
     * Buscar todas as métricas dos últimos 7 dias (rolling window).
     */
    @Query("SELECT m FROM DailyMetrics m WHERE m.dateMetric >= :sevenDaysAgo ORDER BY m.dateMetric ASC")
    List<DailyMetrics> findLast7Days(LocalDate sevenDaysAgo);

    /**
     * Deletar métricas anteriores a 7 dias (cleanup automático).
     * Retorna o número de registros deletados.
     */
    @Modifying
    long deleteByDateMetricBefore(LocalDate cutoffDate);
}
