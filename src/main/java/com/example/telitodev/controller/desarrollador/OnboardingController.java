package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.OnboardingService;
import com.example.telitodev.dto.ApiResponse;
import com.example.telitodev.dto.CredencialApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class OnboardingController {

    final UsuarioRepository usuarioRepository;
    final OnboardingService onboardingService;

    public OnboardingController(UsuarioRepository usuarioRepository, OnboardingService onboardingService) {
        this.usuarioRepository = usuarioRepository;
        this.onboardingService = onboardingService;
    }

    @GetMapping("/onboarding")
    public String showonb(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Validar que el usuario tenga rol DEV
        if (usuario.getRol().getIdRol() != 2) {
            return "redirect:/access-denied"; // Redirigir si no es DEV
        }

        model.addAttribute("usuario", usuario);

        // Obtener datos adicionales para la vista
        try {
            System.out.println("=== DEBUG CONTROLADOR: Cargando datos para onboarding ===");
            List<ApiResponse> apisDisponibles = onboardingService.obtenerApisDisponibles();
            System.out.println("APIs disponibles cargadas: " + apisDisponibles.size());
            for (ApiResponse api : apisDisponibles) {
                System.out.println("API: id=" + api.getIdApi() + ", nombre=" + api.getNombre());
            }
            
            List<CredencialApiResponse> misCredenciales = onboardingService.obtenerCredencialesUsuario(usuario.getDni());
            System.out.println("Credenciales del usuario: " + misCredenciales.size());
            
            model.addAttribute("apisDisponibles", apisDisponibles);
            model.addAttribute("misCredenciales", misCredenciales);
            model.addAttribute("totalCredenciales", misCredenciales.size());
            
            long credencialesActivas = misCredenciales.stream()
                    .filter(CredencialApiResponse::getEstado)
                    .count();
            model.addAttribute("credencialesActivas", credencialesActivas);
            
        } catch (Exception e) {
            // En caso de error, continuar con la vista pero sin los datos adicionales
            model.addAttribute("error", "Error al cargar datos del onboarding");
        }

        return "desarrollador/onboarding";
    }
}
