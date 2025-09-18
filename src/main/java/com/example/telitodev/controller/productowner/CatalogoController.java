package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService; // Importa el servicio
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importa @RequestParam
import java.util.Optional;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class CatalogoController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;


    public CatalogoController(UsuarioRepository usuarioRepository, ApiService apiService) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
    }

    @GetMapping("/catalogo")
    public String showCatalogoView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/catalogo";
    }

    @GetMapping("/documentacion")
    public String showDocumentacionView(@RequestParam("id") Integer idApi, Model model, Authentication auth) {

        Optional<Api> apiOptional = apiService.getApiById(idApi);

        if (apiOptional.isPresent()) {
            Api api = apiOptional.get();
            model.addAttribute("api", api);
        } else {

            return "redirect:/po/catalogo";
        }

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        return "po/documentacion";
    }
}
