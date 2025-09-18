package com.example.telitodev.controller.productowner;

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
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class PoController {

    final UsuarioRepository usuarioRepository;
    public PoController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping("/documentacion")
    public String showDocumentacionView(Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        return "po/documentacion";
    }

    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/KPIs";
    }
    @GetMapping("/bandejaSolicitud")
    public String showbandejaSolicitudesView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/bandejaSolicitud";
    }
    @GetMapping("/versolicitud")
    public String showSolicitudesView(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/verSolicitud";
    }
    @GetMapping("/backlog")
    public String showBacklogView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/backlog";
    }
    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/feedback";
    }
    @GetMapping("/roadmap")
    public String showRoadmapView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/roadmap";
    }
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/roadmapGestion";
    }
    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/roadmapDetalle";
    }
    @GetMapping("/feedbackDetalle")
    public String showfeedbackDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/feedbackDetalle";
    }
    @GetMapping("/Dashboard")
    public String showDashboardView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        if (isImpersonating != null && isImpersonating) {
            model.addAttribute("isImpersonating", true);
            model.addAttribute("impersonatedUserDni", session.getAttribute("IMPERSONATED_USER_DNI"));
            model.addAttribute("originalAdminUsername", session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("🎭 PO - Modo impersonación detectado para DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
        } else {
            model.addAttribute("isImpersonating", false);
        }
        
        return "po/Dashboard";
    }
    @GetMapping("/verPerfil")
    public String showverPerfilView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/verPerfil";
    }
    @GetMapping("/verSolicitud")
    public String showverSolicitudView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/verSolicitud";
    }

    @GetMapping("/catalogo")
    public String showCatalogoView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/catalogo";
    }
}
