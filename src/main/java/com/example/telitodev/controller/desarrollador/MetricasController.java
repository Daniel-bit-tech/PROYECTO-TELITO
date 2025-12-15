package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.LogapiRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasAnyRole('DEV', 'DEVELOPER', 'SADMIN')")
public class MetricasController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final LogapiRepository logapiRepository;
    private final com.example.telitodev.service.MetricsService metricsService;
    private final com.example.telitodev.repository.ApiRepository apiRepository;

    @Autowired
    public MetricasController(UsuarioRepository usuarioRepository, LogapiRepository logapiRepository,
            com.example.telitodev.service.MetricsService metricsService,
            com.example.telitodev.repository.ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.logapiRepository = logapiRepository;
        this.metricsService = metricsService;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/metricas")
    public String showmetricas(Model model, Authentication auth, HttpSession session) {

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);

        // Obtener IDs de APIs de la organización del usuario
        Integer orgId = null;
        if (usuario.getOrganizacion() != null) {
            orgId = usuario.getOrganizacion().getIdOrganizacion();
            model.addAttribute("listaApis", apiRepository.findByOrganizacionId(orgId));
        } else if (usuario.getEquipo() != null && usuario.getEquipo().getOrganizacion() != null) {
            orgId = usuario.getEquipo().getOrganizacion().getIdOrganizacion();
        }

        List<Integer> apiIds = null;
        if (orgId != null) {
            List<com.example.telitodev.entity.Api> userApis = apiRepository.findByOrganizacionId(orgId);
            if (userApis != null && !userApis.isEmpty()) {
                apiIds = userApis.stream().map(com.example.telitodev.entity.Api::getIdApi).toList();
            } else {
                apiIds = List.of(); // Empty list ensures we filter to nothing if org has no APIs
            }
        }

        // 1. Datos para las tarjetas de métricas

        model.addAttribute("totalRequests", metricsService.getTotalRequests());
        model.addAttribute("avgLatency", Math.round(metricsService.getAverageLatency()));
        model.addAttribute("successRate", metricsService.getSuccessRate());
        model.addAttribute("errorRate", metricsService.getErrorRate());

        // 2. Datos para los gráficos (Usando DTOs del servicio)
        model.addAttribute("requestsPerHour", metricsService.getThroughputByHour());

        // Filtered Status Distribution
        model.addAttribute("statusCounts", metricsService.getStatusDistributionFiltered(apiIds, null, null));

        // Filtered Latency by Endpoint
        model.addAttribute("latencyByEndpoint", metricsService.getLatencyByEndpointFiltered(apiIds));

        // 3. NUEVO: Datos para Alertas y Rendimiento por API
        List<Map<String, Object>> recentAlerts = logapiRepository.findRecentAlerts();
        // Filtered Performance List (reuses TopUsedApis)
        model.addAttribute("apiPerformanceList", metricsService.getTopUsedApisFiltered(apiIds));

        model.addAttribute("alerts", recentAlerts);

        // 4. NUEVO: Logs Recientes
        model.addAttribute("recentLogs", metricsService.getRecentLogs());

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/metricas";
    }

    /**
     * Endpoint REST para obtener métricas detalladas de una API específica
     * Para desarrolladores: métricas técnicas con desglose por método HTTP
     */
    @GetMapping("/api/metricas/{idApi}")
    @ResponseBody
    public Map<String, Object> getMetricasPorApi(@PathVariable Integer idApi) {
        Map<String, Object> metricas = new java.util.HashMap<>();

        try {
            // Métricas básicas
            Long totalLlamadas = logapiRepository.countByApi_IdApi(idApi);
            Long totalErrores = logapiRepository.countErroresByApi(idApi);
            Double latenciaPromedio = logapiRepository.avgLatenciaByApi(idApi);
            Long usuariosUnicos = logapiRepository.countDistinctUsuariosByApi(idApi);

            metricas.put("totalLlamadas", totalLlamadas != null ? totalLlamadas : 0);
            metricas.put("totalErrores", totalErrores != null ? totalErrores : 0);
            metricas.put("latenciaPromedio", latenciaPromedio != null ? Math.round(latenciaPromedio) : 0);
            metricas.put("usuariosUnicos", usuariosUnicos != null ? usuariosUnicos : 0);

            // Calcular tasa de éxito
            if (totalLlamadas != null && totalLlamadas > 0) {
                double tasaExito = ((totalLlamadas - (totalErrores != null ? totalErrores : 0)) * 100.0)
                        / totalLlamadas;
                metricas.put("tasaExito", Math.round(tasaExito * 100.0) / 100.0);
            } else {
                metricas.put("tasaExito", 100.0);
            }

            // Desglose por método HTTP
            Map<String, Long> metodoHttp = new java.util.HashMap<>();
            metodoHttp.put("GET", logapiRepository.countByApiAndMetodo(idApi, "GET"));
            metodoHttp.put("POST", logapiRepository.countByApiAndMetodo(idApi, "POST"));
            metodoHttp.put("PUT", logapiRepository.countByApiAndMetodo(idApi, "PUT"));
            metodoHttp.put("DELETE", logapiRepository.countByApiAndMetodo(idApi, "DELETE"));
            metricas.put("porMetodoHttp", metodoHttp);

            metricas.put("success", true);

        } catch (Exception e) {
            metricas.put("success", false);
            metricas.put("error", e.getMessage());
        }

        return metricas;
    }

    @GetMapping("/api/charts-data")
    @ResponseBody
    public Map<String, Object> getChartsData(@RequestParam(required = false) Integer apiId,
            Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        Integer orgId = null;
        if (usuario.getOrganizacion() != null) {
            orgId = usuario.getOrganizacion().getIdOrganizacion();
        } else if (usuario.getEquipo() != null && usuario.getEquipo().getOrganizacion() != null) {
            orgId = usuario.getEquipo().getOrganizacion().getIdOrganizacion();
        }

        List<Integer> apiIds = new java.util.ArrayList<>();
        if (apiId != null && orgId != null) {
            // Verify API belongs to org
            Optional<Api> apiOpt = apiRepository.findById(apiId);
            if (apiOpt.isPresent() && apiOpt.get().getEquipo().getOrganizacion().getIdOrganizacion().equals(orgId)) {
                apiIds.add(apiId);
            } else {
                return new java.util.HashMap<>();
            }
        } else if (orgId != null) {
            // Get all Org APIs
            List<Api> userApis = apiRepository.findByOrganizacionId(orgId);
            if (userApis != null) {
                apiIds = userApis.stream().map(Api::getIdApi).toList();
            }
        }

        Map<String, Object> data = new java.util.HashMap<>();

        // 1. Requests per Hour
        data.put("requestsPerHour", metricsService.getThroughputByHourFiltered(apiIds));

        // 2. Status Distribution
        data.put("statusCounts", metricsService.getStatusDistributionFiltered(apiIds, null, null));

        // 3. Latency
        data.put("latencyByEndpoint", metricsService.getLatencyByEndpointFiltered(apiIds));

        // 4. Alerts
        data.put("alerts", metricsService.getRecentAlertsFiltered(apiIds));

        return data;
    }
}
