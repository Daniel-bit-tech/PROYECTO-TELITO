package com.example.telitodev.service;

import com.example.telitodev.entity.MetricaApi;
import com.example.telitodev.repository.MetricaApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MetricsService {

    @Autowired
    private MetricaApiRepository metricaApiRepository;

    /* ===== KPIs (versión simple con findAll) ===== */
    public long getTotalRequests() {
        return metricaApiRepository.findAll().stream()
                .mapToLong(MetricaApi::getLlamadas)
                .sum();
    }

    public int getAverageLatency() {
        return (int) metricaApiRepository.findAll().stream()
                .mapToDouble(MetricaApi::getLatenciaPromedio)
                .average()
                .orElse(0.0);
    }

    public double getSuccessRate() {
        List<MetricaApi> metricas = metricaApiRepository.findAll();
        long totalLlamadas = metricas.stream().mapToLong(MetricaApi::getLlamadas).sum();
        long totalErrores  = metricas.stream().mapToLong(MetricaApi::getErrores).sum();
        if (totalLlamadas == 0) return 0.0;
        return (double) (totalLlamadas - totalErrores) / totalLlamadas * 100.0;
    }

    public double getErrorRate() {
        List<MetricaApi> metricas = metricaApiRepository.findAll();
        long totalLlamadas = metricas.stream().mapToLong(MetricaApi::getLlamadas).sum();
        long totalErrores  = metricas.stream().mapToLong(MetricaApi::getErrores).sum();
        if (totalLlamadas == 0) return 0.0;
        return (double) totalErrores / totalLlamadas * 100.0;
    }

    /* ===== DTOs para charts ===== */
    public record ChartSeriesDTO(List<String> labels, List<Integer> data) {}
    public record ChartSeriesLongDTO(List<String> labels, List<Long> data) {}

    /* ===== Serie: Latencia promedio por API (barras) ===== */
    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyBarsByApi(Integer idApi,
                                              Integer idEntorno,
                                              Timestamp start,
                                              Timestamp end) {
        var rows = metricaApiRepository.avgLatencyByApi(idApi, idEntorno, start, end);
        var labels = rows.stream().map(r -> (String) r[0]).toList();
        var data   = rows.stream().map(r -> ((Number) r[1]).intValue()).toList();
        return new ChartSeriesDTO(labels, data);
    }

    /* ===== Donut: Éxito vs Errores ===== */
    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getStatusDistribution(Integer idApi,
                                                    Integer idEntorno,
                                                    Timestamp start,
                                                    Timestamp end) {
        Object[] row = metricaApiRepository.successVsErrors(idApi, idEntorno, start, end);
        long exitos  = ((Number) row[0]).longValue();
        long errores = ((Number) row[1]).longValue();
        if (exitos < 0) exitos = 0; // por seguridad
        return new ChartSeriesLongDTO(
                List.of("Éxito (2xx/3xx)", "Errores (4xx/5xx)"),
                List.of(exitos, errores)
        );
    }

    /* ===== Búsquedas y listado ===== */
    @Transactional(readOnly = true)
    public List<MetricaApi> search(Integer idApi, Integer idEntorno, Timestamp start, Timestamp end) {
        return metricaApiRepository.search(idApi, idEntorno, start, end);
    }

    @Transactional(readOnly = true)
    public List<MetricaApi> getAllMetrics() {
        return metricaApiRepository.findAll();
    }

    /* ===== KPIs (opcional: más eficientes) ===== */
    @Transactional(readOnly = true)
    public long getTotalRequestsFast() {
        Long sum = metricaApiRepository.sumLlamadas();
        return (sum == null) ? 0L : sum;
    }

    @Transactional(readOnly = true)
    public int getAverageLatencyFast() {
        Double avg = metricaApiRepository.avgLatencia();
        return (int) Math.round(avg == null ? 0.0 : avg);
    }

    @Transactional(readOnly = true)
    public double getSuccessRateFast() {
        long total = getTotalRequestsFast();
        long err   = (metricaApiRepository.sumErrores() == null) ? 0L : metricaApiRepository.sumErrores();
        if (total == 0) return 0.0;
        return (double) (total - err) / total * 100.0;
    }
}
