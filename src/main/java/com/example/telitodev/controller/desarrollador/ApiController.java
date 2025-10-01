package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.VersionApi;
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
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;

    public ApiController(ApiRepository apiRepository, UsuarioRepository usuarioRepository, VersionApiRepository versionApiRepository, DocumentacionRepository documentacionRepository, VersionApiRepository versionApiRepository1, EjemplosCodigoRepository ejemplosCodigoRepository, DominioRepository dominioRepository, TagRepository tagRepository) {
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
        this.versionApiRepository = versionApiRepository1;
        this.ejemplosCodigoRepository = ejemplosCodigoRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
    }


    @GetMapping()
    public String catalogo(@RequestParam(value = "dominios",required = false) List<Integer> selDominios,
                           @RequestParam(value = "tags", required = false) List<Integer> selTags,
                           @RequestParam(value = "nombre", required = false) String nombre,
                            Model model, Authentication auth, HttpSession session) {
        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: "+dominios + " \nTags: " + tags);

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
    // Endpoint de Versiones de Api específica
    @GetMapping("/{id}/versiones")
    @ResponseBody
    public List<VersionApi> obtenerVersiones(@PathVariable Integer id) {
        return versionApiRepository.findByApi_IdApi(id);
//        return versionApiRepository.findAll();
    }

}
