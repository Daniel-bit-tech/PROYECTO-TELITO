package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.EntornoRepository;
import com.example.telitodev.repository.ProyectoHasApiRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.MetricsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    /** Vista principal de KPIs */
    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();

        // Banner de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // ===== KPIs (POR ORGANIZACIÓN) =====
        long totalLlamadas = metricsService.getTotalRequestsByOrg(orgId);
        model.addAttribute("totalLlamadas", totalLlamadas);

        double latenciaPromedio = metricsService.getAverageLatencyByOrg(orgId);
        model.addAttribute("latenciaPromedio", Math.round(latenciaPromedio));

        double tasaExito = metricsService.getSuccessRateByOrg(orgId);
        model.addAttribute("tasaExito", String.format("%.1f", tasaExito));

        double tasaError = metricsService.getErrorRateByOrg(orgId);
        model.addAttribute("tasaError", String.format("%.1f", tasaError));

        // ===== KPIs AVANZADOS (POR ORGANIZACIÓN) =====
        double throughput = metricsService.getCurrentThroughputByOrg(orgId);
        model.addAttribute("throughput", String.format("%.2f", throughput));

        double disponibilidad = metricsService.getSystemAvailabilityByOrg(orgId);
        model.addAttribute("disponibilidad", String.format("%.2f", disponibilidad));

        double costoTotal = metricsService.getTotalCostByOrg(orgId);
        model.addAttribute("costoTotal", String.format("%.2f", costoTotal));

        // ===== CAMBIOS (POR ORGANIZACIÓN) =====
        model.addAttribute("totalLlamadasChange", metricsService.getTotalRequestsChangeByOrg(orgId));
        model.addAttribute("latenciaPromedioChange", metricsService.getAverageLatencyChangeByOrg(orgId));
        model.addAttribute("tasaExitoChange", metricsService.getSuccessRateChangeByOrg(orgId));
        model.addAttribute("tasaErrorChange", metricsService.getErrorRateChangeByOrg(orgId));
        model.addAttribute("throughputChange", metricsService.getThroughputChange());
        model.addAttribute("disponibilidadChange", metricsService.getAvailabilityChange());
        model.addAttribute("costoTotalChange", metricsService.getTotalCostChange());

        // ===== LISTA DE APIs (Organización) =====
        List<Api> apisDeLaOrganizacion = Collections.emptyList();
        try {
            apisDeLaOrganizacion = proyectoHasApiRepository.findDistinctApisByOrganizacionId(orgId);
        } catch (Exception e) {
            System.err.println("Error al obtener APIs de la organización: " + e.getMessage());
        }

        model.addAttribute("totalApisActivas", apisDeLaOrganizacion.size());
        model.addAttribute("totalApisActivasChange", "+0");

        // ===== TABLAS (POR ORGANIZACIÓN) =====
        List<MetricsService.TopApiDTO> topApis = metricsService.getTopUsedApisByOrg(orgId);
        model.addAttribute("topApis", topApis);

        List<MetricsService.CostApiDTO> costosApis = metricsService.getCostMetricsByApiByOrg(orgId);
        model.addAttribute("costosApis", costosApis);

        model.addAttribute("usoEntornos", metricsService.getUsageByEnvironmentByOrg(orgId));

        // ===== FILTROS (selects) =====
        model.addAttribute("listaApis", apisDeLaOrganizacion);
        model.addAttribute("listaEntornos", entornoRepository.findAll());

        // ===== ALERTAS (demo) =====
        List<AlertaDTO> alertas = List.of(
                new AlertaDTO("error", "Aumento de errores 5xx en API de Pagos", "Tasa de error subió del 0.5% al 3.2%", "Hace 15 min"),
                new AlertaDTO("warning", "Aumento de latencia en API de Usuarios", "p95 pasó de 200ms a 450ms", "Hace 2 h"),
                new AlertaDTO("info", "Patrón de tráfico inusual", "+40% requests a API de Productos", "Ayer 16:32")
        );
        model.addAttribute("alertasRecientes", alertas);

        // ===== TABLA RENDIMIENTO (reusa topApis) =====
        model.addAttribute("rendimientoApis", topApis);

        return "po/KPIs";
    }

    public record AlertaDTO(String tipo, String titulo, String descripcion, String tiempo) {}

    /** Barras: Latencia promedio por API (filtros opcionales) - POR ORG */
    @GetMapping("/KPIs/chart")
    @ResponseBody
    public MetricsService.ChartSeriesDTO chartByApi(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Authentication auth,
            HttpSession session
    ) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();

        LocalDate startD = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate endD   = (endDate   != null) ? endDate   : LocalDate.now();

        Timestamp start = Timestamp.valueOf(startD.atStartOfDay());
        Timestamp end   = Timestamp.valueOf(endD.atTime(23, 59, 59));

        return metricsService.getLatencyBarsByApiByOrg(orgId, idApi, idEntorno, start, end);
    }

    /** Donut: Éxito vs Errores (filtros opcionales) - POR ORG */
    @GetMapping("/KPIs/status-distribution")
    @ResponseBody
    public MetricsService.ChartSeriesLongDTO statusDistribution(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Authentication auth,
            HttpSession session
    ) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();

        LocalDate startD = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate endD   = (endDate   != null) ? endDate   : LocalDate.now();

        Timestamp start = Timestamp.valueOf(startD.atStartOfDay());
        Timestamp end   = Timestamp.valueOf(endD.atTime(23, 59, 59));

        return metricsService.getStatusDistributionByOrg(orgId, idApi, idEntorno, start, end);
    }

    /** Endpoint simple para verificación rápida */
    @GetMapping("/KPIs/ping")
    @ResponseBody
    public String ping() {
        return "ok";
    }

    /* ===== KPIs AVANZADOS (POR ORG) ===== */

    /** Throughput por hora (últimas 24h) - POR ORG */
    @GetMapping("/KPIs/throughput-hour")
    @ResponseBody
    public MetricsService.ChartSeriesLongDTO getThroughputByHour(Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
        return metricsService.getThroughputByHourByOrg(orgId);
    }

    /** Tendencia de latencia (últimos 7 días) - POR ORG */
    @GetMapping("/KPIs/latency-trend")
    @ResponseBody
    public MetricsService.ChartSeriesDTO getLatencyTrend(Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
        return metricsService.getLatencyTrendByOrg(orgId);
    }

    /** APIs más utilizadas - POR ORG */
    @GetMapping("/KPIs/top-apis")
    @ResponseBody
    public List<MetricsService.TopApiDTO> getTopApis(Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
        return metricsService.getTopUsedApisByOrg(orgId);
    }

    /** Métricas de costo por API - POR ORG */
    @GetMapping("/KPIs/cost-metrics")
    @ResponseBody
    public List<MetricsService.CostApiDTO> getCostMetrics(Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
        return metricsService.getCostMetricsByApiByOrg(orgId);
    }

    /** Distribución de uso por entorno - POR ORG */
    @GetMapping("/KPIs/environment-usage")
    @ResponseBody
    public List<MetricsService.EnvironmentUsageDTO> getEnvironmentUsage(Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
        return metricsService.getUsageByEnvironmentByOrg(orgId);
    }


}
