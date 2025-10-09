package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController extends BaseController {

    final UsuarioRepository usuarioRepository;
    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/perfil")
    public String showperfil(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/perfil";
    }
}