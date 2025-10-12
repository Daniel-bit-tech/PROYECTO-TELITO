package com.example.telitodev.service;

import com.example.telitodev.entity.MetricaApi;
import com.example.telitodev.repository.MetricaApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
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

    /* ===== NUEVOS KPIs AVANZADOS ===== */
    
    /**
     * Obtiene el throughput actual (requests por minuto)
     */
    @Transactional(readOnly = true)
    public double getCurrentThroughput() {
        Double throughput = metricaApiRepository.getCurrentThroughput();
        return throughput != null ? Math.round(throughput * 100.0) / 100.0 : 0.0;
    }
    
    /**
     * Obtiene la disponibilidad del sistema (últimas 24h)
     */
    @Transactional(readOnly = true)
    public double getSystemAvailability() {
        Double availability = metricaApiRepository.getSystemAvailability();
        return availability != null ? Math.round(availability * 100.0) / 100.0 : 100.0;
    }
    
    /**
     * Obtiene el costo total del sistema
     */
    @Transactional(readOnly = true)
    public double getTotalCost() {
        return metricaApiRepository.findAll().stream()
                .filter(m -> m.getCosto() != null)
                .mapToDouble(m -> m.getCosto().doubleValue())
                .sum();
    }
    
    /**
     * Obtiene las APIs más utilizadas
     */
    @Transactional(readOnly = true)
    public List<TopApiDTO> getTopUsedApis() {
        try {
            var rows = metricaApiRepository.getTopUsedApis();
            return rows.stream().map(row -> new TopApiDTO(
                    (String) row[0],           // nombre
                    ((Number) row[1]).longValue(),  // totalRequests
                    ((Number) row[2]).intValue(),   // avgLatency
                    ((Number) row[3]).doubleValue() // errorRate
            )).toList();
        } catch (Exception e) {
            // Si hay error (tabla vacía, etc.), retorna lista vacía
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene métricas de costo por API
     */
    @Transactional(readOnly = true)
    public List<CostApiDTO> getCostMetricsByApi() {
        try {
            var rows = metricaApiRepository.getCostMetricsByApi();
            return rows.stream().map(row -> new CostApiDTO(
                    (String) row[0],              // nombre
                    ((Number) row[1]).doubleValue(), // totalCost
                    ((Number) row[2]).doubleValue(), // avgCost
                    ((Number) row[3]).longValue()    // requests
            )).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene throughput por hora (últimas 24h)
     */
    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getThroughputByHour() {
        try {
            var rows = metricaApiRepository.getThroughputByHour();
            var labels = rows.stream().map(r -> String.format("%02d:00", ((Number) r[0]).intValue())).toList();
            var data = rows.stream().map(r -> ((Number) r[1]).longValue()).toList();
            return new ChartSeriesLongDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesLongDTO(new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /**
     * Obtiene tendencia de latencia (últimos 7 días)
     */
    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyTrend() {
        try {
            var rows = metricaApiRepository.getLatencyTrend();
            var labels = rows.stream().map(r -> r[0].toString()).toList();
            var data = rows.stream().map(r -> ((Number) r[1]).intValue()).toList();
            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesDTO(new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /**
     * Obtiene distribución de uso por entorno
     */
    @Transactional(readOnly = true)
    public List<EnvironmentUsageDTO> getUsageByEnvironment() {
        try {
            var rows = metricaApiRepository.getUsageByEnvironment();
            return rows.stream().map(row -> new EnvironmentUsageDTO(
                    (String) row[0],              // nombre
                    ((Number) row[1]).longValue(), // requests
                    ((Number) row[2]).doubleValue() // percentage
            )).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /* ===== DTOs ADICIONALES ===== */
    public record TopApiDTO(String nombre, long totalRequests, int avgLatency, double errorRate) {}
    public record CostApiDTO(String nombre, double totalCost, double avgCost, long requests) {}
    public record EnvironmentUsageDTO(String nombre, long requests, double percentage) {}
}
