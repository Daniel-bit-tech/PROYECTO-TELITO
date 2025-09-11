package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
                            Model model, Authentication auth) {
        String dominios = selDominios == null ? null : String.join(",", selDominios);
        String tags = selTags == null ? null : String.join(",", selTags);

        System.out.println("dominios: " + dominios);
        System.out.println("tags: " + tags);

        List<Api> apis = apiRepository.findByFilters(nombre, dominios, tags);
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());

        }

        if (auth != null && auth.isAuthenticated()) {
            String correo = auth.getName();
            Usuario usuario = usuarioRepository.findByCorreo(correo);
            model.addAttribute("usuario", usuario);
        }

        model.addAttribute("apis", apis);
        model.addAttribute("tags", selTags);
        model.addAttribute("dominios", selDominios);


        return "desarrollador/apis";
    }

    @GetMapping("/{id}/docs")
//    @PreAuthorize("hasAnyRole('DEV','SADMIN','QA','PO')")
    @PreAuthorize("isAuthenticated()")
    public String detalleApi(@PathVariable Integer id, Model model, Authentication auth) {

        Optional<Api> api = apiRepository.findById(id);
        if (api.isPresent()) {
            model.addAttribute("api", api.get());

            List<Documentacion> docs = documentacionRepository.findByApi_IdApi(id);
            model.addAttribute("docs", docs);
        }


        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        model.addAttribute("usuario", usuario);



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
