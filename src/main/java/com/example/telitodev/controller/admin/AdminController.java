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
        return "redirect:/dev/home";
    }

    @GetMapping("/usuarios")
    public String showUsuarios() {
        // Redirigir al nuevo sistema de gestión de usuarios
        return "redirect:/admin/gestion-usuarios";
    }
    
    @GetMapping("/perfil")
    public String showPerfil(Model model, Authentication authentication) {
        try {
            String correo = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
            
            if (usuarioOpt.isPresent()) {
                model.addAttribute("usuario", usuarioOpt.get());
            } else {
                model.addAttribute("usuario", createMockAdmin(correo));
            }
            
            return "admin/perfil-admin";
        } catch (Exception e) {
            model.addAttribute("usuario", createMockAdmin(authentication.getName()));
            return "admin/perfil-admin";
        }
    }

    @GetMapping("/reportes")
    public String showReportes(Model model, Authentication authentication) {
        model.addAttribute("usuario", createMockAdmin(authentication.getName()));
        return "admin/reportes";
    }

    @GetMapping("/configuracion")
    public String showConfiguracion(Model model, Authentication authentication) {
        model.addAttribute("usuario", createMockAdmin(authentication.getName()));
        return "admin/configuracion";
    }
    
    @GetMapping("/test")
    public String test(Model model, Authentication authentication) {
        System.out.println("=== ADMIN TEST ===");
        System.out.println("Usuario: " + authentication.getName());
        
        // Probar conexión a BD
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            model.addAttribute("totalUsuarios", usuarios.size());
            model.addAttribute("testResult", "Conexión a BD exitosa. Usuarios encontrados: " + usuarios.size());
        } catch (Exception e) {
            model.addAttribute("testResult", "Error de conexión a BD: " + e.getMessage());
        }
        
        return "admin/test";
    }
    
    // Método auxiliar para crear un usuario mock sin depender de la base de datos
    private Object createMockAdmin(String email) {
        return new Object() {
            public String getNombre() { return "Admin"; }
            public String getCorreo() { return email; }
            public Object getRol() { 
                return new Object() {
                    public String getNombreRol() { return "SUPERADMIN"; }
                    public String getDescripcion() { return "Super Administrador"; }
                };
            }
        };
    }
}