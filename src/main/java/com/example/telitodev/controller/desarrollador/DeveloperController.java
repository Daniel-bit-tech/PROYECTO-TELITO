package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.CredencialApiRepository;
import com.example.telitodev.repository.NotificacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasAnyRole('DEV', 'SADMIN')")
public class DeveloperController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    public DeveloperController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
    }


    @GetMapping("/home")
    public String showDeveloperView(Model model, Authentication auth) {
        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        Integer noti = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);

        model.addAttribute("usuario", usuario);
        model.addAttribute("credActivas", NCredenciales);
        model.addAttribute("notificaciones", noti);



        return "desarrollador/developer";
    }

    @GetMapping("/catalogo")
    public String developerDashboard(Model model, Authentication authentication) {
        model.addAttribute("smg", authentication.getName());





        return "desarrollador/apis";
    }
}
