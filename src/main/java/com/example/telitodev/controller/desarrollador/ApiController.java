package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.controller.BaseController;
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

public class ApiController extends BaseController {

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
            // Obtener el usuario correcto considerando impersonación usando BaseController
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
        }

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());

        model.addAttribute("apis", apis);
        model.addAttribute("selTags", selTags);
        model.addAttribute("selDominios", selDominios);
        model.addAttribute("nombre", nombre);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/apis";
    }

    @GetMapping("/{id}/docs")
//    @PreAuthorize("hasAnyRole('DEV','SUPERADMIN','QA','PO')")
    @PreAuthorize("isAuthenticated()")
    public String detalleApi(@PathVariable Integer id,
                             Model model, Authentication auth, HttpSession session) {

        boolean apiExists = apiRepository.existsById(id);
        if (apiExists) {

            List<Documentacion> docs = documentacionRepository.findByApi_IdApiOrderByFechaCreacionDesc(id);
            model.addAttribute("docs", docs);
        }

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "general/docs/documentacion";
    }


    @GetMapping("/{id}/test")
    @PreAuthorize("isAuthenticated()")
    public String testApi(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {
        Optional<Api> api = apiRepository.findById(id);
        if (api.isPresent()) {
            model.addAttribute("api", api.get());
        }

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/sandbox";
    }
    // Endpoint de Versiones de Api específica
    @GetMapping("/{id}/versiones")
    @ResponseBody
    public List<VersionApi> obtenerVersiones(@PathVariable Integer id) {
        return versionApiRepository.findByApi_IdApi(id);
//        return versionApiRepository.findAll();
    }

}
