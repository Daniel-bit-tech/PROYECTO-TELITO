package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.ActividadReciente;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.FeedbackRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class FeedbackQaController extends BaseController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ActividadRecienteRepository actividadRecienteRepository;


    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // Filtrar feedbacks del usuario autenticado
        List<Feedback> listaFeedback = feedbackRepository.findByUsuario(usuario);
        model.addAttribute("listaFeedback", listaFeedback);

        return "qa/feedback";
    }

    @GetMapping("/feedbackDetalle/{id}")
    public String showFeedbackDetalleView(Model model, @PathVariable("id") int idFeedback, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(idFeedback);

        if (feedbackOptional.isPresent()) {
            model.addAttribute("feedback", feedbackOptional.get());
            return "qa/feedbackDetalle";
        } else {
            return "redirect:/qa/feedback";
        }
    }

    @GetMapping("/crearFeedback/{apiId}")
    public String madeFeedback(@PathVariable("apiId") Integer apiId, Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Obtener la API por su id
        Optional<Api> apiOptional = apiRepository.findById(apiId);
        if (apiOptional.isPresent()) {
            model.addAttribute("api", apiOptional.get());
        } else {
            return "redirect:/qa/feedback"; // Si no existe la API, redirige a la lista de feedback
        }

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);
        model.addAttribute("feedback", new Feedback()); // Un objeto Feedback vacío para el formulario

        // Registrar la actividad reciente
        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Feedback");
        actividad.setDescripcion("El QA " + usuario.getNombre() + " ha creado un feedback.");
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);


        return "qa/feedbackCrear"; // Vista para crear el feedback
    }

    @PostMapping("/guardarFeedback")
    public String submitFeedback(@RequestParam("comentario") String comentario,
                                 @RequestParam("calificacion") int calificacion,
                                 @RequestParam("apiId") int apiId,
                                 Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        Api api = apiRepository.findById(apiId).orElseThrow(() -> new RuntimeException("API no encontrada"));

        Feedback feedback = new Feedback();
        feedback.setComentario(comentario);
        feedback.setCalificacion(calificacion);
        feedback.setApi(api);
        feedback.setUsuario(usuario);

        // Establecer la fecha de creación al momento de la creación del feedback
        feedback.setFechaCreacion(new Timestamp(System.currentTimeMillis()));

        System.out.println("Hemos llegado a guardar el feedback");

        feedbackRepository.save(feedback);



        return "redirect:/qa/feedback"; // Redirigir al listado de feedback después de crear
    }

}
