package com.example.telitodev.controller.productowner;


import com.example.telitodev.service.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class FeedbackPoController {

    final FeedbackService feedbackService;
    final BacklogService backlogService;
    final UsuarioRepository usuarioRepository;
    final FeedbackRepository feedbackRepository;

    public FeedbackPoController(UsuarioRepository usuarioRepository, FeedbackRepository feedbackRepository, BacklogService backlogService, FeedbackService feedbackService) {
        this.usuarioRepository = usuarioRepository;
        this.feedbackRepository = feedbackRepository;
        this.backlogService = backlogService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        List<Feedback> listaFeedback = feedbackRepository.findAll();
        model.addAttribute("listaFeedback", listaFeedback);
        return "po/feedback";
    }

    @GetMapping("/feedbackDetalle/{id}")
    public String showFeedbackDetalleView(Model model, @PathVariable("id") int idFeedback, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(idFeedback);

        if (feedbackOptional.isPresent()) {
            model.addAttribute("feedback", feedbackOptional.get());
            return "po/feedbackDetalle";
        } else {
            return "redirect:/po/feedback";
        }
    }

    @PostMapping("/registrarFeedbackEnBacklog")
    public String registrarFeedbackEnBacklog(@RequestParam("idFeedback") Integer idFeedback,
                                             @RequestParam("asunto") String asunto,
                                             Authentication auth, // Inyectamos Authentication para obtener el usuario autenticado
                                             Model model) {
        try {
            // Obtener el usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            // Llamamos al FeedbackService para registrar el feedback en el backlog
            Backlog backlog = feedbackService.registrarFeedbackEnBacklog(idFeedback, asunto, usuario);

            System.out.println("Usuario autenticado: " + usuario.getCorreo() + " con rol " + usuario.getRol().getNombreRol());


            // Redirigir al PO a la vista de Feedback o Backlog
            return "redirect:/po/backlog";

        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar el backlog: " + e.getMessage());
            return "po/error"; // Vista de error en caso de fallar
        }
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
