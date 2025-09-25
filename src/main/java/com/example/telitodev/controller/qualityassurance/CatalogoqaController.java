package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
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
public class CatalogoqaController {
    final UsuarioRepository usuarioRepository;
    final ApiRepository apiRepository;

    public CatalogoqaController(UsuarioRepository usuarioRepository, ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/catalogo")        //reutilizar vista apis.html de dev?
    public String showCatalogo (Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        // Obtener las APIs relacionadas al proyecto y la organización del usuario
        List<ApiProyectoDTO> apis = apiRepository.findApisByUsuarioAndProyecto(usuario.getDni());
        model.addAttribute("usuario", usuario);
        model.addAttribute("apis", apis);
        return "qa/catalogo";
    }
}