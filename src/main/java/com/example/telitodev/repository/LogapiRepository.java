package com.example.telitodev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.telitodev.entity.LogApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface LogapiRepository extends JpaRepository<LogApi, Integer> {

        @Query(value = "SELECT COUNT(*) FROM logapi WHERE DATE(fecha) = CURDATE()", nativeQuery = true)
        long countRequestsToday();

        @Query(value = "SELECT (SUM(CASE WHEN estadohttp < 400 THEN 1 ELSE 0 END) / COUNT(*)) * 100 " +
                        "FROM logapi WHERE fecha >= NOW() - INTERVAL 24 HOUR", nativeQuery = true)
        Optional<Double> calculateSuccessRateLast24Hours();

        @Query(value = "SELECT AVG(tiempoRespuestams) FROM logapi WHERE fecha >= NOW() - INTERVAL 24 HOUR", nativeQuery = true)
        Optional<Double> findAverageLatencyLast24Hours();

        @Query(value = "SELECT COUNT(*) FROM logapi WHERE estadohttp >= 400 AND fecha >= NOW() - INTERVAL 24 HOUR", nativeQuery = true)
        long countErrorsLast24Hours();

        // PARA LA PAGINA DE METRICAS:

        @Query(value = "SELECT AVG(tiempoRespuestaMs) FROM logapi", nativeQuery = true)
        Optional<Double> findAverageLatency();

        @Query(value = "SELECT (SUM(CASE WHEN estadoHttp < 400 THEN 1 ELSE 0 END) * 1.0 / COUNT(*)) * 100 FROM logapi", nativeQuery = true)
        Optional<Double> calculateSuccessRate();

        @Query(value = "SELECT (SUM(CASE WHEN estadoHttp >= 400 THEN 1 ELSE 0 END) * 1.0 / COUNT(*)) * 100 FROM logapi", nativeQuery = true)
        Optional<Double> calculateErrorRate();

        @Query(value = "SELECT endpoint, AVG(tiempoRespuestaMs) as avg_latency FROM logapi GROUP BY endpoint ORDER BY avg_latency DESC LIMIT 6", nativeQuery = true)
        List<Map<String, Object>> findAverageLatencyByEndpoint();

        @Query(value = "SELECT HOUR(fecha) as hour, COUNT(*) as count FROM logapi GROUP BY HOUR(fecha) ORDER BY hour", nativeQuery = true)
        List<Map<String, Object>> countRequestsGroupedByHour();

        @Query(value = "SELECT estadoHttp, COUNT(*) as count FROM logapi GROUP BY estadoHttp", nativeQuery = true)
        List<Map<String, Object>> countRequestsGroupedByStatusCode();

        @Query("SELECT l.fecha AS timestamp, l.endpoint AS title, l.endpoint AS description, l.estadoHttp AS severity "
                        +
                        "FROM LogApi l WHERE l.estadoHttp >= 400 ORDER BY l.fecha DESC")
        List<Map<String, Object>> findRecentAlerts();

        @Query("SELECT l.endpoint AS endpoint, COUNT(l) AS totalRequests, AVG(l.tiempoRespuestaMs) AS latency, " +
                        "SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) * 100.0 / COUNT(l) AS errorRate " +
                        "FROM LogApi l GROUP BY l.endpoint ORDER BY totalRequests DESC")
        List<Map<String, Object>> findApiPerformanceMetrics();

        // Queries para KPIs
        @Query("SELECT COUNT(l) FROM LogApi l WHERE l.api.idApi = :apiId")
        Long countByApi_IdApi(@Param("apiId") Integer apiId);

        @Query("SELECT COUNT(DISTINCT l.usuario.dni) FROM LogApi l WHERE l.api.idApi = :apiId")
        Long countDistinctUsuariosByApi(@Param("apiId") Integer apiId);

        @Query("SELECT AVG(l.tiempoRespuestaMs) FROM LogApi l WHERE l.api.idApi = :apiId")
        Double avgLatenciaByApi(@Param("apiId") Integer apiId);

        @Query("SELECT COUNT(l) FROM LogApi l WHERE l.api.idApi = :apiId AND l.estadoHttp >= 400")
        Long countErroresByApi(@Param("apiId") Integer apiId);

        // --- Queries Aggregados para MetricsService (Unificación) ---

        // 1. Suma de llamadas en rango
        // 1. Suma de llamadas en rango
        @Query("SELECT COUNT(l) FROM LogApi l WHERE " +
                        "(l.fecha BETWEEN :start AND :end) AND " +
                        "(:apiId IS NULL OR l.api.idApi = :apiId)")
        Long sumLlamadasBetween(@Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end,
                        @Param("apiId") Integer apiId);

        // 2. Promedio Latencia en rango
        @Query("SELECT AVG(l.tiempoRespuestaMs) FROM LogApi l WHERE " +
                        "(l.fecha BETWEEN :start AND :end) AND " +
                        "(:apiId IS NULL OR l.api.idApi = :apiId)")
        Double avgLatenciaBetween(@Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end,
                        @Param("apiId") Integer apiId);

        // 3. Suma de errores en rango
        @Query("SELECT COUNT(l) FROM LogApi l WHERE " +
                        "(l.fecha BETWEEN :start AND :end) AND " +
                        "l.estadoHttp >= 400 AND " +
                        "(:apiId IS NULL OR l.api.idApi = :apiId)")
        Long sumErroresBetween(@Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end,
                        @Param("apiId") Integer apiId);

        // 4. Counts para throughput (minutos con tráfico)
        @Query(value = "SELECT COUNT(DISTINCT DATE_FORMAT(fecha, '%Y-%m-%d %H:%i')) FROM logapi " +
                        "WHERE fecha BETWEEN :start AND :end " +
                        "AND (:apiId IS NULL OR idAPI = :apiId)", nativeQuery = true)
        Long countMinutesWithCallsBetween(@Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end,
                        @Param("apiId") Integer apiId);

        // 5. Latency Bars by API
        @Query("SELECT l.api.nombre, AVG(l.tiempoRespuestaMs) FROM LogApi l " +
                        "WHERE (l.fecha BETWEEN :start AND :end) " +
                        "AND (:apiId IS NULL OR l.api.idApi = :apiId) " +
                        "GROUP BY l.api.nombre")
        List<Object[]> avgLatencyByApi(@Param("apiId") Integer apiId,
                        @Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end);

        // 6. Success vs Errors
        @Query("SELECT " +
                        "SUM(CASE WHEN l.estadoHttp < 400 THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) " +
                        "FROM LogApi l WHERE (l.fecha BETWEEN :start AND :end) " +
                        "AND (:apiId IS NULL OR l.api.idApi = :apiId)")
        Object successVsErrors(@Param("apiId") Integer apiId,
                        @Param("start") java.time.LocalDateTime start,
                        @Param("end") java.time.LocalDateTime end);

        // 7. Throughput by Hour
        @Query(value = "SELECT HOUR(fecha) as h, COUNT(*) as c FROM logapi " +
                        "WHERE fecha >= NOW() - INTERVAL 24 HOUR " +
                        "GROUP BY HOUR(fecha) ORDER BY h", nativeQuery = true)
        List<Object[]> getThroughputByHour();

        // 8. Latency Trend (Last 7 days)
        @Query(value = "SELECT DATE(fecha) as d, AVG(tiempoRespuestams) as val FROM logapi " +
                        "WHERE fecha >= NOW() - INTERVAL 7 DAY " +
                        "GROUP BY DATE(fecha) ORDER BY d", nativeQuery = true)
        List<Object[]> getLatencyTrend();

        // 9. Top APIs Used
        @Query("SELECT l.api.nombre, COUNT(l) as total, AVG(l.tiempoRespuestaMs), " +
                        "(SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) * 1.0 / COUNT(l)) " +
                        "FROM LogApi l " +
                        "GROUP BY l.api.nombre " +
                        "ORDER BY total DESC")
        List<Object[]> getTopUsedApis();

        // 11. Environment Usage (Asumimos ID 1 = Prod, others based on logic if column
        // existed, else dummy)
        // Since LogApi is mostly Prod, we can just group by 'Produccion' literal or
        // similar.
        // For now, let's just return total grouped by a static 'Produccion' to
        // satisfied the interface
        @Query(value = "SELECT 'Produccion', COUNT(*), 100.0 FROM logapi", nativeQuery = true)
        List<Object[]> getUsageByEnvironment();

        // Helper for fast count
        @Query("SELECT COUNT(l) FROM LogApi l")
        Long sumLlamadas();

        @Query("SELECT COUNT(l) FROM LogApi l WHERE l.api.idApi = :apiId AND l.metodoHttp = :metodo")
        Long countByApiAndMetodo(@Param("apiId") Integer apiId, @Param("metodo") String metodo);

        List<LogApi> findTop20ByOrderByFechaDesc();

        @Query("SELECT l.api.nombre, COUNT(l) as total, AVG(l.tiempoRespuestaMs), " +
                        "(SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) * 1.0 / COUNT(l)) " +
                        "FROM LogApi l " +
                        "WHERE l.api.idApi IN :apiIds " +
                        "GROUP BY l.api.nombre " +
                        "ORDER BY total DESC")
        List<Object[]> getTopUsedApisFiltered(@Param("apiIds") List<Integer> apiIds);

        @Query("SELECT " +
                        "SUM(CASE WHEN l.estadoHttp < 400 THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) " +
                        "FROM LogApi l WHERE (l.fecha BETWEEN :start AND :end) " +
                        "AND l.api.idApi IN :apiIds")
        Object successVsErrorsFiltered(@Param("apiIds") List<Integer> apiIds,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT HOUR(fecha) as h, COUNT(*) as c FROM logapi " +
                        "WHERE fecha >= NOW() - INTERVAL 24 HOUR " +
                        "AND idapi IN :apiIds " +
                        "GROUP BY HOUR(fecha) ORDER BY h", nativeQuery = true)
        List<Object[]> getThroughputByHourFiltered(@Param("apiIds") List<Integer> apiIds);

        @Query("SELECT l.fecha AS timestamp, l.endpoint AS title, l.endpoint AS description, l.estadoHttp AS severity "
                        + "FROM LogApi l WHERE l.estadoHttp >= 400 AND l.api.idApi IN :apiIds ORDER BY l.fecha DESC")
        List<Map<String, Object>> findRecentAlertsFiltered(@Param("apiIds") List<Integer> apiIds);

        @Query(value = "SELECT CONCAT(metodoHttp, ' ', endpoint) AS endpoint, AVG(tiempoRespuestaMs) AS avg_latency " +
                        "FROM logapi WHERE idapi IN :apiIds " +
                        "GROUP BY metodoHttp, endpoint " +
                        "ORDER BY avg_latency DESC LIMIT 10;", nativeQuery = true)
        List<Map<String, Object>> findAverageLatencyByEndpointFiltered(@Param("apiIds") List<Integer> apiIds);

        @Query(value = "SELECT DATE(fecha) as d, AVG(tiempoRespuestams) as val FROM logapi " +
                        "WHERE fecha >= NOW() - INTERVAL 7 DAY " +
                        "AND idapi IN :apiIds " +
                        "GROUP BY DATE(fecha) ORDER BY d", nativeQuery = true)
        List<Object[]> getLatencyTrendFiltered(@Param("apiIds") List<Integer> apiIds);
}
