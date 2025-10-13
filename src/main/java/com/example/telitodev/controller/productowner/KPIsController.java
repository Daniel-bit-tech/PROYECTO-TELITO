package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.MetricsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class KPIsController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final MetricsService metricsService;

    public KPIsController(UsuarioRepository usuarioRepository, MetricsService metricsService) {
        this.usuarioRepository = usuarioRepository;
        this.metricsService = metricsService;
    }

    /** Vista principal de KPIs (inyecta métricas de cabecera + lista básica) */
    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // KPIs (puedes cambiar a las versiones *Fast()* si prefieres agregaciones directas en DB)
        long totalLlamadas = metricsService.getTotalRequests();
        double latenciaPromedio = metricsService.getAverageLatency();
        double tasaExito = metricsService.getSuccessRate();
        double tasaError = metricsService.getErrorRate();

        System.out.println("=== KPIs CARGADOS ===");
        System.out.println("Total Requests: " + totalLlamadas);
        System.out.println("Latencia Promedio: " + latenciaPromedio);
        System.out.println("Tasa Éxito: " + tasaExito);
        System.out.println("Tasa Error: " + tasaError);

        model.addAttribute("totalLlamadas", totalLlamadas);
        model.addAttribute("latenciaPromedio", Math.round(latenciaPromedio));
        model.addAttribute("tasaExito", String.format("%.1f", tasaExito));
        model.addAttribute("tasaError", String.format("%.1f", tasaError));

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

        System.out.println("=== SOLICITUD GRÁFICO ===");
        System.out.println("API: " + idApi + ", Entorno: " + idEntorno);
        System.out.println("Fechas: " + startD + " a " + endD);

        MetricsService.ChartSeriesDTO result = metricsService.getLatencyBarsByApi(idApi, idEntorno, start, end);

        System.out.println("=== RESULTADO GRÁFICO ===");
        System.out.println("Labels: " + result.labels());
        System.out.println("Data: " + result.data());

        return result;
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
