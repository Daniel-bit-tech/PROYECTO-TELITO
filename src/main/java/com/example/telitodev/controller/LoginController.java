package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Optional;

@Controller
public class LoginController {

    final UsuarioRepository usuarioRepository;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (logout != null) {
            model.addAttribute("logout", "Sesion cerrada con exito");
        }
        return "sesion/login";
    }

//    @PostMapping("/login")
//    public String processLogin(@RequestParam String correo, @RequestParam String contrasena, Model model, HttpSession session) {
//
//        return "redirect:/developer";
//    }

    @GetMapping("/home")
    public String home(Authentication authentication) {

        if (authentication == null) {
            return "redirect:/login";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_SADMIN":
                    return "redirect:/admin/home";
                case "ROLE_DEV":
                    return "redirect:/dev/home";
                case "ROLE_QA":
                    return "redirect:/qa/home";
                case "ROLE_PO":
                    return "redirect:/po/home";
                default:
                    return "redirect:/login";
            }
        }

        return "redirect:/login";
    }
}

