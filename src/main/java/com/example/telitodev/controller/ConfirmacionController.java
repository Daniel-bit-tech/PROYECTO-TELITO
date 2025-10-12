package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.entity.TokenConfirmacion;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import com.example.telitodev.repository.TokenConfirmacionRepository;
import com.example.telitodev.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/confirmar-cuenta")
public class ConfirmacionController {

    @Autowired
    private TokenConfirmacionRepository tokenConfirmacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Mostrar página de confirmación de cuenta
     */
    @GetMapping
    public String mostrarFormularioConfirmacion(@RequestParam(required = false) String email,
                                               @RequestParam(required = false) String token,
                                               Model model) {
        System.out.println("=== MOSTRANDO FORMULARIO DE CONFIRMACIÓN ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("token", token != null ? token : "");
        
        return "confirmar-cuenta";
    }

    /**
     * Procesar confirmación de cuenta
     */
    @PostMapping
    public String procesarConfirmacion(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("=== PROCESANDO CONFIRMACIÓN DE CUENTA ===");
            System.out.println("Email: " + email);
            System.out.println("Token: " + token);
            System.out.println("Password length: " + (password != null ? password.length() : "NULL"));
            System.out.println("ConfirmPassword length: " + (confirmPassword != null ? confirmPassword.length() : "NULL"));

            // Validaciones básicas
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email es requerido");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            
            if (token == null || token.trim().isEmpty()) {
                model.addAttribute("error", "Código de confirmación es requerido");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña es requerida");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                model.addAttribute("error", "Debes confirmar la contraseña");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Limpiar datos
            email = email.trim().toLowerCase();
            token = token.trim();
            password = password.trim();
            confirmPassword = confirmPassword.trim();

            // Validar contraseñas
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            if (password.length() < 8) {
                model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Buscar token
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository.findByTokenAndEmailIgnoreCase(token, email);
            if (!tokenOpt.isPresent()) {
                model.addAttribute("error", "Código de confirmación inválido o email incorrecto");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            TokenConfirmacion tokenConfirmacion = tokenOpt.get();
            System.out.println("✅ Token encontrado para: " + tokenConfirmacion.getEmail());

            // Verificar que el token sea válido
            if (tokenConfirmacion.estaUsado()) {
                model.addAttribute("error", "Este código de confirmación ya ha sido utilizado");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            if (tokenConfirmacion.estaExpirado()) {
                model.addAttribute("error", "El código de confirmación ha expirado. Solicita una nueva cuenta.");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Verificar que el usuario no exista
            if (usuarioRepository.existsById(tokenConfirmacion.getDniUsuario())) {
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                model.addAttribute("error", "Ya existe un usuario con este DNI");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            if (usuarioRepository.findByCorreo(tokenConfirmacion.getEmail()) != null) {
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                model.addAttribute("error", "Ya existe un usuario con este email");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Obtener rol
            Optional<Rol> rolOpt = rolRepository.findById(tokenConfirmacion.getIdRolTemporal());
            if (!rolOpt.isPresent()) {
                model.addAttribute("error", "Error interno: rol no encontrado");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Crear usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDni(tokenConfirmacion.getDniUsuario());
            nuevoUsuario.setNombre(tokenConfirmacion.getNombreTemporal());
            nuevoUsuario.setApellidoPaterno(tokenConfirmacion.getApellidoPaternoTemporal());
            nuevoUsuario.setApellidoMaterno(tokenConfirmacion.getApellidoMaternoTemporal());
            nuevoUsuario.setCorreo(tokenConfirmacion.getEmail());
            nuevoUsuario.setContrasena(passwordEncoder.encode(password));
            nuevoUsuario.setRol(rolOpt.get());
            nuevoUsuario.setEstado(true);
            nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));

            // Generar alias básico
            String alias = tokenConfirmacion.getNombreTemporal().toLowerCase() + 
                          "." + tokenConfirmacion.getApellidoPaternoTemporal().toLowerCase();
            nuevoUsuario.setAlias(alias);

            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            System.out.println("✅ Usuario creado: " + usuarioGuardado.getDni());

            // Marcar token como usado
            tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());

            // Invalidar otros tokens del mismo email/DNI
            tokenConfirmacionRepository.invalidarTokensPorEmail(tokenConfirmacion.getEmail());
            tokenConfirmacionRepository.invalidarTokensPorDni(tokenConfirmacion.getDniUsuario());

            // Enviar email de bienvenida
            emailService.enviarEmailBienvenida(
                usuarioGuardado.getCorreo(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getDni(),
                usuarioGuardado.getRol().getNombreRol()
            );

            System.out.println("✅ Confirmación completada exitosamente");
            redirectAttributes.addFlashAttribute("success", 
                "¡Cuenta confirmada exitosamente! Ya puedes iniciar sesión con el email: " + 
                usuarioGuardado.getCorreo());
            
            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("❌ Error confirmando cuenta: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "confirmar-cuenta";
        }
    }
}