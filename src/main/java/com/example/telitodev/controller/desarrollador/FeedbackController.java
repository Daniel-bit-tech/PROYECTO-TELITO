package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.FeedbackRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAnyRole('DEV', 'DEVELOPER', 'QA')")
public class FeedbackController {

    private final UsuarioRepository usuarioRepository;
    private final ApiService apiService;
    private final ApiRepository apiRepository;
    private final FeedbackRepository feedbackRepository;

    public FeedbackController(UsuarioRepository usuarioRepository, ApiService apiService, ApiRepository apiRepository, FeedbackRepository feedbackRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.apiRepository = apiRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model, Authentication auth) {
        List<Api> apis = apiService.getAllApis();
        model.addAttribute("apis", apis);

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        List<Feedback> misFeedback = feedbackRepository.findByUsuario_Dni(usuario.getDni());
        model.addAttribute("misFeedback", misFeedback);

        // Traer feedbacks que fueron hechos a las APIs de este usuario
        List<Feedback> apisFeedback = feedbackRepository.findByApi_Usuario_Dni(usuario.getDni());
        model.addAttribute("apisFeedback", apisFeedback);

        return "desarrollador/feedback";
    }

    @PostMapping("/feedback/guardar")
    public String saveFeedback(@RequestParam("apiId") int apiId,
                               @RequestParam("comentario") String comentario,
                               @RequestParam("calificacion") int calificacion,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {

            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/feedback";
            }


            Optional<Api> apiOptional = apiRepository.findById(apiId);
            if (!apiOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "API no encontrada.");
                return "redirect:/feedback";
            }


            Feedback feedback = new Feedback();
            feedback.setComentario(comentario);
            feedback.setCalificacion(calificacion);
            feedback.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
            feedback.setApi(apiOptional.get());
            feedback.setUsuario(usuario);


            feedbackRepository.save(feedback);

            redirectAttributes.addFlashAttribute("message", "¡Feedback enviado con éxito!");
        } catch (Exception e) {

            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al enviar el feedback: " + e.getMessage());
        }
        return "redirect:/feedback";
    }
}