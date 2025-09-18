package com.example.telitodev.controller; // O el paquete que hayas elegido

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.TicketService;
import com.example.telitodev.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize; // Importa esta clase
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/po/tickets")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class TicketController {

    private final TicketService ticketService;
    private final UsuarioService usuarioService;
    private final ApiService apiService;

    public TicketController(TicketService ticketService, UsuarioService usuarioService, ApiService apiService) {
        this.ticketService = ticketService;
        this.usuarioService = usuarioService;
        this.apiService = apiService;
    }


    @GetMapping
    public String listarTickets(Model model) {
        List<Ticket> listaTickets = ticketService.obtenerTodosTickets();
        model.addAttribute("listaTickets", listaTickets);
        return "po/tickets";
    }


    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("ticket", new Ticket());
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        List<Api> apis = apiService.getAllApis();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("apis", apis);
        return "po/ticket-form";
    }


    @PostMapping("/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket) {
        ticketService.guardarTicket(ticket);
        return "redirect:/po/tickets";
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, Model model) {
        Optional<Ticket> ticketOpt = ticketService.obtenerTicketPorId(id);
        if (ticketOpt.isPresent()) {
            model.addAttribute("ticket", ticketOpt.get());
            List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
            List<Api> apis = apiService.getAllApis();
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("apis", apis);
            return "po/ticket-form";
        }
        return "redirect:/po/tickets";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarTicket(@PathVariable("id") Integer id) {
        ticketService.eliminarTicket(id);
        return "redirect:/po/tickets";
    }
}