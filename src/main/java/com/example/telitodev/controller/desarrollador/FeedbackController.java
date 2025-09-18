package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Api;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Controller
@PreAuthorize("hasAnyRole('DEV', 'QA')")
public class FeedbackController {

    private final UsuarioRepository usuarioRepository;
    private final ApiService apiService;
    private final TicketService ticketService;

    public FeedbackController(UsuarioRepository usuarioRepository, ApiService apiService, TicketService ticketService) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.ticketService = ticketService;
    }

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model, Authentication auth) {
        List<Api> apis = apiService.getAllApis();
        model.addAttribute("apis", apis);
        model.addAttribute("ticket", new Ticket());

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        List<Ticket> misTickets = ticketService.obtenerTicketsPorUsuario(usuario.getDni());
        model.addAttribute("misTickets", misTickets);

        return "desarrollador/feedback";
    }

    @PostMapping("/feedback/guardar")
    public String saveFeedback(@ModelAttribute("ticket") Ticket ticket, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        if (usuario != null) {
            ticket.setUsuario(usuario);
        }
        ticket.setFechaCreacion(new Timestamp(new Date().getTime()));

        ticket.setEstado(false);

        ticketService.guardarTicket(ticket);
        return "redirect:/feedback?success";
    }
}