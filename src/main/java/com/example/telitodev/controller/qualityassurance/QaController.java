package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class QaController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    final TicketRepository ticketRepository;

    final FeedbackRepository feedbackRepository;
    private final ApiRepository apiRepository;



    public QaController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository, TicketRepository ticketRepository,ReporteRepository reporteRepository,ApiRepository apiRepository, FeedbackRepository feedbackRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
        this.ticketRepository = ticketRepository;

        this.feedbackRepository = feedbackRepository;
        this.apiRepository = apiRepository;

    }

    @GetMapping("/home")
    public String showQaView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_Dni(usuario.getDni());
        List<Notificacion> notis = notificacionRepository.findByUsuario_Dni(usuario.getDni());
        Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("NcredActivas", NCredenciales);
        model.addAttribute("credenciales", credenciales);
        model.addAttribute("Nnotis", Nnotis);
        model.addAttribute("notificaciones", notis);
        return "qa/quality";
    }

    @GetMapping("/perfilQa")
    public String showPerfil (Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/perfilQa";
    }

    @GetMapping("/apiDetalle")
    public String showRoadmapView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/apiDetalle";
    }

    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        List<Feedback> listaFeedback = feedbackRepository.findAll();
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

    @GetMapping("/soporte")
    public String showSoporte(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/soporte";
    }

    /*
    @GetMapping("/issueRealizar")
    public String madeIssue(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/issueRealizar";
    }
    */


    @GetMapping("/reporteRealizar")
    public String madeReport(Model model, Authentication auth, HttpSession session){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/reporteRealizar";
    }
}