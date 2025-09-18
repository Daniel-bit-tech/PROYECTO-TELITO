package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.ApiService; // Importa el servicio
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importa @RequestParam

import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class CatalogoController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;
    final ApiRepository apiRepository;

    public CatalogoController(UsuarioRepository usuarioRepository, ApiService apiService, ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/catalogo")
    public String showCatalogoView(@RequestParam(value = "nombre", required = false) String nombre,
                                   Model model, Authentication auth) {

        List<Api> apis;

        if (nombre != null && !nombre.trim().isEmpty()) {
            // Llama a un método que filtre solo por nombre
            apis = apiRepository.findByNombreContainingIgnoreCase(nombre);
        } else {
            // Si no hay nombre, muestra todas las APIs
            apis = apiRepository.findAll();
        }

        model.addAttribute("apis", apis);
        model.addAttribute("nombre", nombre); // Esto es importante para mantener el valor en el buscador

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
