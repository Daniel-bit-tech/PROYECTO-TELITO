package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Optional;

@Controller
public class LoginController {

    final UsuarioRepository usuarioRepository;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model, HttpSession session, Authentication auth) {

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return "redirect:/home";
        }
        
        if (error != null) {
            String errorMessage = "Credenciales inválidas";
            
            System.out.println("🔍 DEBUG LOGIN ERROR:");
            System.out.println("   - Error parameter presente: " + error);
            
            // Verificar si es error por usuario desactivado en tiempo real
            if ("disabled".equals(error)) {
                errorMessage = "Su cuenta ha sido desactivada. Comuníquese con el administrador.";
                System.out.println("   - ✅ Usuario desactivado en tiempo real - aplicando mensaje específico");
                model.addAttribute("error", errorMessage);
                return "sesion/login";
            }
            
            // Obtener la excepción de autenticación de la sesión
            Exception exception = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            
            if (exception != null) {
                System.out.println("   - Excepción encontrada: " + exception.getClass().getSimpleName());
                System.out.println("   - Mensaje completo: '" + exception.getMessage() + "'");
                System.out.println("   - Tipo exacto: " + exception.getClass().getName());
                
                // Verificar si es nuestra excepción personalizada o contiene el mensaje específico
                if (exception.getClass().getName().contains("UsuarioDesactivadoException") ||
                    (exception.getMessage() != null && exception.getMessage().contains("Usuario inactivo"))) {
                    errorMessage = "Usuario inactivo. Comuníquese con el administrador.";
                    System.out.println("   - ✅ Aplicando mensaje de usuario desactivado");
                } else {
                    System.out.println("   - ❌ Aplicando mensaje de credenciales inválidas");
                }
            } else {
                System.out.println("   - ❌ No se encontró excepción en la sesión");
            }
            
            System.out.println("   - Mensaje final: '" + errorMessage + "'");
            model.addAttribute("error", errorMessage);
        }
        
        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada con éxito");
        }
        
        return "sesion/login";
    }



    @GetMapping("/home")
    public String home(Authentication authentication) {
        System.out.println("=== LoginController.home() llamado ===");
        
        if (authentication == null) {
            System.out.println("Sin autenticación, redirigiendo a login");
            return "redirect:/login";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        System.out.println("Usuario autenticado: " + authentication.getName());
        System.out.println("Autoridades: " + authorities);

        for (GrantedAuthority authority : authorities) {
            System.out.println("Procesando autoridad: " + authority.getAuthority());
            switch (authority.getAuthority()) {
                case "ROLE_SUPERADMIN":
                    System.out.println("Redirigiendo SUPERADMIN a /admin/home");
                    return "redirect:/admin/home";
                case "ROLE_DEV":
                    System.out.println("Redirigiendo DEV a /dev/home");
                    return "redirect:/dev/home";
                case "ROLE_QA":
                    System.out.println("Redirigiendo QA a /qa/home");
                    return "redirect:/qa/home";
                case "ROLE_PO":
                    System.out.println("Redirigiendo PO a /po/home");
                    return "redirect:/po/home";
                default:
                    System.out.println("Autoridad no reconocida: " + authority.getAuthority());
                    break;
            }
        }

        System.out.println("No se encontró autoridad válida, redirigiendo a login");
        return "redirect:/login";
    }
}

