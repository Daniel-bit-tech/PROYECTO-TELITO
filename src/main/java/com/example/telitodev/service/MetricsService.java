package com.example.telitodev.service;

import com.example.telitodev.entity.MetricaApi;
import com.example.telitodev.repository.MetricaApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MetricsService {

    @Autowired
    private MetricaApiRepository metricaApiRepository;


    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return (current > 0) ? 100.0 : 0.0;
        }
        double change = ((current - previous) / previous) * 100.0;
        return Math.round(change * 10.0) / 10.0;
    }


    /* ===== KPIs (versión simple con findAll) ===== */

    //public long getTotalRequests() {
    //    return metricaApiRepository.findAll().stream()
    //            .mapToLong(MetricaApi::getLlamadas)
    //            .sum();
    //}
    @Transactional(readOnly = true)
    public long getTotalRequests() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp startPeriod = Timestamp.valueOf(LocalDateTime.now().minusDays(1));

        return metricaApiRepository.sumLlamadasBetween(startPeriod, now, null, null);
    }


    // public int getAverageLatency() {
    //    return (int) metricaApiRepository.findAll().stream()
    //            .mapToDouble(MetricaApi::getLatenciaPromedio)
    //            .average()
    //            .orElse(0.0);
    //}
    @Transactional(readOnly = true)
    public double getAverageLatency() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp startPeriod = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Double avg = metricaApiRepository.avgLatenciaBetween(startPeriod, now, null, null);
        return avg != null ? avg : 0.0;
    }



    //public double getSuccessRate() {
    //    List<MetricaApi> metricas = metricaApiRepository.findAll();
    //    long totalLlamadas = metricas.stream().mapToLong(MetricaApi::getLlamadas).sum();
    //    long totalErrores  = metricas.stream().mapToLong(MetricaApi::getErrores).sum();
    //    if (totalLlamadas == 0) return 0.0;
    //    return (double) (totalLlamadas - totalErrores) / totalLlamadas * 100.0;
    // }


    @Transactional(readOnly = true)
    public double getSuccessRate() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp startPeriod = Timestamp.valueOf(LocalDateTime.now().minusDays(1));

        Long totalLlamadas = metricaApiRepository.sumLlamadasBetween(startPeriod, now, null, null);
        Long totalErrores = metricaApiRepository.sumErroresBetween(startPeriod, now, null, null);

        if (totalLlamadas == null || totalLlamadas == 0) return 100.0;

        long exitosas = totalLlamadas - (totalErrores != null ? totalErrores : 0L);
        double rate = Math.max(0.0, (double) exitosas / totalLlamadas * 100.0);
        return Math.round(rate * 10.0) / 10.0;
    }


    //public double getErrorRate() {
    //    List<MetricaApi> metricas = metricaApiRepository.findAll();
    //    long totalLlamadas = metricas.stream().mapToLong(MetricaApi::getLlamadas).sum();
    //    long totalErrores  = metricas.stream().mapToLong(MetricaApi::getErrores).sum();
    //    if (totalLlamadas == 0) return 0.0;
    //    return (double) totalErrores / totalLlamadas * 100.0;
    //}
    @Transactional(readOnly = true)
    public double getErrorRate() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp startPeriod = Timestamp.valueOf(LocalDateTime.now().minusDays(1));

        Long totalLlamadas = metricaApiRepository.sumLlamadasBetween(startPeriod, now, null, null);
        Long totalErrores = metricaApiRepository.sumErroresBetween(startPeriod, now, null, null);

        if (totalLlamadas == null || totalLlamadas == 0) return 0.0;

        double rate = Math.max(0.0, (double) (totalErrores != null ? totalErrores : 0L) / totalLlamadas * 100.0);
        return Math.round(rate * 10.0) / 10.0;
    }

    // ===== NUEVOSSS =========
    @Transactional(readOnly = true)
    public double getTotalRequestsChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        long current = metricaApiRepository.sumLlamadasBetween(yesterday, now, null, null);
        long previous = metricaApiRepository.sumLlamadasBetween(dayBefore, yesterday, null, null);

        return calculatePercentageChange(current, previous);
    }
    @Transactional(readOnly = true)
    public double getAverageLatencyChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Double currentLatency = metricaApiRepository.avgLatenciaBetween(yesterday, now, null, null);
        Double previousLatency = metricaApiRepository.avgLatenciaBetween(dayBefore, yesterday, null, null);

        return calculatePercentageChange(
                currentLatency != null ? currentLatency : 0.0,
                previousLatency != null ? previousLatency : 0.0
        );
    }
    @Transactional(readOnly = true)
    public double getSuccessRateChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(yesterday, now, null, null);
        Long currentErrors = metricaApiRepository.sumErroresBetween(yesterday, now, null, null);
        double currentSuccessRate = (currentCalls == 0) ? 100.0 : Math.max(0.0, (double)(currentCalls - currentErrors) / currentCalls * 100.0);

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(dayBefore, yesterday, null, null);
        Long previousErrors = metricaApiRepository.sumErroresBetween(dayBefore, yesterday, null, null);
        double previousSuccessRate = (previousCalls == 0) ? 100.0 : Math.max(0.0, (double)(previousCalls - previousErrors) / previousCalls * 100.0);

        double change = currentSuccessRate - previousSuccessRate;
        return Math.round(change * 10.0) / 10.0;
    }
    @Transactional(readOnly = true)
    public double getErrorRateChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(yesterday, now, null, null);
        Long currentErrors = metricaApiRepository.sumErroresBetween(yesterday, now, null, null);
        double currentErrorRate = (currentCalls == 0) ? 0.0 : Math.max(0.0, (double)currentErrors / currentCalls * 100.0);

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(dayBefore, yesterday, null, null);
        Long previousErrors = metricaApiRepository.sumErroresBetween(dayBefore, yesterday, null, null);
        double previousErrorRate = (previousCalls == 0) ? 0.0 : Math.max(0.0, (double)previousErrors / previousCalls * 100.0);

        double change = currentErrorRate - previousErrorRate;
        return Math.round(change * 10.0) / 10.0;
    }
    @Transactional(readOnly = true)
    public double getThroughputChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp hourAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
        Timestamp twoHoursAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(hourAgo, now, null, null);
        Long currentMinutes = metricaApiRepository.countMinutesWithCallsBetween(hourAgo, now, null, null);
        double currentThroughput = (currentMinutes == 0) ? 0.0 : (double)currentCalls / currentMinutes;

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(twoHoursAgo, hourAgo, null, null);
        Long previousMinutes = metricaApiRepository.countMinutesWithCallsBetween(twoHoursAgo, hourAgo, null, null);
        double previousThroughput = (previousMinutes == 0) ? 0.0 : (double)previousCalls / previousMinutes;

        return calculatePercentageChange(currentThroughput, previousThroughput);
    }
    @Transactional(readOnly = true)
    public double getAvailabilityChange() {

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(yesterday, now, null, null);
        Long currentErrors = metricaApiRepository.sumErroresBetween(yesterday, now, null, null);
        double currentAvailability = (currentCalls == 0) ? 100.0 : Math.max(0.0, (double)(currentCalls - currentErrors) / currentCalls * 100.0);

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(dayBefore, yesterday, null, null);
        Long previousErrors = metricaApiRepository.sumErroresBetween(dayBefore, yesterday, null, null);
        double previousAvailability = (previousCalls == 0) ? 100.0 : Math.max(0.0, (double)(previousCalls - previousErrors) / previousCalls * 100.0);

        double change = currentAvailability - previousAvailability;
        return Math.round(change * 10.0) / 10.0;
    }
    @Transactional(readOnly = true)
    public double getTotalCostChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        BigDecimal currentCostBD = metricaApiRepository.sumCostoBetween(yesterday, now, null, null);
        BigDecimal previousCostBD = metricaApiRepository.sumCostoBetween(dayBefore, yesterday, null, null);

        double currentCost = (currentCostBD != null) ? currentCostBD.doubleValue() : 0.0;
        double previousCost = (previousCostBD != null) ? previousCostBD.doubleValue() : 0.0;

        return calculatePercentageChange(currentCost, previousCost);
    }


    /* ===== DTOs para charts ===== */
    public record ChartSeriesDTO(List<String> labels, List<Double> data) {}
    public record ChartSeriesLongDTO(List<String> labels, List<Long> data) {}


    /* ===== Serie: Latencia promedio por API (barras) ===== */
    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyBarsByApi(Integer idApi,
                                              Integer idEntorno,
                                              Timestamp start,
                                              Timestamp end) {

        System.out.println("=== METRICS SERVICE - getLatencyBarsByApi ===");
        System.out.println("Parámetros: idApi=" + idApi + ", idEntorno=" + idEntorno);
        System.out.println("Rango: " + start + " a " + end);

        try {
            var rows = metricaApiRepository.avgLatencyByApi(idApi, idEntorno, start, end);

            System.out.println("Resultados del repositorio: " + rows.size() + " filas");
            for (int i = 0; i < rows.size(); i++) {
                System.out.println("Fila " + i + ": " + Arrays.toString(rows.get(i)));
            }

            var labels = new ArrayList<String>();
            var data   = new ArrayList<Double>();

            for (Object[] row : rows) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    labels.add(row[0].toString());
                    data.add(((Number) row[1]).doubleValue());
                }
            }

            System.out.println("Labels extraídos: " + labels);
            System.out.println("Data extraída: " + data);



            return new ChartSeriesDTO(labels, data);

        } catch (Exception e) {
            System.err.println("❌ Error en getLatencyBarsByApi: " + e.getMessage());
            return new ChartSeriesDTO(new ArrayList<>(), new ArrayList<>());
        }
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

    /* ===== Donut: Éxito vs Errores ===== */
    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getStatusDistribution(Integer idApi,
                                                    Integer idEntorno,
                                                    Timestamp start,
                                                    Timestamp end) {

        System.out.println("=== METRICS SERVICE - getStatusDistribution ===");

        try {
            Object[] row = metricaApiRepository.successVsErrors(idApi, idEntorno, start, end);

            System.out.println("Resultado del repositorio: " + Arrays.toString(row));

            long exitos  = 0;
            long errores = 0;

            if (row != null && row.length >= 2) {
                if (row[0] != null) exitos = ((Number) row[0]).longValue();
                if (row[1] != null) errores = ((Number) row[1]).longValue();
            }

            System.out.println("Éxitos: " + exitos + ", Errores: " + errores);



            return new ChartSeriesLongDTO(
                    List.of("Éxito (2xx/3xx)", "Errores (4xx/5xx)"),
                    List.of(exitos, errores)
            );

        } catch (Exception e) {
            System.err.println("❌ Error en getStatusDistribution: " + e.getMessage());
            return new ChartSeriesLongDTO(new ArrayList<>(), new ArrayList<>());
        }
    }

    /* ===== Datos de ejemplo ===== */
    /* private ChartSeriesDTO getSampleLatencyData() {
        return new ChartSeriesDTO(
                List.of("API Usuarios", "API Pagos", "API Productos", "API Pedidos"),
                List.of(120.0, 200.0, 150.0, 180.0)
        );
    }

    private ChartSeriesLongDTO getSampleStatusData() {
        // Basado en tus KPIs: 25000 total, 99.32% éxito
        long total = 25000L;
        long exitos = (long) (total * 0.9932); // ≈ 24830
        long errores = total - exitos; // ≈ 170
        return new ChartSeriesLongDTO(
                List.of("Éxito (2xx/3xx)", "Errores (4xx/5xx)"),
                List.of(exitos, errores)
        );
    }
    */

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
            var data = rows.stream().map(r -> ((Number) r[1]).doubleValue()).toList();
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
