package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.TicketService;
import com.example.telitodev.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.telitodev.repository.UsuarioRepository;


@Controller("productOwnerQaDevController")
@RequestMapping("/qa-dev")
@PreAuthorize("hasAnyRole('QA', 'DEV')")
public class QaDevController {

    private final TicketService ticketService;
    private final ApiService apiService;
    private final UsuarioRepository usuarioRepository;

    public QaDevController(TicketService ticketService, ApiService apiService, UsuarioRepository usuarioRepository) {
        this.ticketService = ticketService;
        this.apiService = apiService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/tickets/crear")
    public String mostrarFormularioCreacion(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        
        model.addAttribute("ticket", new Ticket());
        List<Api> apis = apiService.getAllApis();
        model.addAttribute("apis", apis);
        return "qa-dev/ticket-form";
    }

    @PostMapping("/tickets/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        
        if (usuario != null) {
            ticket.setUsuario(usuario);
        }
        ticketService.guardarTicket(ticket);
        return "redirect:/qa-dev/home";
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
                    System.out.println("🎭 QaDev - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 QaDev - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }
}
