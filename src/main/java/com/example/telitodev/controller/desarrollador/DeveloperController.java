package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.CredencialApi;
import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasAnyRole('DEV', 'DEVELOPER', 'SUPERADMIN')")
public class DeveloperController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    final TicketRepository ticketRepository;
    final LogapiRepository logapiRepository;

    public DeveloperController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository, TicketRepository ticketRepository, LogapiRepository logapiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
        this.ticketRepository = ticketRepository;
        this.logapiRepository = logapiRepository;
    }


    @GetMapping("/home")
    public String showDeveloperView(Model model, Authentication auth, HttpSession session) {
        System.out.println("=== DEVELOPER CONTROLLER HOME ===");
        System.out.println("Usuario: " + auth.getName());
        System.out.println("Autoridades: " + auth.getAuthorities());

        try {
            Usuario usuario = getCurrentUser(auth, session);
            System.out.println("Usuario obtenido: " + usuario.getCorreo());

            Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
            List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());

            List<Notificacion> notis = notificacionRepository.findTop5ByUsuarioDniAndLeidoOrderByFechaDesc(usuario.getDni(), false);
            Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);

            List<Ticket> tickets = ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());

            long requestsToday = logapiRepository.countRequestsToday();
            long errors24h = logapiRepository.countErrorsLast24Hours();
            Optional<Double> successRateOpt = logapiRepository.calculateSuccessRateLast24Hours();
            Optional<Double> avgLatencyOpt = logapiRepository.findAverageLatencyLast24Hours();

            DecimalFormat df = new DecimalFormat("#.##");
            String formattedSuccessRate = successRateOpt.map(rate -> df.format(rate)).orElse("100");
            String formattedAvgLatency = avgLatencyOpt.map(latency -> df.format(latency)).orElse("0");

            model.addAttribute("requestsToday", requestsToday);
            model.addAttribute("successRate", formattedSuccessRate);
            model.addAttribute("avgLatency", formattedAvgLatency);
            model.addAttribute("errors24h", errors24h);

            model.addAttribute("usuario", usuario);
            model.addAttribute("NcredActivas", NCredenciales);
            model.addAttribute("credenciales", credenciales);
            model.addAttribute("Nnotis", Nnotis);
            model.addAttribute("notificaciones", notis);
            model.addAttribute("tickets", tickets);

            // Agregar información de impersonación al modelo usando BaseController
            addImpersonationAttributes(model, session);

            System.out.println(" Devolviendo vista: desarrollador/developer");
            return "desarrollador/developer";

        } catch (Exception e) {
            System.err.println("Error en DeveloperController: " + e.getMessage());
            e.printStackTrace();
            return "error/403";
        }
    }

    @GetMapping("/catalogo")
    public String developerDashboard(Model model, Authentication authentication, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(authentication, session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("smg", usuario.getCorreo());

        // Agregar información de impersonación al modelo
        addImpersonationAttributes(model, session);

        return "desarrollador/apis";
    }


}
