package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.FeedbackRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class FeedbackPoController {

    final UsuarioRepository usuarioRepository;
    final FeedbackRepository feedbackRepository;

    public FeedbackPoController(UsuarioRepository usuarioRepository, FeedbackRepository feedbackRepository) {
        this.usuarioRepository = usuarioRepository;
        this.feedbackRepository = feedbackRepository;
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
}
