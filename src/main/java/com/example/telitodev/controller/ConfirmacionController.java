package com.example.telitodev.controller;

import com.example.telitodev.entity.Rol;
import com.example.telitodev.entity.TokenConfirmacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.RolRepository;
import com.example.telitodev.repository.TokenConfirmacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ConfirmacionController {

    @Autowired
    private TokenConfirmacionRepository tokenConfirmacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Mostrar formulario de confirmación de cuenta
     */
    @GetMapping("/confirmar-cuenta")
    public String mostrarFormularioConfirmacion(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String token,
            Model model) {
        
        System.out.println("=== MOSTRAR FORMULARIO CONFIRMACIÓN ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        
        // Si se proporcionan email y token, buscar información
        if (email != null && token != null && !email.trim().isEmpty() && !token.trim().isEmpty()) {
            try {
                Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository
                    .findByTokenAndEmailIgnoreCase(token.trim(), email.trim());
                
                if (tokenOpt.isPresent()) {
                    TokenConfirmacion tokenConfirmacion = tokenOpt.get();
                    
                    // Verificar que no esté expirado
                    if (!tokenConfirmacion.estaExpirado()) {
                        Map<String, Object> tokenInfo = new HashMap<>();
                        tokenInfo.put("email", tokenConfirmacion.getEmail());
                        tokenInfo.put("nombre", tokenConfirmacion.getNombreTemporal() + " " + 
                                     tokenConfirmacion.getApellidoPaternoTemporal());
                        tokenInfo.put("fechaExpiracion", tokenConfirmacion.getFechaExpiracion());
                        
                        model.addAttribute("tokenInfo", tokenInfo);
                        model.addAttribute("email", email.trim());
                        model.addAttribute("token", token.trim());
                        
                        System.out.println("✅ Token válido encontrado para: " + tokenConfirmacion.getEmail());
                    } else {
                        model.addAttribute("error", "El código de confirmación ha expirado. Solicita uno nuevo.");
                        System.out.println("❌ Token expirado para: " + email);
                    }
                } else {
                    model.addAttribute("error", "Código de confirmación inválido o email incorrecto.");
                    System.out.println("❌ Token no encontrado para email: " + email);
                }
            } catch (Exception e) {
                System.err.println("❌ Error buscando token: " + e.getMessage());
                model.addAttribute("error", "Error verificando el código. Intenta nuevamente.");
            }
        }
        
        return "sesion/confirmar-cuenta";
    }

    /**
     * Procesar confirmación de cuenta
     */
    @PostMapping("/confirmar-cuenta")
    public String procesarConfirmacion(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== PROCESANDO CONFIRMACIÓN ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        
        try {
            // Validaciones básicas
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email es requerido");
                return "sesion/confirmar-cuenta";
            }
            
            if (token == null || token.trim().isEmpty()) {
                model.addAttribute("error", "Código de confirmación es requerido");
                model.addAttribute("email", email);
                return "sesion/confirmar-cuenta";
            }
            
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña es requerida");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            if (confirmPassword == null || !password.equals(confirmPassword)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            if (password.length() < 8) {
                model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            // Limpiar datos
            email = email.trim().toLowerCase();
            token = token.trim();
            
            // Buscar token
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository
                .findByTokenAndEmailIgnoreCase(token, email);
            
            if (!tokenOpt.isPresent()) {
                model.addAttribute("error", "Código de confirmación inválido o email incorrecto");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            TokenConfirmacion tokenConfirmacion = tokenOpt.get();
            
            // Verificar expiración
            if (tokenConfirmacion.estaExpirado()) {
                model.addAttribute("error", "El código de confirmación ha expirado");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            // Verificar que no esté usado
            if (tokenConfirmacion.getUsado()) {
                model.addAttribute("error", "Este código ya ha sido utilizado");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            // Obtener rol - manejo de duplicados
            Rol rol;
            try {
                Integer idRol = tokenConfirmacion.getIdRolTemporal();
                Optional<Rol> rolOpt = rolRepository.findById(idRol);
                
                if (rolOpt.isPresent()) {
                    rol = rolOpt.get();
                } else {
                    // Fallback: buscar rol DEVELOPER
                    List<Rol> rolesDev = rolRepository.findAll().stream()
                        .filter(r -> "DEVELOPER".equals(r.getNombreRol()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!rolesDev.isEmpty()) {
                        rol = rolesDev.get(0);
                        System.out.println("⚠️ Usando rol DEVELOPER como fallback (ID: " + rol.getIdRol() + ")");
                    } else {
                        throw new RuntimeException("No se encontró un rol válido");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo rol: " + e.getMessage());
                model.addAttribute("error", "Error interno del servidor. Contacta al administrador.");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "sesion/confirmar-cuenta";
            }
            
            // Crear usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDni(tokenConfirmacion.getDniUsuario());
            nuevoUsuario.setNombre(tokenConfirmacion.getNombreTemporal());
            nuevoUsuario.setApellidoPaterno(tokenConfirmacion.getApellidoPaternoTemporal());
            nuevoUsuario.setApellidoMaterno(tokenConfirmacion.getApellidoMaternoTemporal());
            nuevoUsuario.setCorreo(tokenConfirmacion.getEmail());
            nuevoUsuario.setContrasena(passwordEncoder.encode(password));
            nuevoUsuario.setRol(rol);
            nuevoUsuario.setEstado(true);
            nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));
            
            // Configurar como usuario interno por defecto
            nuevoUsuario.setTipoAcceso(Usuario.TipoAcceso.interno);
            
            // Guardar usuario
            Usuario usuarioCreado = usuarioRepository.save(nuevoUsuario);
            System.out.println("✅ Usuario creado exitosamente: " + usuarioCreado.getCorreo());
            
            // Marcar token como usado
            tokenConfirmacion.setUsado(true);
            tokenConfirmacionRepository.save(tokenConfirmacion);
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success", 
                "¡Cuenta confirmada exitosamente! Ya puedes iniciar sesión.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            System.err.println("❌ Error confirmando cuenta: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "sesion/confirmar-cuenta";
        }
    }
}