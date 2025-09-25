package com.example.telitodev.controller.productowner; // O el paquete que hayas elegido

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.TicketService;
import com.example.telitodev.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize; // Importa esta clase
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/po/tickets")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class TicketController {

    private final TicketService ticketService;
    private final UsuarioService usuarioService;
    private final ApiService apiService;
    private final UsuarioRepository usuarioRepository;

    public TicketController(TicketService ticketService, UsuarioService usuarioService, ApiService apiService, UsuarioRepository usuarioRepository) {
        this.ticketService = ticketService;
        this.usuarioService = usuarioService;
        this.apiService = apiService;
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping
    public String listarTickets(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        
        List<Ticket> listaTickets = ticketService.obtenerTodosTickets();
        model.addAttribute("listaTickets", listaTickets);
        return "po/tickets";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        
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
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        
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
                    System.out.println("🎭 Ticket - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Ticket - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }
}