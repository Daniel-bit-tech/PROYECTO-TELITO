package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.doc_alto_nivel;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.ApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class CatalogoController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;
    final ApiRepository apiRepository;
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;
    final DocAltoNivelRepository docAltoNivelRepository;

    public CatalogoController(DocAltoNivelRepository docAltoNivelRepository, DominioRepository dominioRepository, TagRepository tagRepository, UsuarioRepository usuarioRepository, ApiService apiService, ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.docAltoNivelRepository = docAltoNivelRepository;
        this.apiRepository = apiRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping("/catalogo")
    public String showCatalogoView(@RequestParam(value = "nombre", required = false) String nombre,
                                   @RequestParam(value = "dominios",required = false) List<Integer> selDominios,
                                   @RequestParam(value = "tags", required = false) List<Integer> selTags,
                                   Model model, Authentication auth, HttpSession session) {

        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: "+dominios + " \nTags: " + tags);

        //trae todas las apis y lo filtra por nombre dominio y tags
        List<Api> apis = apiRepository.findByFilters(nombre, selDominios, selTags);
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());
        }

        if (auth != null && auth.isAuthenticated()) {
            // Obtener el usuario correcto considerando impersonación
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());

        model.addAttribute("apis", apis);
        model.addAttribute("selTags", selTags);
        model.addAttribute("selDominios", selDominios);
        model.addAttribute("nombre", nombre);

        System.out.println("Tags" + selTags);
        System.out.println("Dominios" + dominios);

        return "po/catalogo";
    }


    @GetMapping("/documentacion")
    public String showDocumentacionView(@RequestParam("id") Integer idApi, Model model, Authentication auth, HttpSession session) {

        // Obtener la documentación de alto nivel para la API seleccionada
        Optional<doc_alto_nivel> docAltoNivelOptional = docAltoNivelRepository.findByApi_IdApi(idApi);
        Optional<Api> apiOptional = apiService.getApiById(idApi);


        if (docAltoNivelOptional.isPresent() && apiOptional.isPresent()) {
            // Si la documentación existe, la añadimos al modelo
            Api api = apiOptional.get();
            model.addAttribute("api", api);
            System.out.println("API seleccionada: " + api);
            doc_alto_nivel docAltoNivel = docAltoNivelOptional.get();
            model.addAttribute("doc", docAltoNivel);
        } else {
            // Si no existe, redirigimos al catálogo
            return "redirect:/po/catalogo";
        }

        // Obtener el usuario autenticado
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuario", usuario);

        return "po/documentacion";  // Página de documentación de alto nivel
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
        if (usuario == null) {
            // Try corporate email if not found by personal email
            usuario = usuarioRepository.findByCorreoCorporativo(auth.getName());
            if (usuario != null) {
                System.out.println("👤 Catalogo - Usuario autenticado por correo corporativo: " + usuario.getNombre());
            }
        } else {
            System.out.println("👤 Catalogo - Usando datos del usuario autenticado: " + usuario.getNombre());
        }
        return usuario;
    }
}
