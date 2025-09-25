package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/apis")

public class ApiController {

    final ApiRepository apiRepository;
    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;
    final VersionApiRepository versionApiRepository;
    final EjemplosCodigoRepository ejemplosCodigoRepository;

    public ApiController(ApiRepository apiRepository, UsuarioRepository usuarioRepository, VersionApiRepository versionApiRepository, DocumentacionRepository documentacionRepository, VersionApiRepository versionApiRepository1, EjemplosCodigoRepository ejemplosCodigoRepository) {
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
        this.versionApiRepository = versionApiRepository1;
        this.ejemplosCodigoRepository = ejemplosCodigoRepository;
    }

    @GetMapping()
    public String catalogo(@RequestParam(value = "dominios",required = false) List<String> selDominios,
                           @RequestParam(value = "tags", required = false) List<String> selTags,
                           @RequestParam(value = "nombre", required = false) String nombre,
                            Model model, Authentication auth, HttpSession session) {
        String dominios = selDominios == null ? null : String.join(",", selDominios);
        String tags = selTags == null ? null : String.join(",", selTags);

        System.out.println("dominios: " + dominios);
        System.out.println("tags: " + tags);

        List<Api> apis = apiRepository.findByFilters(nombre, dominios, tags);
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());

        }

        if (auth != null && auth.isAuthenticated()) {
            // Obtener el usuario correcto considerando impersonación
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        model.addAttribute("apis", apis);
        model.addAttribute("tags", selTags);
        model.addAttribute("dominios", selDominios);
        model.addAttribute("nombre", nombre);

        return "desarrollador/apis";
    }

    @GetMapping("/{id}/docs")
//    @PreAuthorize("hasAnyRole('DEV','SUPERADMIN','QA','PO')")
    @PreAuthorize("isAuthenticated()")
    public String detalleApi(@PathVariable Integer id,
                             @RequestParam(value = "fecha",required = false) String fecha,
                             Model model, Authentication auth, HttpSession session) {

        System.out.println("\n\n\n DOCS \n");

        boolean apiExists = apiRepository.existsById(id);
        if (apiExists) {

            List<Documentacion> docs = documentacionRepository.findByApi_IdApiOrderByFechaCreacionDesc(id);
            model.addAttribute("docs", docs);
        }

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("fecha", fecha);

        return "desarrollador/documentacion";
    }


    @GetMapping("/{id}/test")
    @PreAuthorize("isAuthenticated()")
    public String testApi(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {
        Optional<Api> api = apiRepository.findById(id);
        if (api.isPresent()) {
            model.addAttribute("api", api.get());
        }

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        return "desarrollador/sandbox";
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
                    System.out.println("🎭 API DEV - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 API DEV - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }

}
