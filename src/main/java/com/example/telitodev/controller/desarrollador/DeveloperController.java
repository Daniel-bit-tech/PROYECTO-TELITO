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

import java.util.List;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasAnyRole('DEV', 'SADMIN')")
public class DeveloperController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    final TicketRepository ticketRepository;
    public DeveloperController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository, TicketRepository ticketRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
        this.ticketRepository = ticketRepository;
    }


    @GetMapping("/home")
    public String showDeveloperView(Model model, Authentication auth, HttpSession session) {
        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());

        List<Notificacion> notis = notificacionRepository.findByUsuario_Dni(usuario.getDni());
        Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);

        List<Ticket> tickets = ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());

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
    public String developerDashboard(Model model, Authentication authentication) {
        model.addAttribute("smg", authentication.getName());





        return "desarrollador/apis";
    }
}
