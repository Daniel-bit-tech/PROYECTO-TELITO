package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class QaController {

    final UsuarioRepository usuarioRepository;
    public QaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping("/catalogo")        //reutilizar vista apis.html de dev?
    public String showCatalogo (Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        if (isImpersonating != null && isImpersonating) {
            model.addAttribute("isImpersonating", true);
            model.addAttribute("impersonatedUserDni", session.getAttribute("IMPERSONATED_USER_DNI"));
            model.addAttribute("originalAdminUsername", session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("🎭 QA - Modo impersonación detectado para DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
        } else {
            model.addAttribute("isImpersonating", false);
        }
        
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
    
    @GetMapping("/perfil")
    public String showPerfil(Model model, Authentication authentication) {
        System.out.println("=== DEBUG PERFIL QA ===");
        System.out.println("Authentication name: " + authentication.getName());
        
        // Obtener información del usuario autenticado
        String correo = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        
        if (usuario != null) {
            System.out.println("Usuario encontrado: " + usuario.getNombre() + " " + usuario.getApellidoPaterno());
            System.out.println("Rol: " + usuario.getRol().getNombreRol());
            System.out.println("Fecha registro: " + usuario.getFechaRegistro());
            model.addAttribute("usuario", usuario);
        } else {
            System.out.println("Usuario NO encontrado para correo: " + correo);
        }
        
        System.out.println("Retornando vista: desarrollador/perfil (reutilizada para QA)");
        return "desarrollador/perfil";
    }
    

}
