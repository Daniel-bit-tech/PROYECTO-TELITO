package com.example.telitodev.controller;

import com.example.telitodev.entity.Rol;
import com.example.telitodev.entity.TokenConfirmacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.RolRepository;
import com.example.telitodev.repository.TokenConfirmacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Mostrar formulario de confirmación de cuenta
     * Combina: Tu lógica de validación previa + template path de tu versión
     */
    @GetMapping
    public String mostrarFormularioConfirmacion(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String token,
            Model model) {

        System.out.println("=== MOSTRAR FORMULARIO CONFIRMACIÓN ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);

        // Tu lógica de validación previa mejorada
        if (email != null && token != null && !email.trim().isEmpty() && !token.trim().isEmpty()) {
            try {
                Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository
                        .findByTokenAndEmailIgnoreCase(token.trim(), email.trim());

                if (tokenOpt.isPresent()) {
                    TokenConfirmacion tokenConfirmacion = tokenOpt.get();

                    // Verificar que no esté expirado ni usado
                    if (!tokenConfirmacion.estaExpirado() && !tokenConfirmacion.estaUsado()) {
                        Map<String, Object> tokenInfo = new HashMap<>();
                        tokenInfo.put("email", tokenConfirmacion.getEmail());
                        tokenInfo.put("nombre", tokenConfirmacion.getNombreTemporal() + " " +
                                tokenConfirmacion.getApellidoPaternoTemporal());
                        tokenInfo.put("fechaExpiracion", tokenConfirmacion.getFechaExpiracion());

                        model.addAttribute("tokenInfo", tokenInfo);
                        model.addAttribute("email", email.trim());
                        model.addAttribute("token", token.trim());

                        System.out.println("✅ Token válido encontrado para: " + tokenConfirmacion.getEmail());
                    } else if (tokenConfirmacion.estaExpirado()) {
                        model.addAttribute("error", "El código de confirmación ha expirado. Solicita uno nuevo.");
                        System.out.println("❌ Token expirado para: " + email);
                    } else {
                        model.addAttribute("error", "Este código de confirmación ya ha sido utilizado.");
                        System.out.println("❌ Token ya usado para: " + email);
                    }
                } else {
                    model.addAttribute("error", "Código de confirmación inválido o email incorrecto.");
                    System.out.println("❌ Token no encontrado para email: " + email);
                }
            } catch (Exception e) {
                System.err.println("❌ Error buscando token: " + e.getMessage());
                model.addAttribute("error", "Error verificando el código. Intenta nuevamente.");
            }
        } else {
            // Parámetros para formulario vacío
            model.addAttribute("email", email != null ? email : "");
            model.addAttribute("token", token != null ? token : "");
        }

        return "confirmar-cuenta"; // Tu template path
    }

    /**
     * Procesar confirmación de cuenta
     * Combina: Tu lógica + validaciones de duplicados + generación de alias + email de bienvenida
     */
    @PostMapping
    public String procesarConfirmacion(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== PROCESANDO CONFIRMACIÓN DE CUENTA ===");
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        System.out.println("Password length: " + (password != null ? password.length() : "NULL"));

        try {
            // Validaciones básicas (combinadas de ambas versiones)
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email es requerido");
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
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository
                    .findByTokenAndEmailIgnoreCase(token, email);

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

            // NUEVA FUNCIONALIDAD: Verificar duplicados (de tu amigo)
            if (usuarioRepository.existsById(tokenConfirmacion.getDniUsuario())) {
                // Usar tu método o el de tu amigo según lo que tengas
                tokenConfirmacion.setUsado(true);
                tokenConfirmacionRepository.save(tokenConfirmacion);
                // O usar: tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());

                model.addAttribute("error", "Ya existe un usuario con este DNI");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            if (usuarioRepository.findByCorreo(tokenConfirmacion.getEmail()) != null) {
                tokenConfirmacion.setUsado(true);
                tokenConfirmacionRepository.save(tokenConfirmacion);
                // O usar: tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());

                model.addAttribute("error", "Ya existe un usuario con este email");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Obtener rol (tu lógica mejorada con fallback)
            Rol rol;
            try {
                Integer idRol = tokenConfirmacion.getIdRolTemporal();
                Optional<Rol> rolOpt = rolRepository.findById(idRol);

                if (rolOpt.isPresent()) {
                    rol = rolOpt.get();
                } else {
                    // Tu fallback a DEVELOPER
                    List<Rol> rolesDev = rolRepository.findAll().stream()
                            .filter(r -> "DEVELOPER".equals(r.getNombreRol()))
                            .collect(java.util.stream.Collectors.toList());

                    if (!rolesDev.isEmpty()) {
                        rol = rolesDev.get(0);
                        System.out.println("⚠ Usando rol DEVELOPER como fallback (ID: " + rol.getIdRol() + ")");
                    } else {
                        throw new RuntimeException("No se encontró un rol válido");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo rol: " + e.getMessage());
                model.addAttribute("error", "Error interno del servidor. Contacta al administrador.");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Crear usuario (combinando ambas versiones)
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

            // Tu configuración de acceso interno
            nuevoUsuario.setTipoAcceso(Usuario.TipoAcceso.interno);

            // NUEVA FUNCIONALIDAD: Generación automática de alias (de tu amigo)
            String alias = tokenConfirmacion.getNombreTemporal().toLowerCase() +
                    "." + tokenConfirmacion.getApellidoPaternoTemporal().toLowerCase();
            nuevoUsuario.setAlias(alias);

            // Guardar usuario
            Usuario usuarioCreado = usuarioRepository.save(nuevoUsuario);
            System.out.println("✅ Usuario creado exitosamente: " + usuarioCreado.getCorreo());

            // Marcar token como usado
            tokenConfirmacion.setUsado(true);
            tokenConfirmacionRepository.save(tokenConfirmacion);

            // NUEVA FUNCIONALIDAD: Invalidar otros tokens (de tu amigo) - SI LOS MÉTODOS EXISTEN
            try {
                tokenConfirmacionRepository.invalidarTokensPorEmail(tokenConfirmacion.getEmail());
                tokenConfirmacionRepository.invalidarTokensPorDni(tokenConfirmacion.getDniUsuario());
                System.out.println("✅ Tokens relacionados invalidados");
            } catch (Exception e) {
                System.out.println("⚠ No se pudieron invalidar otros tokens: " + e.getMessage());
                // No es crítico, continúa
            }

            // NUEVA FUNCIONALIDAD: Email de bienvenida (de tu amigo)
            try {
                emailService.enviarEmailBienvenida(
                        usuarioCreado.getCorreo(),
                        usuarioCreado.getNombre(),
                        usuarioCreado.getDni(),
                        usuarioCreado.getRol().getNombreRol()
                );
                System.out.println("✅ Email de bienvenida enviado");
            } catch (Exception e) {
                System.err.println("⚠ Error enviando email de bienvenida: " + e.getMessage());
                // No es crítico, el usuario ya fue creado
            }

            // Mensaje de éxito mejorado
            redirectAttributes.addFlashAttribute("success",
                    "¡Cuenta confirmada exitosamente! Ya puedes iniciar sesión con el email: " +
                            usuarioCreado.getCorreo());

            System.out.println("✅ Confirmación completada exitosamente");
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