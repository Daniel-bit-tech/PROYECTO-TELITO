package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Proyecto;
import com.example.telitodev.entity.ProyectoHasApi;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ProyectoRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProyectosController {

    final UsuarioRepository usuarioRepository;
    final ProyectoRepository proyectoRepository;
    public ProyectosController(UsuarioRepository usuarioRepository, ProyectoRepository proyectoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @GetMapping("/proyectos")
    public String showsand(@RequestParam(value = "filter", required = false) String filtro,
                           Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        List<Proyecto> listaProyectos = null;
        if (filtro != null && filtro.equals("activos")) {
            listaProyectos = proyectoRepository.findByActivoAndOrganizacion_Usuarios_Dni(true, usuario.getDni());
        } else if (filtro != null && filtro.equals("ocultos")) {
            listaProyectos = proyectoRepository.findByPublicoAndOrganizacion_Usuarios_Dni(true, usuario.getDni());
        } else {
            listaProyectos = proyectoRepository.findByOrganizacion_Usuarios_Dni(usuario.getDni());
        }

        for (Proyecto proyecto : listaProyectos) {
            for (ProyectoHasApi pha : proyecto.getProyectoHasApis()) {
                System.out.println(proyecto.getNombre()+" con: "+pha.getApi().getNombre()+" y "+pha.getEntorno().getNombre());
            }

        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("filtro", filtro);

        model.addAttribute("usuario", usuario);

        return "desarrollador/proyectos";
    }

}
