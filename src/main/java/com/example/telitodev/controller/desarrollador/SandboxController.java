package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasAnyRole('DEV', 'DEVELOPER', 'SUPERADMIN')")
public class SandboxController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/sandbox")
    public String showSandboxPage(Model model, Authentication authentication, HttpSession session) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByCorreo(email);

        model.addAttribute("usuario", usuario);
        Boolean isImpersonating = (Boolean) session.getAttribute("isImpersonating");
        model.addAttribute("isImpersonating", isImpersonating != null && isImpersonating);
        model.addAttribute("impersonatedUserName", session.getAttribute("impersonatedUserName"));
        model.addAttribute("impersonatedRole", session.getAttribute("impersonatedRole"));

        return "desarrollador/sandbox";
    }
}