package com.example.telitodev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.telitodev.entity.LogApi;

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


    @Query("SELECT l.fecha AS timestamp, l.endpoint AS title, l.endpoint AS description, l.estadoHttp AS severity " +
            "FROM LogApi l WHERE l.estadoHttp >= 400 ORDER BY l.fecha DESC")
    List<Map<String, Object>> findRecentAlerts();


    @Query("SELECT l.endpoint AS endpoint, COUNT(l) AS totalRequests, AVG(l.tiempoRespuestaMs) AS latency, " +
            "SUM(CASE WHEN l.estadoHttp >= 400 THEN 1 ELSE 0 END) * 100.0 / COUNT(l) AS errorRate " +
            "FROM LogApi l GROUP BY l.endpoint ORDER BY totalRequests DESC")
    List<Map<String, Object>> findApiPerformanceMetrics();
}
