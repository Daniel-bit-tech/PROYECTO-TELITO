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
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class CatalogoController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;
    final ApiRepository apiRepository;
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;

    public CatalogoController(UsuarioRepository usuarioRepository, ApiService apiService, ApiRepository apiRepository, DominioRepository dominioRepository, TagRepository tagRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.apiRepository = apiRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
    }
    @GetMapping("/catalogo")
    public String showCatalogoView(@RequestParam(value = "nombre", required = false) String nombre,
                                   @RequestParam(value = "dominios", required = false) List<Integer> selDominios,
                                   @RequestParam(value = "tags", required = false) List<Integer> selTags,
                                   Model model, Authentication auth, HttpSession session) {

        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: " + dominios + " \nTags: " + tags);

        // Se determina el filtro para obtener las APIs
        List<Api> apis;
        if (nombre != null && !nombre.trim().isEmpty()) {
            // Si hay un nombre, filtra por nombre
            apis = apiRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (selDominios != null || selTags != null) {
            // Si hay filtros por dominios o tags
            apis = apiRepository.findByFilters(nombre, selDominios, selTags);
        } else {
            // Si no hay filtro, obtiene todas las APIs
            apis = apiRepository.findAll();
        }

        // Imprime los nombres de las APIs en la consola
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());
        }

        // Si hay un usuario autenticado, agrega la información del usuario
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        // Se agregan las listas de dominios y tags al modelo
        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());

        // Se agregan las APIs y los filtros seleccionados al modelo
        model.addAttribute("apis", apis);
        model.addAttribute("selTags", selTags);
        model.addAttribute("selDominios", selDominios);
        model.addAttribute("nombre", nombre);

        return "po/catalogo";
    }


    @GetMapping("/documentacion")
    public String showDocumentacionView(@RequestParam("id") Integer idApi, Model model, Authentication auth, HttpSession session) {

        Optional<Api> apiOptional = apiService.getApiById(idApi);

        if (apiOptional.isPresent()) {
            Api api = apiOptional.get();
            model.addAttribute("api", api);
        } else {

            return "redirect:/po/catalogo";
        }

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        return "po/documentacion";
    }
    
    /**
     * Método helper para obtener el usuario correcto durante impersonación
     */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        // Verificar si hay impersonación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        
        if (isImpersonating != null && isImpersonating) {
            // Durante impersonación, obtener usuario por DNI del usuario impersonado
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    System.out.println("🎭 Catalogo - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Catalogo - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }


}
