package com.example.telitodev.controller;

import com.example.telitodev.entity.Rol;
import com.example.telitodev.entity.TokenConfirmacion;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class RegisterController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private TokenConfirmacionRepository tokenConfirmacionRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "sesion/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidoPaterno") String apellidoPaterno,
            @RequestParam("apellidoMaterno") String apellidoMaterno,
            @RequestParam("dni") String dni,
            @RequestParam("correo") String correo,
            @RequestParam("contrasena") String contrasena,
            @RequestParam("confirmarContrasena") String confirmarContrasena,
            @RequestParam("rol") String rol,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== PROCESANDO REGISTRO ===");
            System.out.println("Nombre: " + nombre);
            System.out.println("Email: " + correo);
            System.out.println("DNI: " + dni);
            System.out.println("Rol: " + rol);
            
            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty()) {
                model.addAttribute("error", "El nombre es requerido");
                return "sesion/register";
            }
            
            if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty()) {
                model.addAttribute("error", "El apellido paterno es requerido");
                return "sesion/register";
            }
            
            if (dni == null || dni.trim().length() != 8) {
                model.addAttribute("error", "El DNI debe tener exactamente 8 dígitos");
                return "sesion/register";
            }
            
            if (correo == null || !correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                model.addAttribute("error", "El formato del email no es válido");
                return "sesion/register";
            }
            
            if (contrasena == null || contrasena.length() < 8) {
                model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                return "sesion/register";
            }
            
            if (!contrasena.equals(confirmarContrasena)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                return "sesion/register";
            }
            
            // Verificar si el usuario ya existe
            if (usuarioRepository.existsById(dni)) {
                model.addAttribute("error", "Ya existe un usuario con ese DNI");
                return "sesion/register";
            }
            
            if (usuarioRepository.findByCorreo(correo.toLowerCase().trim()) != null) {
                model.addAttribute("error", "Ya existe un usuario con ese email");
                return "sesion/register";
            }
            
            // Determinar rol ID basado en la selección
            Integer idRol;
            if ("desarrollador".equals(rol)) {
                // Buscar rol DEVELOPER, si hay duplicados usar el primer resultado
                try {
                    List<Rol> rolesDev = rolRepository.findAll().stream()
                        .filter(r -> "DEVELOPER".equals(r.getNombreRol()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!rolesDev.isEmpty()) {
                        idRol = rolesDev.get(0).getIdRol(); // Usar el primer DEVELOPER encontrado
                        System.out.println("✅ Rol DEVELOPER encontrado con ID: " + idRol);
                    } else {
                        idRol = 2; // Fallback al rol DEV
                        System.out.println("⚠️ No se encontró rol DEVELOPER, usando DEV (ID: 2)");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error buscando rol DEVELOPER: " + e.getMessage());
                    idRol = 2; // Fallback seguro
                }
            } else if ("qa".equals(rol)) {
                // Buscar rol QA de forma similar
                try {
                    List<Rol> rolesQa = rolRepository.findAll().stream()
                        .filter(r -> "QA".equals(r.getNombreRol()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!rolesQa.isEmpty()) {
                        idRol = rolesQa.get(0).getIdRol();
                        System.out.println("✅ Rol QA encontrado con ID: " + idRol);
                    } else {
                        idRol = 3; // Fallback
                        System.out.println("⚠️ No se encontró rol QA, usando ID: 3");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error buscando rol QA: " + e.getMessage());
                    idRol = 3; // Fallback seguro
                }
            } else {
                model.addAttribute("error", "Rol no válido seleccionado");
                return "sesion/register";
            }
            
            // Verificar si ya existe un token válido
            LocalDateTime ahora = LocalDateTime.now();
            if (tokenConfirmacionRepository.existeTokenValidoPorEmail(correo.toLowerCase().trim(), ahora)) {
                model.addAttribute("error", "Ya existe un token de confirmación válido para este email. Revisa tu bandeja de entrada.");
                return "sesion/register";
            }
            
            if (tokenConfirmacionRepository.existeTokenValidoPorDni(dni, ahora)) {
                model.addAttribute("error", "Ya existe un token de confirmación válido para este DNI.");
                return "sesion/register";
            }
            
            // Generar token de 6 dígitos
            String token = generarToken6Digitos();
            System.out.println("Token generado: " + token);
            
            // Cifrar contraseña
            String contrasenaEncriptada = passwordEncoder.encode(contrasena);
            
            // Obtener IP del cliente
            String ipCliente = obtenerIpCliente(request);
            
            // Crear token de confirmación
            TokenConfirmacion tokenConfirmacion = new TokenConfirmacion(
                token, 
                correo.toLowerCase().trim(), 
                dni.trim(), 
                nombre.trim(), 
                apellidoPaterno.trim(), 
                apellidoMaterno != null ? apellidoMaterno.trim() : "", 
                contrasenaEncriptada, 
                idRol, 
                ipCliente
            );
            
            // Guardar token
            tokenConfirmacionRepository.save(tokenConfirmacion);
            System.out.println("✅ Token guardado en BD con ID: " + tokenConfirmacion.getId());
            
            // Enviar email
            boolean emailEnviado = emailService.enviarTokenConfirmacion(
                correo.toLowerCase().trim(), 
                nombre.trim(), 
                token, 
                tokenConfirmacion.getFechaExpiracion()
            );
            
            if (emailEnviado) {
                redirectAttributes.addFlashAttribute("success", 
                    "Registro exitoso. Se ha enviado un código de confirmación a tu email: " + correo);
            } else {
                System.err.println("❌ Error enviando email de confirmación");
                redirectAttributes.addFlashAttribute("warning", 
                    "Usuario registrado pero el email no pudo ser enviado. " +
                    "Contacta al administrador para activar tu cuenta.");
            }
            
            System.out.println("✅ Proceso de registro completado");
            return "redirect:/login";
            
        } catch (Exception e) {
            System.err.println("❌ Error en el registro: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            return "sesion/register";
        }
    }
    
    /**
     * Generar token de 6 dígitos
     */
    private String generarToken6Digitos() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Obtener IP del cliente
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

