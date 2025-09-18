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
public class IssueController {
    final UsuarioRepository usuarioRepository;
    public IssueController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/issues")
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
}