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




    /* ===== DTOs ===== */
    public record ChartSeriesDTO(List<String> labels, List<Double> data) {}
    public record ChartSeriesLongDTO(List<String> labels, List<Long> data) {}

    public record TopApiDTO(String nombre, long totalRequests, int avgLatency, double errorRate) {}
    public record CostApiDTO(String nombre, double totalCost, double avgCost, long requests) {}
    public record EnvironmentUsageDTO(String nombre, long requests, double percentage) {}

    //==============================================================================================
    // ✅ KPIs POR ORGANIZACIÓN (24h por defecto)
    //==============================================================================================

    @Transactional(readOnly = true)
    public long getTotalRequestsByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Long sum = metricaApiRepository.sumLlamadasBetweenByOrg(start, now, orgId, null, null);
        return sum != null ? sum : 0L;
    }

    @Transactional(readOnly = true)
    public double getAverageLatencyByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Double avg = metricaApiRepository.avgLatenciaBetweenByOrg(start, now, orgId, null, null);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public double getSuccessRateByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));

        Long total = metricaApiRepository.sumLlamadasBetweenByOrg(start, now, orgId, null, null);
        Long errors = metricaApiRepository.sumErroresBetweenByOrg(start, now, orgId, null, null);

        if (total == null || total == 0) return 100.0;
        long err = (errors != null) ? errors : 0L;
        long ok = Math.max(0, total - err);

        double rate = (double) ok / total * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getErrorRateByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));

        Long total = metricaApiRepository.sumLlamadasBetweenByOrg(start, now, orgId, null, null);
        Long errors = metricaApiRepository.sumErroresBetweenByOrg(start, now, orgId, null, null);

        if (total == null || total == 0) return 0.0;
        long err = (errors != null) ? errors : 0L;

        double rate = (double) err / total * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getCurrentThroughputByOrg(Integer orgId) {
        Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
        Double throughput = metricaApiRepository.getCurrentThroughputByOrg(orgId, since);
        return throughput != null ? Math.round(throughput * 100.0) / 100.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public double getSystemAvailabilityByOrg(Integer orgId) {
        Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(24));
        Double availability = metricaApiRepository.getSystemAvailabilityByOrg(orgId, since);
        return availability != null ? Math.round(availability * 100.0) / 100.0 : 100.0;
    }

    @Transactional(readOnly = true)
    public double getTotalCostByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        BigDecimal total = metricaApiRepository.sumCostoBetween(start, now, null, null); // si quieres por org, mejor agrega sumCostoBetweenByOrg
        // ✅ Recomendado: si ya agregaste sumCostoBetweenByOrg, usa ese:
        // BigDecimal total = metricaApiRepository.sumCostoBetweenByOrg(start, now, orgId, null, null);

        return total != null ? total.doubleValue() : 0.0;
    }

    //==============================================================================================
    // ✅ CAMBIOS PORCENTUALES (Día actual vs día anterior) POR ORG
    //==============================================================================================

    @Transactional(readOnly = true)
    public double getTotalRequestsChangeByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        long current = metricaApiRepository.sumLlamadasBetweenByOrg(yesterday, now, orgId, null, null);
        long previous = metricaApiRepository.sumLlamadasBetweenByOrg(dayBefore, yesterday, orgId, null, null);

        return calculatePercentageChange(current, previous);
    }

    @Transactional(readOnly = true)
    public double getAverageLatencyChangeByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Double current = metricaApiRepository.avgLatenciaBetweenByOrg(yesterday, now, orgId, null, null);
        Double previous = metricaApiRepository.avgLatenciaBetweenByOrg(dayBefore, yesterday, orgId, null, null);

        return calculatePercentageChange(current != null ? current : 0.0, previous != null ? previous : 0.0);
    }

    @Transactional(readOnly = true)
    public double getSuccessRateChangeByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        double current = calcSuccessRate(orgId, yesterday, now);
        double previous = calcSuccessRate(orgId, dayBefore, yesterday);

        double change = current - previous; // pp
        return Math.round(change * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public double getErrorRateChangeByOrg(Integer orgId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        double current = calcErrorRate(orgId, yesterday, now);
        double previous = calcErrorRate(orgId, dayBefore, yesterday);

        double change = current - previous; // pp
        return Math.round(change * 10.0) / 10.0;
    }

    private double calcSuccessRate(Integer orgId, Timestamp start, Timestamp end) {
        Long total = metricaApiRepository.sumLlamadasBetweenByOrg(start, end, orgId, null, null);
        Long errors = metricaApiRepository.sumErroresBetweenByOrg(start, end, orgId, null, null);
        if (total == null || total == 0) return 100.0;
        long err = (errors != null) ? errors : 0L;
        long ok = Math.max(0, total - err);
        return (double) ok / total * 100.0;
    }

    private double calcErrorRate(Integer orgId, Timestamp start, Timestamp end) {
        Long total = metricaApiRepository.sumLlamadasBetweenByOrg(start, end, orgId, null, null);
        Long errors = metricaApiRepository.sumErroresBetweenByOrg(start, end, orgId, null, null);
        if (total == null || total == 0) return 0.0;
        long err = (errors != null) ? errors : 0L;
        return (double) err / total * 100.0;
    }

    //==============================================================================================
    // ✅ CHARTS / TABLAS POR ORG
    //==============================================================================================

    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyBarsByApiByOrg(Integer orgId, Integer idApi, Integer idEntorno, Timestamp start, Timestamp end) {
        try {
            var rows = metricaApiRepository.avgLatencyByApiByOrg(orgId, idApi, idEntorno, start, end);

            var labels = new ArrayList<String>();
            var data   = new ArrayList<Double>();

            for (Object[] row : rows) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    labels.add(row[0].toString());
                    data.add(((Number) row[1]).doubleValue());
                }
            }
            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            System.err.println("❌ Error getLatencyBarsByApiByOrg: " + e.getMessage());
            return new ChartSeriesDTO(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getStatusDistributionByOrg(Integer orgId, Integer idApi, Integer idEntorno, Timestamp start, Timestamp end) {
        try {
            Object[] row = metricaApiRepository.successVsErrorsByOrg(orgId, idApi, idEntorno, start, end);

            long exitos = 0;
            long errores = 0;
            if (row != null && row.length >= 2) {
                if (row[0] != null) exitos = ((Number) row[0]).longValue();
                if (row[1] != null) errores = ((Number) row[1]).longValue();
            }

            return new ChartSeriesLongDTO(
                    List.of("Éxito (2xx/3xx)", "Errores (4xx/5xx)"),
                    List.of(exitos, errores)
            );
        } catch (Exception e) {
            System.err.println("❌ Error getStatusDistributionByOrg: " + e.getMessage());
            return new ChartSeriesLongDTO(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Transactional(readOnly = true)
    public List<TopApiDTO> getTopUsedApisByOrg(Integer orgId) {
        try {
            var rows = metricaApiRepository.getTopUsedApisByOrg(orgId);
            return rows.stream().map(row -> new TopApiDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).intValue(),
                    ((Number) row[3]).doubleValue()
            )).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<CostApiDTO> getCostMetricsByApiByOrg(Integer orgId) {
        try {
            var rows = metricaApiRepository.getCostMetricsByApiByOrg(orgId);
            return rows.stream().map(row -> new CostApiDTO(
                    (String) row[0],
                    ((Number) row[1]).doubleValue(),
                    ((Number) row[2]).doubleValue(),
                    ((Number) row[3]).longValue()
            )).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesLongDTO getThroughputByHourByOrg(Integer orgId) {
        try {
            Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusHours(24));
            var rows = metricaApiRepository.getThroughputByHourByOrg(orgId, since);

            var labels = rows.stream()
                    .map(r -> String.format("%02d:00", ((Number) r[0]).intValue()))
                    .toList();
            var data = rows.stream()
                    .map(r -> ((Number) r[1]).longValue())
                    .toList();

            return new ChartSeriesLongDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesLongDTO(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Transactional(readOnly = true)
    public ChartSeriesDTO getLatencyTrendByOrg(Integer orgId) {
        try {
            Timestamp since = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
            var rows = metricaApiRepository.getLatencyTrendByOrg(orgId, since);

            var labels = rows.stream().map(r -> r[0].toString()).toList();
            var data = rows.stream().map(r -> ((Number) r[1]).doubleValue()).toList();

            return new ChartSeriesDTO(labels, data);
        } catch (Exception e) {
            return new ChartSeriesDTO(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Transactional(readOnly = true)
    public List<EnvironmentUsageDTO> getUsageByEnvironmentByOrg(Integer orgId) {
        try {
            var rows = metricaApiRepository.getUsageByEnvironmentByOrgRaw(orgId);

            long total = 0L;
            for (Object[] r : rows) total += ((Number) r[1]).longValue();

            var result = new ArrayList<EnvironmentUsageDTO>();
            for (Object[] r : rows) {
                String nombre = (String) r[0];
                long requests = ((Number) r[1]).longValue();
                double pct = (total == 0) ? 0.0 : (requests * 100.0 / total);
                pct = Math.round(pct * 10.0) / 10.0;
                result.add(new EnvironmentUsageDTO(nombre, requests, pct));
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    //==============================================================================================
    // ✅ Si todavía usas estas funciones “globales”, las dejamos (compatibilidad)
    // (pero para KPIs del PO, usa las ByOrg)
    //==============================================================================================

    @Transactional(readOnly = true)
    public List<MetricaApi> search(Integer idApi, Integer idEntorno, Timestamp start, Timestamp end) {
        return metricaApiRepository.search(idApi, idEntorno, start, end);
    }

    @Transactional(readOnly = true)
    public List<MetricaApi> getAllMetrics() {
        return metricaApiRepository.findAll();
    }

    @Transactional(readOnly = true)
    public double getThroughputChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp hourAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
        Timestamp twoHoursAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(hourAgo, now, null, null);
        Long currentMinutes = metricaApiRepository.countMinutesWithCallsBetween(hourAgo, now, null, null);
        double currentThroughput = (currentMinutes == null || currentMinutes == 0) ? 0.0 : (double) currentCalls / currentMinutes;

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(twoHoursAgo, hourAgo, null, null);
        Long previousMinutes = metricaApiRepository.countMinutesWithCallsBetween(twoHoursAgo, hourAgo, null, null);
        double previousThroughput = (previousMinutes == null || previousMinutes == 0) ? 0.0 : (double) previousCalls / previousMinutes;

        return calculatePercentageChange(currentThroughput, previousThroughput);
    }

    @Transactional(readOnly = true)
    public double getAvailabilityChange() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp yesterday = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp dayBefore = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

        Long currentCalls = metricaApiRepository.sumLlamadasBetween(yesterday, now, null, null);
        Long currentErrors = metricaApiRepository.sumErroresBetween(yesterday, now, null, null);
        double currentAvailability = (currentCalls == null || currentCalls == 0) ? 100.0 :
                Math.max(0.0, (double) (currentCalls - (currentErrors == null ? 0 : currentErrors)) / currentCalls * 100.0);

        Long previousCalls = metricaApiRepository.sumLlamadasBetween(dayBefore, yesterday, null, null);
        Long previousErrors = metricaApiRepository.sumErroresBetween(dayBefore, yesterday, null, null);
        double previousAvailability = (previousCalls == null || previousCalls == 0) ? 100.0 :
                Math.max(0.0, (double) (previousCalls - (previousErrors == null ? 0 : previousErrors)) / previousCalls * 100.0);

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


}
