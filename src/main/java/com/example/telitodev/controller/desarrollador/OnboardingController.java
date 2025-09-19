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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

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

    /**
     * Endpoint REST para obtener APIs disponibles (consumido por JavaScript)
     */
    @GetMapping("/api/onboarding/apis")
    @ResponseBody
    public ResponseEntity<List<ApiResponse>> getApisDisponibles(Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).build();
            }

            List<ApiResponse> apisDisponibles = onboardingService.obtenerApisDisponibles();
            return ResponseEntity.ok(apisDisponibles);
            
        } catch (Exception e) {
            System.err.println("Error al obtener APIs disponibles: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint REST para obtener credenciales del usuario (consumido por JavaScript)
     */
    @GetMapping("/api/onboarding/mis-credenciales")
    @ResponseBody
    public ResponseEntity<List<CredencialApiResponse>> getMisCredenciales(Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).build();
            }

            List<CredencialApiResponse> misCredenciales = onboardingService.obtenerCredencialesUsuario(usuario.getDni());
            return ResponseEntity.ok(misCredenciales);
            
        } catch (Exception e) {
            System.err.println("Error al obtener credenciales del usuario: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint REST para revocar una credencial (consumido por JavaScript)
     */
    @DeleteMapping("/api/onboarding/credenciales/{credencialId}")
    @ResponseBody
    public ResponseEntity<?> revocarCredencial(@PathVariable Integer credencialId, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            
            if (usuario == null) {
                return ResponseEntity.status(401).body("{\"error\":\"Usuario no encontrado\"}");
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).body("{\"error\":\"No tienes permisos para realizar esta acción\"}");
            }

            // Revocar la credencial usando el servicio
            onboardingService.revocarCredencial(credencialId, usuario.getDni());
            
            return ResponseEntity.ok("{\"message\":\"Credencial revocada exitosamente\"}");
            
        } catch (RuntimeException e) {
            System.err.println("Error al revocar credencial: " + e.getMessage());
            return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Error interno al revocar credencial: " + e.getMessage());
            return ResponseEntity.status(500).body("{\"error\":\"Error interno del servidor\"}");
        }
    }
}
