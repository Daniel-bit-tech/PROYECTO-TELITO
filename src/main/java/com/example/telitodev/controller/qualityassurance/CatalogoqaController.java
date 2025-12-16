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
import org.springframework.data.domain.*;
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
@PreAuthorize("hasAnyRole('QA', 'SUPERADMIN')")
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

    @GetMapping("/catalogo")
    public String showCatalogo (@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size, // Tamaño de página por defecto
                                @RequestParam(required = false) String nombre,
                                @RequestParam(required = false) List<String> dominios,
                                @RequestParam(required = false) List<String> tags,
                                Model model, Authentication auth, HttpSession session) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        String dni = usuario.getDni();

        // Configurar la paginación
        Pageable pageable = PageRequest.of(page, size);

        // Evitar problemas con IN () en JPQL: enviar null si la lista está vacía
        if (dominios != null && dominios.isEmpty()) dominios = null;
        if (tags != null && tags.isEmpty()) tags = null;

        // Obtener la página de APIs DTO desde el repositorio
        Page<ApiProyectoDTO> apiPage = apiRepository.findApisForQaCatalog(dni, nombre, dominios, tags, pageable);

        // Si la página solicitada está fuera de rango, ajustar a la última página válida
        if (page >= apiPage.getTotalPages() && apiPage.getTotalPages() > 0) {
            page = apiPage.getTotalPages() - 1;
            pageable = PageRequest.of(page, size);
            apiPage = apiRepository.findApisForQaCatalog(dni, nombre, dominios, tags, pageable);
        }

        // Cargar listas completas para poblar checkboxes de filtros
        List<Dominio> allDominios = dominioRepository.findAll();
        List<Tag> allTags = tagRepository.findAll();

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        // Pasar datos al modelo
        model.addAttribute("usuario", usuario);
        model.addAttribute("apiPage", apiPage); // Enviar el objeto Page completo a la vista
        model.addAttribute("dominios", allDominios);
        model.addAttribute("tags", allTags);

        // Devolver los filtros seleccionados para mantener el estado en la vista
        model.addAttribute("selectedDominios", dominios);
        model.addAttribute("selectedTags", tags);
        model.addAttribute("nombre", nombre);

        return "qa/catalogo";
    }
}