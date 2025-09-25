package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.TicketRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class FeedbackPoController {

    final UsuarioRepository usuarioRepository;
    final TicketRepository ticketRepository;

    public FeedbackPoController(UsuarioRepository usuarioRepository, TicketRepository ticketRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        model.addAttribute("listaTickets", ticketRepository.findAll());
        return "po/feedback";
    }


    @GetMapping("/feedbackDetalle/{id}")
    public String showFeedbackDetalleView(Model model, @PathVariable("id") int idTicket, Authentication auth, HttpSession session) {

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        Optional<Ticket> ticketOptional = ticketRepository.findByIdWithDetails(idTicket);

        if (ticketOptional.isPresent()) {
            model.addAttribute("ticket", ticketOptional.get());
            return "po/feedbackDetalle";
        } else {
            return "redirect:/po/feedback";
        }
    }


    @PostMapping("/tickets/resolver/{id}")
    public String resolverTicket(@PathVariable("id") int idTicket) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(idTicket);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            ticket.setEstado(true);
            ticketRepository.save(ticket);
        }
        return "redirect:/po/feedback";
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
                    System.out.println("🎭 Feedback - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Feedback - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }

}
