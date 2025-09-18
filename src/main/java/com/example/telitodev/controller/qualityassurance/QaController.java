package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.entity.CredencialApi;
import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.CredencialApiRepository;
import com.example.telitodev.repository.NotificacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class QaController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    public QaController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @GetMapping("/home")
    public String showQaView(Model model, Authentication auth) {
        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_Dni(usuario.getDni());

        List<Notificacion> notis = notificacionRepository.findByUsuario_Dni(usuario.getDni());
        Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);

        model.addAttribute("usuario", usuario);
        model.addAttribute("NcredActivas", NCredenciales);
        model.addAttribute("credenciales", credenciales);
        model.addAttribute("Nnotis", Nnotis);
        model.addAttribute("notificaciones", notis);

        return "qa/quality";
    }

    @GetMapping("/perfilQa")
    public String showPerfil (Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/perfilQa";
    }

    @GetMapping("/apiDetalle")      //@GetMapping("apis/{id}")      @PathVariable Integer id
    public String showRoadmapView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/apiDetalle";
    }

    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/feedback";
    }
    @GetMapping("/feedbackDetalle")         //@GetMapping("feedback/{id}")      @PathVariable Integer id
    public String showfeedbackDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/feedbackDetalle";
    }



    @GetMapping("/reporteDetalle")      //@GetMapping("reportes/{id}")      @PathVariable Integer id
    public String showReporteDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/reporteDetalle";
    }

    @GetMapping("/soporte")
    public String showSoporte(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/soporte";
    }

    @GetMapping("/issueRealizar")
    public String madeIssue(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/issueRealizar";
    }

    @GetMapping("/reporteRealizar")
    public String madeReport(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/reporteRealizar";
    }
}
