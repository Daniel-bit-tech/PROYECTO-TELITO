package com.example.telitodev.controller.admin;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/home")
    public String showAdminHome(Model model, Authentication authentication) {
        // Obtener información completa del usuario para pasarla al developer view
        String correo = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
        
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
        }
        
        return "redirect:/dev/home";
    }

    @GetMapping("/usuarios")
    public String showUsuarios(Model model, Authentication authentication) {
        // Obtener todos los usuarios de la base de datos
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        model.addAttribute("usuario", authentication.getName());
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @GetMapping("/perfil")
    public String showPerfil(Model model, Authentication authentication) {
        System.out.println("=== DEBUG PERFIL ADMIN ===");
        System.out.println("Authentication name: " + authentication.getName());
        
        // Obtener información del usuario autenticado
        String correo = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado: " + usuario.getNombre() + " " + usuario.getApellidoPaterno());
            System.out.println("Rol: " + usuario.getRol().getNombreRol());
            System.out.println("Fecha registro: " + usuario.getFechaRegistro());
            model.addAttribute("usuario", usuario);
        } else {
            System.out.println("Usuario NO encontrado para correo: " + correo);
            // Intenta sin filtrar por estado
            Usuario usuarioSinEstado = usuarioRepository.findByCorreo(correo);
            if (usuarioSinEstado != null) {
                System.out.println("Usuario encontrado sin filtro estado: " + usuarioSinEstado.getNombre());
                System.out.println("Estado del usuario: " + usuarioSinEstado.getEstado());
            }
        }
        
        System.out.println("Retornando vista: admin/perfil-admin (vista específica para admin)");
        return "admin/perfil-admin";
    }

    @GetMapping("/reportes")
    public String showReportes(Model model, Authentication authentication) {
        model.addAttribute("usuario", authentication.getName());
        return "admin/reportes";
    }

    @GetMapping("/configuracion")
    public String showConfiguracion(Model model, Authentication authentication) {
        model.addAttribute("usuario", authentication.getName());
        return "admin/configuracion";
    }

    @GetMapping("/perfil-test")
    public String perfilTest() {
        System.out.println("ENTRANDO A PERFIL TEST");
        return "admin/perfil-test";
    }
}