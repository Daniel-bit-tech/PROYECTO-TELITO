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
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showFeedbackView(Model model, Authentication auth, HttpSession session,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "8") int size) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);

        // Filtrar feedbacks del usuario autenticado
        Page<Feedback> feedbackPage = feedbackRepository.findByUsuario(usuario, pageable);
        model.addAttribute("feedbackPage", feedbackPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());

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
                                 Authentication auth, RedirectAttributes redirectAttributes) {

        // --- INICIO DE VALIDACIONES ---
        boolean hasErrors = false;

        // Validación 1: Comentario no puede estar vacío
        if (comentario == null || comentario.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorComentario", "El comentario no puede estar vacío.");
            hasErrors = true;
        }
        // Validación 2: Comentario no puede exceder 400 caracteres
        else if (comentario.length() > 400) {
            redirectAttributes.addFlashAttribute("errorComentario", "El comentario no puede superar los 400 caracteres.");
            hasErrors = true;
        }

        // Validación 3: Calificación debe estar en el rango de 1 a 5
        if (calificacion < 1 || calificacion > 5) {
            redirectAttributes.addFlashAttribute("errorCalificacion", "Por favor, seleccione una calificación válida.");
            hasErrors = true;
        }

        // Si hay errores, redirigir de vuelta al formulario de creación
        if (hasErrors) {
            // Guardamos los datos enviados para que el usuario no los pierda
            redirectAttributes.addFlashAttribute("submittedComentario", comentario);
            redirectAttributes.addFlashAttribute("submittedCalificacion", calificacion);
            return "redirect:/qa/crearFeedback/" + apiId;
        }
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
