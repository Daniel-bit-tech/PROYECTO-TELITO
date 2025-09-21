package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.*;
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
                            Model model, Authentication auth) {
        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: "+dominios + " \nTags: " + tags);

        List<Api> apis = apiRepository.findByFilters(nombre, selDominios, selTags);
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());
        }

        if (auth != null && auth.isAuthenticated()) {
            String correo = auth.getName();
            Usuario usuario = usuarioRepository.findByCorreo(correo);
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
//    @PreAuthorize("hasAnyRole('DEV','SADMIN','QA','PO')")
    @PreAuthorize("isAuthenticated()")
    public String detalleApi(@PathVariable Integer id,
                             @RequestParam(value = "fecha",required = false) String fecha,
                             Model model, Authentication auth) {

        System.out.println("\n\n\n DOCS \n");

        boolean apiExists = apiRepository.existsById(id);
        if (apiExists) {

            List<Documentacion> docs = documentacionRepository.findByApi_IdApiOrderByFechaCreacionDesc(id);
            model.addAttribute("docs", docs);
        }


        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("fecha", fecha);


        return "desarrollador/documentacion";
    }


    @GetMapping("/{id}/test")
    @PreAuthorize("isAuthenticated()")
    public String testApi(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Api> api = apiRepository.findById(id);
        if (api.isPresent()) {
            model.addAttribute("api", api.get());

        }

        return "desarrollador/sandbox";
    }



}
