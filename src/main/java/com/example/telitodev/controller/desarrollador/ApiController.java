package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.UsuarioRepository;
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

    public ApiController(ApiRepository apiRepository, UsuarioRepository usuarioRepository) {
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
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

        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        model.addAttribute("usuario", usuario);

        model.addAttribute("apis", apis);
        model.addAttribute("tags", selTags);
        model.addAttribute("dominios", selDominios);


        return "desarrollador/apis";
    }

    @GetMapping("/{id}")
    public String detalleApi(@PathVariable Integer id, Model model, Authentication auth) {

        Optional<Api> api = apiRepository.findById(id);


        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        model.addAttribute("usuario", usuario);



        return "desarrollador/apis/" + id;
    }




}
