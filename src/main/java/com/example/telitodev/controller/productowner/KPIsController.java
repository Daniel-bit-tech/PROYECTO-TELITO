package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.EntornoRepository;
import com.example.telitodev.repository.ProyectoHasApiRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.MetricsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class KPIsController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final MetricsService metricsService;
    private final ApiRepository apiRepository;
    private final EntornoRepository entornoRepository;
    private final ProyectoHasApiRepository proyectoHasApiRepository;

    public KPIsController(UsuarioRepository usuarioRepository,
            MetricsService metricsService,
            ApiRepository apiRepository,
            EntornoRepository entornoRepository,
            ProyectoHasApiRepository proyectoHasApiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.metricsService = metricsService;
        this.apiRepository = apiRepository;
        this.entornoRepository = entornoRepository;
        this.proyectoHasApiRepository = proyectoHasApiRepository;
    }

    /** Vista principal de KPIs (inyecta métricas de cabecera + lista básica) */
    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);

        Integer idOrganizacion = usuario.getOrganizacion().getIdOrganizacion();
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // --- KPIs BÁSICOS ---
        long totalLlamadas = metricsService.getTotalRequests();
        model.addAttribute("totalLlamadas", totalLlamadas);
        double latenciaPromedio = metricsService.getAverageLatency();
        model.addAttribute("latenciaPromedio", Math.round(latenciaPromedio));
        double tasaExito = metricsService.getSuccessRate();
        model.addAttribute("tasaExito", String.format("%.1f", tasaExito));
        double tasaError = metricsService.getErrorRate();
        model.addAttribute("tasaError", String.format("%.1f", tasaError));

        // --- KPIs AVANZADOS ---
        double throughput = metricsService.getCurrentThroughput();
        model.addAttribute("throughput", String.format("%.2f", throughput));
        double disponibilidad = metricsService.getSystemAvailability();
        model.addAttribute("disponibilidad", String.format("%.2f", disponibilidad));
        // Costo eliminado

        // --- CAMBIOS PORCENTUALES (Pasar como Double) ---
        model.addAttribute("totalLlamadasChange", metricsService.getTotalRequestsChange());
        model.addAttribute("latenciaPromedioChange", metricsService.getAverageLatencyChange());
        model.addAttribute("tasaExitoChange", metricsService.getSuccessRateChange());
        model.addAttribute("tasaErrorChange", metricsService.getErrorRateChange());
        model.addAttribute("throughputChange", metricsService.getThroughputChange());
        model.addAttribute("disponibilidadChange", metricsService.getAvailabilityChange());
        // Costo cambio eliminado

        // --- DATOS PARA TABLAS Y GRÁFICOS ---
        List<Api> apisDeLaOrganizacion = Collections.emptyList();
        try {
            apisDeLaOrganizacion = apiRepository.findByOrganizacionId(idOrganizacion);
        } catch (Exception e) {
            System.err.println("Error al obtener APIs de la organización: " + e.getMessage());
        }

        model.addAttribute("totalApisActivas", apisDeLaOrganizacion.size());
        model.addAttribute("totalApisActivasChange", "+0");

        final List<Api> finalApisDeOrg = apisDeLaOrganizacion;
        List<MetricsService.TopApiDTO> topApis = metricsService.getTopUsedApis().stream()
                .filter(apiDto -> finalApisDeOrg.stream().anyMatch(api -> api.getNombre().equals(apiDto.nombre())))
                .collect(Collectors.toList());
        model.addAttribute("topApis", topApis);

        // Costos Apis eliminado

        model.addAttribute("usoEntornos", metricsService.getUsageByEnvironment());

        // --- DATOS PARA FILTROS DINÁMICOS ---
        model.addAttribute("listaApis", apisDeLaOrganizacion);

        // --- DATOS PARA TABLA RENDIMIENTO ---
        model.addAttribute("rendimientoApis", topApis);

        return "po/KPIs";
    }

    /**
     * Serie para Chart.js (barras): Latencia promedio por API (filtros opcionales)
     */
    @GetMapping("/KPIs/chart")
    @ResponseBody
    public MetricsService.ChartSeriesDTO chartByApi(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        var startD = (startDate != null) ? startDate : java.time.LocalDate.now().minusDays(30);
        var endD = (endDate != null) ? endDate : java.time.LocalDate.now();

        var start = startD.atStartOfDay();
        var end = endD.atTime(23, 59, 59);

        System.out.println("=== SOLICITUD GRÁFICO ===");
        System.out.println("API: " + idApi + ", Entorno: " + idEntorno);
        System.out.println("Fechas: " + startD + " a " + endD);

        MetricsService.ChartSeriesDTO result = metricsService.getLatencyBarsByApi(idApi, start, end);

        System.out.println("=== RESULTADO GRÁFICO ===");
        System.out.println("Labels: " + result.labels());
        System.out.println("Data: " + result.data());

        return metricsService.getLatencyBarsByApi(idApi, start, end);
    }

    /** Serie para Chart.js (donut): Éxito vs Errores (filtros opcionales) */
    @GetMapping("/KPIs/status-distribution")
    @ResponseBody
    public MetricsService.ChartSeriesLongDTO statusDistribution(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        var start = (startDate != null ? startDate : java.time.LocalDate.now().minusDays(30)).atStartOfDay();
        var end = (endDate != null ? endDate : java.time.LocalDate.now()).atTime(23, 59, 59);

        return metricsService.getStatusDistribution(idApi, start, end);
    }

    /* ===== NUEVOS ENDPOINTS PARA KPIs AVANZADOS ===== */

    /** Throughput por hora (últimas 24h) */
    @GetMapping("/KPIs/throughput-hour")
    @ResponseBody
    public MetricsService.ChartSeriesLongDTO getThroughputByHour(@RequestParam(required = false) Integer idApi) {
        if (idApi != null) {
            return metricsService.getThroughputByHourFiltered(java.util.Collections.singletonList(idApi));
        }
        return metricsService.getThroughputByHour();
    }

    /** Tendencia de latencia (últimos 7 días) */
    @GetMapping("/KPIs/latency-trend")
    @ResponseBody
    public MetricsService.ChartSeriesDTO getLatencyTrend(@RequestParam(required = false) Integer idApi) {
        if (idApi != null) {
            return metricsService.getLatencyTrendFiltered(java.util.Collections.singletonList(idApi));
        }
        return metricsService.getLatencyTrend();
    }

    /** APIs más utilizadas */
    @GetMapping("/KPIs/top-apis")
    @ResponseBody
    public java.util.List<MetricsService.TopApiDTO> getTopApis() {
        return metricsService.getTopUsedApis();
    }

    // Cost endpoint eliminado

    /** Distribución de uso por entorno */
    @GetMapping("/KPIs/environment-usage")
    @ResponseBody
    public java.util.List<MetricsService.EnvironmentUsageDTO> getEnvironmentUsage() {
        return metricsService.getUsageByEnvironment();
    }

    /**
     * Endpoint REST para obtener KPIs de negocio de una API específica
     * Para Product Owner: métricas de negocio resumidas
     */
    @GetMapping("/api/kpis/{idApi}")
    @ResponseBody
    public java.util.Map<String, Object> getKpisPorApi(@PathVariable Integer idApi) {
        java.util.Map<String, Object> kpis = new java.util.HashMap<>();

        try {
            // Buscar API
            Api api = apiRepository.findById(idApi).orElse(null);
            if (api == null) {
                kpis.put("success", false);
                kpis.put("error", "API no encontrada");
                return kpis;
            }

            kpis.put("apiId", idApi);
            kpis.put("apiNombre", api.getNombre());

            // Usar MetricsService para obtener métricas filtradas por API
            // Nota: Estos métodos calculan desde LogApi
            long totalLlamadas = metricsService.getTotalRequests(idApi);
            double latenciaPromedio = metricsService.getAverageLatency(idApi);
            double tasaExito = metricsService.getSuccessRate(idApi);
            double disponibilidad = metricsService.getSystemAvailability(idApi);

            kpis.put("totalLlamadas", totalLlamadas);
            kpis.put("latenciaPromedio", Math.round(latenciaPromedio));
            kpis.put("tasaExito", String.format("%.1f", tasaExito));
            kpis.put("disponibilidad", String.format("%.2f", disponibilidad));

            // Costos eliminados

            kpis.put("success", true);

        } catch (Exception e) {
            kpis.put("success", false);
            kpis.put("error", e.getMessage());
        }

        return kpis;
    }

}
