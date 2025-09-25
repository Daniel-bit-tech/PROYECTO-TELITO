package com.example.telitodev.controller.desarrollador;

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
@PreAuthorize("hasAnyRole('DEV', 'SUPERADMIN')")
public class DeveloperController {

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
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);

        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());

        List<Notificacion> notis = notificacionRepository.findByUsuario_Dni(usuario.getDni());
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
        
        // Agregar información de impersonación al modelo
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        if (isImpersonating != null && isImpersonating) {
            model.addAttribute("isImpersonating", true);
            model.addAttribute("impersonatedUserDni", session.getAttribute("IMPERSONATED_USER_DNI"));
            model.addAttribute("originalAdminUsername", session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("🎭 DEV - Modo impersonación detectado para DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
        } else {
            model.addAttribute("isImpersonating", false);
        }

        return "desarrollador/developer";
    }

    @GetMapping("/catalogo")
    public String developerDashboard(Model model, Authentication authentication, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(authentication, session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("smg", usuario.getCorreo());

        return "desarrollador/apis";
    }

    /**
     * Método helper para obtener el usuario correcto durante impersonación
     */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        // Verificar si hay impersonación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        
        if (isImpersonating != null && isImpersonating) {
            // Durante impersonación, obtener usuario por DNI del usuario impersonado
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    System.out.println("🎭 DEV - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 DEV - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }
}
