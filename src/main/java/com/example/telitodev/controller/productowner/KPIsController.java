package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.MetricsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class KPIsController {

    private final UsuarioRepository usuarioRepository;
    private final MetricsService metricsService;

    public KPIsController(UsuarioRepository usuarioRepository, MetricsService metricsService) {
        this.usuarioRepository = usuarioRepository;
        this.metricsService = metricsService;
    }

    /** Vista principal de KPIs (inyecta métricas de cabecera + lista básica) */
    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        // KPIs (puedes cambiar a las versiones *Fast()* si prefieres agregaciones directas en DB)
        model.addAttribute("totalLlamadas",    metricsService.getTotalRequests());
        model.addAttribute("latenciaPromedio", metricsService.getAverageLatency());
        model.addAttribute("tasaExito", String.format("%.1f", metricsService.getSuccessRate()));
        model.addAttribute("tasaError", String.format("%.1f", metricsService.getErrorRate()));

        // Para tablas u otros listados (opcional)
        model.addAttribute("metricas", metricsService.getAllMetrics());

        return "po/KPIs";
    }

    /** Serie para Chart.js (barras): Latencia promedio por API (filtros opcionales) */
    @GetMapping("/KPIs/chart")
    @ResponseBody
    public MetricsService.ChartSeriesDTO chartByApi(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate startDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate endDate
    ) {
        var startD = (startDate != null) ? startDate : java.time.LocalDate.now().minusDays(30);
        var endD   = (endDate   != null) ? endDate   : java.time.LocalDate.now();

        var start = java.sql.Timestamp.valueOf(startD.atStartOfDay());
        var end   = java.sql.Timestamp.valueOf(endD.atTime(23, 59, 59));

        return metricsService.getLatencyBarsByApi(idApi, idEntorno, start, end);
    }

    /** Serie para Chart.js (donut): Éxito vs Errores (filtros opcionales) */
    @GetMapping("/KPIs/status-distribution")
    @ResponseBody
    public MetricsService.ChartSeriesLongDTO statusDistribution(
            @RequestParam(required = false) Integer idApi,
            @RequestParam(required = false) Integer idEntorno,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate startDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate endDate
    ) {
        var start = java.sql.Timestamp.valueOf(
                (startDate != null ? startDate : java.time.LocalDate.now().minusDays(30)).atStartOfDay());
        var end   = java.sql.Timestamp.valueOf(
                (endDate   != null ? endDate   : java.time.LocalDate.now()).atTime(23, 59, 59));

        return metricsService.getStatusDistribution(idApi, idEntorno, start, end);
    }

    /** Endpoint simple para verificación rápida */
    @GetMapping("/KPIs/ping")
    @ResponseBody
    public String ping() {
        return "ok";
    }
}
