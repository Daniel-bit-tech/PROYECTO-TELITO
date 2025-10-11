package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.LogapiRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/dev")
public class MetricasController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final LogapiRepository logapiRepository;

    @Autowired
    public MetricasController(UsuarioRepository usuarioRepository, LogapiRepository logapiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.logapiRepository = logapiRepository;
    }

    @GetMapping("/metricas")
    public String showmetricas(Model model, Authentication auth, HttpSession session) {

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);

        // 1. Datos para las tarjetas de métricas
        long totalRequests = logapiRepository.count();
        Optional<Double> avgLatencyOpt = logapiRepository.findAverageLatency();
        Optional<Double> successRateOpt = logapiRepository.calculateSuccessRate();
        Optional<Double> errorRateOpt = logapiRepository.calculateErrorRate();

        DecimalFormat df = new DecimalFormat("#.##");
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("avgLatency", avgLatencyOpt.map(latency -> df.format(latency)).orElse("0"));
        model.addAttribute("successRate", successRateOpt.map(rate -> df.format(rate)).orElse("100"));
        model.addAttribute("errorRate", errorRateOpt.map(rate -> df.format(rate)).orElse("0"));

        // 2. Datos para los gráficos
        List<Map<String, Object>> requestsPerHour = logapiRepository.countRequestsGroupedByHour();
        List<Map<String, Object>> statusCounts = logapiRepository.countRequestsGroupedByStatusCode();
        List<Map<String, Object>> latencyByEndpoint = logapiRepository.findAverageLatencyByEndpoint();

        model.addAttribute("requestsPerHour", requestsPerHour);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("latencyByEndpoint", latencyByEndpoint);

        // 3. NUEVO: Datos para Alertas y Rendimiento por API
        List<Map<String, Object>> recentAlerts = logapiRepository.findRecentAlerts();
        List<Map<String, Object>> apiPerformanceList = logapiRepository.findApiPerformanceMetrics();

        model.addAttribute("alerts", recentAlerts);
        model.addAttribute("apiPerformanceList", apiPerformanceList);

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/metricas";
    }
}
