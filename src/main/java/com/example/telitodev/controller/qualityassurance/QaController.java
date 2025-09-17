package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class QaController {

    final UsuarioRepository usuarioRepository;
    public QaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping("/catalogo")        //reutilizar vista apis.html de dev?
    public String showCatalogo (Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/catalogo";
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
    @GetMapping("/issue")
    public String showIssueView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/issues";
    }
    @GetMapping("/issueDetalle")        //@GetMapping("issues/{id}")    @PathVariable Integer id
    public String showIssueDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/issueDetalle";
    }
    @GetMapping("/reporte")
    public String showReporteView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/reportes";
    }
    @GetMapping("/reporteDetalle")      //@GetMapping("reportes/{id}")      @PathVariable Integer id
    public String showReporteDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/reporteDetalle";
    }
    
    

}
