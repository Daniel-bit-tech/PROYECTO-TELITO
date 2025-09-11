package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/po")
public class PoController {

    final UsuarioRepository usuarioRepository;
    public PoController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @GetMapping("/documentacion")
    public String showDocumentacionView(Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        return "productOwner/documentacion";
    }

    @GetMapping("/KPIs")
    public String showKPIsView(Model model, Authentication auth) {
        return "productOwner/KPIs";
    }
    @GetMapping("/bandejaSolicitud")
    public String showbandejaSolicitudesView(Model model, Authentication auth) {
        return "productOwner/bandejaSolicitud";
    }
    @GetMapping("/versolicitud")
    public String showSolicitudesView(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/verSolicitud";
    }
    @GetMapping("/backlog")
    public String showBacklogView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/backlog";
    }
    @GetMapping("/feedback")
    public String showFeedbackView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/feedback";
    }
    @GetMapping("/roadmap")
    public String showRoadmapView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/roadmap";
    }
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/roadmapGestion";
    }
    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/roadmapDetalle";
    }
    @GetMapping("/feedbackDetalle")
    public String showfeedbackDetalleView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/feedbackDetalle";
    }
    @GetMapping("/Dashboard")
    public String showDashboardView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/Dashboard";
    }
    @GetMapping("/verPerfil")
    public String showverPerfilView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/verPerfil";
    }
    @GetMapping("/verSolicitud")
    public String showverSolicitudView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "productOwner/verSolicitud";
    }

}
