package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
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
@PreAuthorize("hasAnyRole('QA', 'DEV', 'DEVELOPER')")
public class QaDevController extends BaseController {

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
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("ticket", new Ticket());
        List<Api> apis = apiService.getAllApis();
        model.addAttribute("apis", apis);
        return "qa-dev/ticket-form";
    }

    @PostMapping("/tickets/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        
        if (usuario != null) {
            ticket.setUsuario(usuario);
        }
        ticketService.guardarTicket(ticket);
        return "redirect:/qa-dev/home";
    }
}
