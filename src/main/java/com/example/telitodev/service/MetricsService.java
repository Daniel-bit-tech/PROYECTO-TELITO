package com.example.telitodev.service;

import com.example.telitodev.repository.LogapiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MetricsService {

    @Autowired
    private LogapiRepository logapiRepository;

    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return (current > 0) ? 100.0 : 0.0;
        }
        double change = ((current - previous) / previous) * 100.0;
        return Math.round(change * 10.0) / 10.0;
    }

    /* ===== KPIs GENERALE ===== */

    @Transactional(readOnly = true)
    public long getTotalRequests() {
        return getTotalRequests(null);
    }

    @Transactional(readOnly = true)
    public long getTotalRequests(Integer apiId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startPeriod = LocalDateTime.now().minusDays(1);
        Long val = logapiRepository.sumLlamadasBetween(startPeriod, now, apiId);
        return val != null ? val : 0L;
    }

    @Transactional(readOnly = true)
    public double getAverageLatency() {
        return getAverageLatency(null);
    }

    @Transactional(readOnly = true)
    public double getAverageLatency(Integer apiId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startPeriod = LocalDateTime.now().minusDays(1);
        Double avg = logapiRepository.avgLatenciaBetween(startPeriod, now, apiId);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public double getSuccessRate() {
        return getSuccessRate(null);
    }

    @Transactional(readOnly = true)
    public double getSuccessRate(Integer apiId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startPeriod = LocalDateTime.now().minusDays(1);

        Long totalLlamadas = logapiRepository.sumLlamadasBetween(startPeriod, now, apiId);
        Long totalErrores = logapiRepository.sumErroresBetween(startPeriod, now, apiId);

        if (totalLlamadas == null || totalLlamadas == 0)
            return 100.0;
        double exitosas = (double) (totalLlamadas - (totalErrores != null ? totalErrores : 0L));
        double rate = (exitosas / totalLlamadas) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getErrorRate() {
        return getErrorRate(null);
    }

    @Transactional(readOnly = true)
    public double getErrorRate(Integer apiId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startPeriod = LocalDateTime.now().minusDays(1);

        Long totalLlamadas = logapiRepository.sumLlamadasBetween(startPeriod, now, apiId);
        Long totalErrores = logapiRepository.sumErroresBetween(startPeriod, now, apiId);

        if (totalLlamadas == null || totalLlamadas == 0)
            return 0.0;
        double rate = ((double) (totalErrores != null ? totalErrores : 0L) / totalLlamadas) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getSystemAvailability(Integer apiId) {
        return getSuccessRate(apiId);
    }

    /* ===== CAMBIOS PORCENTUALES ===== */

    @Transactional(readOnly = true)
    public double getTotalRequestsChange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(2);

        Long current = logapiRepository.sumLlamadasBetween(yesterday, now, null);
        Long previous = logapiRepository.sumLlamadasBetween(dayBefore, yesterday, null);

        return calculatePercentageChange(current != null ? current : 0, previous != null ? previous : 0);
    }

    @Transactional(readOnly = true)
    public double getAverageLatencyChange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(2);

        Double current = logapiRepository.avgLatenciaBetween(yesterday, now, null);
        Double previous = logapiRepository.avgLatenciaBetween(dayBefore, yesterday, null);

        return calculatePercentageChange(current != null ? current : 0.0, previous != null ? previous : 0.0);
    }

    @Transactional(readOnly = true)
    public double getSuccessRateChange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(2);

        double current = calculateSuccessRate(yesterday, now);
        double previous = calculateSuccessRate(dayBefore, yesterday);

        return Math.round((current - previous) * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getErrorRateChange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(2);

        double current = calculateErrorRate(yesterday, now);
        double previous = calculateErrorRate(dayBefore, yesterday);

        return Math.round((current - previous) * 10.0) / 10.0;
    }

    private double calculateSuccessRate(LocalDateTime start, LocalDateTime end) {
        Long calls = logapiRepository.sumLlamadasBetween(start, end, null);
        Long errors = logapiRepository.sumErroresBetween(start, end, null);
        if (calls == null || calls == 0)
            return 100.0;
        return ((calls - (errors != null ? errors : 0)) * 100.0) / calls;
    }

    private double calculateErrorRate(LocalDateTime start, LocalDateTime end) {
        Long calls = logapiRepository.sumLlamadasBetween(start, end, null);
        Long errors = logapiRepository.sumErroresBetween(start, end, null);
        if (calls == null || calls == 0)
            return 0.0;
        return ((errors != null ? errors : 0) * 100.0) / calls;
    }

    @Transactional(readOnly = true)
    public double getThroughputChange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        double current = calculateThroughput(hourAgo, now);
        double previous = calculateThroughput(twoHoursAgo, hourAgo);

        return calculatePercentageChange(current, previous);
    }

    private double calculateThroughput(LocalDateTime start, LocalDateTime end) {
        Long calls = logapiRepository.sumLlamadasBetween(start, end, null);
        Long minutes = logapiRepository.countMinutesWithCallsBetween(start, end, null);
        if (minutes == null || minutes == 0)
            return 0.0;
        return (double) calls / minutes;
    }

    @Transactional(readOnly = true)
    public double getAvailabilityChange() {
        // Usamos SuccessRate como proxy de availability en este contexto simplificado
        return getSuccessRateChange();
    }

    /* ===== DTOs para Charts ===== */
    public record ChartSeriesDTO(List<String> labels, List<Double> data) {
    }

    public record ChartSeriesLongDTO(List<String> labels, List<Long> data) {
    }

    /* ===== Charts ===== */

    @Transactional(readOnly = true)
    public List<TopApiDTO> getLatencyByEndpointFiltered(List<Integer> apiIds) {
        try {
            List<Map<String, Object>> rows;
            if (apiIds != null && !apiIds.isEmpty()) {
                rows = logapiRepository.findAverageLatencyByEndpointFiltered(apiIds);
            } else {
                if (apiIds != null)
                    return Collections.emptyList();
                rows = logapiRepository.findAverageLatencyByEndpoint(); // Fallback to global
            }

            List<TopApiDTO> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String endpoint = (String) row.get("endpoint");
                // avg_latency might be BigDecimal or Double depending on DB
                Number avg = (Number) row.get("avg_latency");
                int latency = avg != null ? avg.intValue() : 0;

                // Reusing TopApiDTO: nombre=endpoint, others=0/dummy
                result.add(new TopApiDTO(endpoint, 0, latency, 0.0));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyBarsByApi(Integer idApi, LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> rows = logapiRepository.avgLatencyByApi(idApi, start, end);
            List<String> labels = new ArrayList<>();
            List<Double> data = new ArrayList<>();

            for (Object[] row : rows) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    labels.add(row[0].toString());
                    data.add(((Number) row[1]).doubleValue());
                }
            }
            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ChartSeriesDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getStatusDistribution(Integer idApi, LocalDateTime start,
            LocalDateTime end) {
        if (idApi != null) {
            return getStatusDistributionFiltered(List.of(idApi), start, end);
        }
        try {
            Object row = logapiRepository.successVsErrors(idApi, start, end);
            return processStatusDistributionRow(row);
        } catch (Exception e) {
            e.printStackTrace();
            return new ChartSeriesLongDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getStatusDistributionFiltered(List<Integer> apiIds, LocalDateTime start,
            LocalDateTime end) {
        try {
            if (start == null)
                start = LocalDateTime.now().minusHours(48);
            if (end == null)
                end = LocalDateTime.now();

            if (apiIds != null && apiIds.isEmpty()) {
                return new ChartSeriesLongDTO(List.of("Exito", "Error"), List.of(0L, 0L));
            }

            Object row = logapiRepository.successVsErrorsFiltered(apiIds, start, end);
            System.out.println(row.toString());
            return processStatusDistributionRow(row);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChartSeriesLongDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    private ChartSeriesLongDTO processStatusDistributionRow(Object row) {
        long exitos = 0;
        long errores = 0;

        if (row != null) {
            if (row instanceof Object[] data) {
                if (data[0] != null)
                    exitos = ((Number) data[0]).longValue();
                if (data[1] != null)
                    errores = ((Number) data[1]).longValue();
            }
        }
        return new ChartSeriesLongDTO(List.of("Exito", "Error"), List.of(exitos, errores));
    }

    /* ===== KPIs Avanzados ===== */

    @Transactional(readOnly = true)
    public double getCurrentThroughput() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        return Math.round(calculateThroughput(hourAgo, now) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public double getSystemAvailability() {
        return getSuccessRate();
    }

    /* ===== Tablas y Listas ===== */

    @Transactional(readOnly = true)
    public List<TopApiDTO> getTopUsedApis() {
        return getTopUsedApisFiltered(null);
    }

    @Transactional(readOnly = true)
    public List<TopApiDTO> getTopUsedApisFiltered(List<Integer> apiIds) {
        try {
            List<Object[]> rows;
            if (apiIds != null && !apiIds.isEmpty()) {
                rows = logapiRepository.getTopUsedApisFiltered(apiIds);
            } else {
                if (apiIds != null)
                    return Collections.emptyList(); // Empty list passed
                rows = logapiRepository.getTopUsedApis(); // Null passed, get all
            }

            List<TopApiDTO> result = new ArrayList<>();
            for (Object[] row : rows) {
                // nombre, total, avg_latency, error_rate
                String nombre = (String) row[0];
                long total = ((Number) row[1]).longValue();
                int avgLat = ((Number) row[2]).intValue();
                double errRate = ((Number) row[3]).doubleValue() * 100.0; // viene ratio
                result.add(new TopApiDTO(nombre, total, avgLat, errRate));
                if (result.size() >= 5)
                    break; // Limit to 5 java side
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getThroughputByHour() {
        return getThroughputByHourFiltered(null);
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getThroughputByHourFiltered(List<Integer> apiIds) {
        try {
            List<Object[]> rows;
            if (apiIds != null && !apiIds.isEmpty()) {
                rows = logapiRepository.getThroughputByHourFiltered(apiIds);
            } else {
                if (apiIds != null)
                    return new ChartSeriesLongDTO(Collections.emptyList(), Collections.emptyList());
                rows = logapiRepository.getThroughputByHour();
            }

            List<String> labels = new ArrayList<>();
            List<Long> data = new ArrayList<>();
            for (Object[] row : rows) {
                labels.add(row[0] + ":00");
                data.add(((Number) row[1]).longValue());
            }
            return new ChartSeriesLongDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesLongDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyTrend() {
        try {
            List<Object[]> rows = logapiRepository.getLatencyTrend();
            List<String> labels = new ArrayList<>();
            List<Double> data = new ArrayList<>();
            for (Object[] row : rows) {
                labels.add(row[0].toString());
                data.add(((Number) row[1]).doubleValue());
            }
            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyTrendFiltered(List<Integer> apiIds) {
        try {
            List<Object[]> rows;
            if (apiIds != null && !apiIds.isEmpty()) {
                rows = logapiRepository.getLatencyTrendFiltered(apiIds);
            } else {
                if (apiIds != null) {
                    return new ChartSeriesDTO(Collections.emptyList(), Collections.emptyList());
                }
                rows = logapiRepository.getLatencyTrend();
            }

            List<String> labels = new ArrayList<>();
            List<Double> data = new ArrayList<>();
            for (Object[] row : rows) {
                labels.add(row[0].toString());
                data.add(((Number) row[1]).doubleValue());
            }
            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ChartSeriesDTO(Collections.emptyList(), Collections.emptyList());
        }
    }

    @Transactional(readOnly = true)
    public List<EnvironmentUsageDTO> getUsageByEnvironment() {
        try {
            List<Object[]> rows = logapiRepository.getUsageByEnvironment();
            List<EnvironmentUsageDTO> result = new ArrayList<>();
            for (Object[] row : rows) {
                result.add(new EnvironmentUsageDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue()));
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<LogApiDTO> getRecentLogs() {
        try {
            List<com.example.telitodev.entity.LogApi> logs = logapiRepository.findTop20ByOrderByFechaDesc();
            return logs.stream().map(l -> new LogApiDTO(
                    l.getFecha(),
                    l.getMetodoHttp() != null ? l.getMetodoHttp() : "GET", // Default if null
                    l.getEndpoint(),
                    l.getEstadoHttp(),
                    l.getTiempoRespuestaMs())).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentAlerts() {
        return getRecentAlertsFiltered(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentAlertsFiltered(List<Integer> apiIds) {
        try {
            if (apiIds != null && !apiIds.isEmpty()) {
                return logapiRepository.findRecentAlertsFiltered(apiIds);
            } else if (apiIds != null) {
                return Collections.emptyList();
            }
            return logapiRepository.findRecentAlerts();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public record TopApiDTO(String nombre, long totalRequests, int avgLatency, double errorRate) {
    }

    public record EnvironmentUsageDTO(String nombre, long requests, double percentage) {
    }

    public record LogApiDTO(LocalDateTime timestamp, String method, String endpoint, int status, int latency) {
    }
}
