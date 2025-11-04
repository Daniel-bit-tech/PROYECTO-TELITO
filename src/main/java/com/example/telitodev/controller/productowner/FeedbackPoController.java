package com.example.telitodev.controller.productowner;

import com.example.telitodev.repository.po.ActividadRecienteRepository;
import com.example.telitodev.service.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class FeedbackPoController {

    final FeedbackService feedbackService;
    final BacklogService backlogService;
    final UsuarioRepository usuarioRepository;
    final FeedbackRepository feedbackRepository;
    final BacklogRepository backlogRepository;
    final ActividadRecienteRepository actividadRecienteRepository;

    public FeedbackPoController(BacklogRepository backlogRepository,UsuarioRepository usuarioRepository,
                                FeedbackRepository feedbackRepository, BacklogService backlogService,
                                FeedbackService feedbackService, ActividadRecienteRepository actividadRecienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.feedbackRepository = feedbackRepository;
        this.backlogService = backlogService;
        this.feedbackService = feedbackService;
        this.backlogRepository = backlogRepository;
        this.actividadRecienteRepository = actividadRecienteRepository;
        }

    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        // Obtener la organización del PO
        Organizacion organizacion = usuario.getOrganizacion();

        List<Feedback> listaFeedback;

        if (organizacion != null) {
            // Filtrar feedbacks solo de la organización del PO
            listaFeedback = feedbackRepository.findByUsuarioOrganizacionId(organizacion.getIdOrganizacion());

            // Debug mejorado
            System.out.println("=== DEBUG FEEDBACK ORGANIZACIÓN ===");
            System.out.println("PO: " + usuario.getCorreo());
            System.out.println("Organización: " + organizacion.getNombre());
            System.out.println("Total feedbacks encontrados: " + listaFeedback.size());

            for (Feedback feedback : listaFeedback) {
                System.out.println("Feedback ID: " + feedback.getIdFeedback() +
                        " | API: " + feedback.getApi().getNombre() +
                        " | Usuario: " + feedback.getUsuario().getCorreo() +
                        " | Calificación: " + feedback.getCalificacion());
            }
        } else {
            // Si el PO no tiene organización, mostrar lista vacía
            listaFeedback = new ArrayList<>();
            System.out.println("ADVERTENCIA: El PO no tiene organización asignada");
        }

        model.addAttribute("listaFeedback", listaFeedback);
        return "po/feedback";
    }

    @GetMapping("/feedbackDetalle/{id}")
    public String showFeedbackDetalleView(Model model, @PathVariable("id") int idFeedback, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(idFeedback);

        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            model.addAttribute("feedback", feedback);

            // Verificar si ya está en backlog para controlar la vista
            Optional<Backlog> existingBacklog = backlogRepository.findByFeedback_IdFeedback(idFeedback);
            if (existingBacklog.isPresent()) {
                model.addAttribute("feedbackYaRegistrado", true);
                model.addAttribute("feedbackRegistrado", false);
            } else {
                model.addAttribute("feedbackYaRegistrado", false);
                model.addAttribute("feedbackRegistrado", false);
            }

            return "po/feedbackDetalle";
        } else {
            return "redirect:/po/feedback";
        }
    }

    @PostMapping("/registrarFeedbackEnBacklog")
    public String registrarFeedbackEnBacklog(@RequestParam("idFeedback") Integer idFeedback,
                                             @RequestParam("asunto") String asunto,
                                             Model model,
                                             Authentication auth,
                                             HttpSession session) {

        try {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);

            // Verificar si ya existe
            Optional<Backlog> existingBacklog = backlogRepository.findByFeedback_IdFeedback(idFeedback);

            if (existingBacklog.isPresent()) {
                model.addAttribute("error", "Este feedback ya está registrado en el backlog");
                model.addAttribute("feedbackYaRegistrado", true);
                model.addAttribute("feedbackRegistrado", false);
            } else {
                // ✅ Este método ahora actualiza automáticamente registradoBacklog
                feedbackService.registrarFeedbackEnBacklog(idFeedback, asunto, usuario);

                // === ACTIVIDAD RECIENTE ===
                Optional<Feedback> feedbackOpt = feedbackRepository.findById(idFeedback);
                if (feedbackOpt.isPresent()) {
                    Feedback feedback = feedbackOpt.get();

                    ActividadReciente actividad = new ActividadReciente();
                    actividad.setTitulo("Feedback Registrado en Backlog");
                    actividad.setDescripcion("Has registrado en backlog el feedback de " +
                            feedback.getUsuario().getNombre() + " " +
                            feedback.getUsuario().getApellidoPaterno() +
                            " para la API: " + feedback.getApi().getNombre());
                    actividad.setUsuario(usuario);
                    actividad.setFecha(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
                    actividadRecienteRepository.save(actividad);

                    System.out.println("✅ Feedback registrado en backlog - Actividad registrada para PO: " + usuario.getCorreo());
                }

                model.addAttribute("success", "Feedback registrado exitosamente en el backlog");
                model.addAttribute("feedbackRegistrado", true);
                model.addAttribute("feedbackYaRegistrado", false);
            }

            // Cargar el feedback actualizado (con registradoBacklog = true)
            Optional<Feedback> feedbackOptional = feedbackRepository.findById(idFeedback);
            if (feedbackOptional.isPresent()) {
                model.addAttribute("feedback", feedbackOptional.get());
                return "po/feedbackDetalle";
            } else {
                return "redirect:/po/feedback";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar en el backlog: " + e.getMessage());
            return "po/error";
        }
    }


    @PostMapping("/feedback/{id}/registrar-backlog")
    public String registrarEnBacklog(@PathVariable Integer id) {
        feedbackRepository.marcarComoRegistradoEnBacklog(id);
        return "redirect:/po/feedback"; // o a donde quieras redirigir
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
