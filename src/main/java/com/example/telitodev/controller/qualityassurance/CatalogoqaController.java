package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.Dominio;
import com.example.telitodev.entity.Tag;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.DominioRepository;
import com.example.telitodev.repository.TagRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class CatalogoqaController extends BaseController {
    final UsuarioRepository usuarioRepository;
    final ApiRepository apiRepository;
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;

    public CatalogoqaController(UsuarioRepository usuarioRepository, ApiRepository apiRepository,
                                DominioRepository dominioRepository, TagRepository tagRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping("/catalogo")        //reutilizar vista apis.html de dev?
    public String showCatalogo (@RequestParam(required = false) String nombre,
                                @RequestParam(required = false) List<String> dominios,
                                @RequestParam(required = false) List<String> tags,
                                Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        String dni = usuario.getDni();

        // Evitar problemas con IN () en JPQL: enviar null si la lista está vacía
        if (dominios != null && dominios.isEmpty()) dominios = null;
        if (tags != null && tags.isEmpty()) tags = null;

        // Obtener las APIs relacionadas al proyecto y la organización del usuario
        List<ApiProyectoDTO> apis = apiRepository.findApisByFilters(dni, nombre, dominios, tags);


        // Cargar listas completas para poblar checkboxes
        List<Dominio> allDominios = dominioRepository.findAll();
        List<Tag> allTags = tagRepository.findAll();

        model.addAttribute("usuario", usuario);
        model.addAttribute("apis", apis);
        model.addAttribute("dominios", allDominios);
        model.addAttribute("tags", allTags);

        // para mantener el estado de los checkboxes y del input
        model.addAttribute("selectedDominios", dominios);
        model.addAttribute("selectedTags", tags);
        model.addAttribute("nombre", nombre);
        return "qa/catalogo";
    }
}