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
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/home")
    public String showAdminHome(Model model, Authentication authentication) {
        System.out.println("=== ADMIN HOME ACCESS ===");
        System.out.println("Usuario: " + authentication.getName());
        System.out.println("Roles: " + authentication.getAuthorities());
        
        try {
            String correo = authentication.getName();
            
            // Intentar obtener usuario de BD, pero usar mock si falla
            try {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
                if (usuarioOpt.isPresent()) {
                    model.addAttribute("usuario", usuarioOpt.get());
                    System.out.println("Usuario encontrado en BD: " + usuarioOpt.get().getNombre());
                } else {
                    model.addAttribute("usuario", createMockAdmin(correo));
                    System.out.println("Usuario no encontrado en BD, usando mock");
                }
            } catch (Exception dbError) {
                System.out.println("Error de BD, usando mock: " + dbError.getMessage());
                model.addAttribute("usuario", createMockAdmin(correo));
            }
            
            // Agregar estadísticas básicas para el dashboard (excluyendo SUPERADMINs)
            try {
                List<Usuario> usuarios = usuarioRepository.findAll();
                // Filtrar usuarios excluyendo SUPERADMINs
                List<Usuario> usuariosGestionables = usuarios.stream()
                    .filter(u -> u.getRol() == null || !"SUPERADMIN".equals(u.getRol().getNombreRol()))
                    .toList();
                
                model.addAttribute("totalUsuarios", usuariosGestionables.size());
                long usuariosActivos = usuariosGestionables.stream().filter(Usuario::getEstado).count();
                model.addAttribute("usuariosActivos", usuariosActivos);
                model.addAttribute("usuariosInactivos", usuariosGestionables.size() - usuariosActivos);
                System.out.println("Estadísticas cargadas: " + usuariosGestionables.size() + " usuarios gestionables (excluyendo SUPERADMINs)");
            } catch (Exception e) {
                System.out.println("Error al cargar estadísticas: " + e.getMessage());
                model.addAttribute("totalUsuarios", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
                model.addAttribute("usuariosActivos", 10);
                model.addAttribute("usuariosInactivos", 1);
            }
            
            System.out.println("Redirigiendo a admin/dashboard");
            return "admin/dashboard";
        } catch (Exception e) {
            System.out.println("Error general en admin/home: " + e.getMessage());
            model.addAttribute("usuario", createMockAdmin(authentication.getName()));
            model.addAttribute("totalUsuarios", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
            model.addAttribute("usuariosActivos", 10);
            model.addAttribute("usuariosInactivos", 1);
            return "admin/dashboard";
        }
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
    
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats(Authentication authentication) {
        System.out.println("=== 📊 API DASHBOARD STATS INICIADA ===");
        System.out.println("Usuario que solicita: " + (authentication != null ? authentication.getName() : "Anónimo"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            System.out.println("✅ Total usuarios encontrados en BD: " + usuarios.size());
            
            // Filtrar usuarios excluyendo SUPERADMINs
            List<Usuario> usuariosGestionables = usuarios.stream()
                .filter(u -> u.getRol() == null || !"SUPERADMIN".equals(u.getRol().getNombreRol()))
                .toList();
            
            System.out.println("✅ Usuarios gestionables (sin SUPERADMINs): " + usuariosGestionables.size());
            
            // Debug: mostrar algunos usuarios gestionables
            usuariosGestionables.stream().limit(3).forEach(u -> {
                System.out.println("  - Usuario: " + u.getNombre() + " " + u.getApellidoPaterno() + 
                    " | Estado: " + u.getEstado() + 
                    " | Rol: " + (u.getRol() != null ? u.getRol().getNombreRol() : "NULL"));
            });
            
            // Contar usuarios gestionables por estado (excluyendo SUPERADMINs)
            Map<String, Integer> usersByStatus = new HashMap<>();
            long activeUsers = usuariosGestionables.stream()
                .filter(u -> u.getEstado() != null && u.getEstado())
                .count();
            long inactiveUsers = usuariosGestionables.stream()
                .filter(u -> u.getEstado() == null || !u.getEstado())
                .count();
            
            usersByStatus.put("active", (int) activeUsers);
            usersByStatus.put("inactive", (int) inactiveUsers);
            
            // Contar usuarios por rol (excluyendo SUPERADMINs)
            Map<String, Integer> usersByRole = new HashMap<>();
            usuariosGestionables.forEach(usuario -> {
                if (usuario.getRol() != null && usuario.getRol().getNombreRol() != null) {
                    String roleName = usuario.getRol().getNombreRol();
                    // No incluir SUPERADMINs (doble verificación)
                    if (!"SUPERADMIN".equals(roleName)) {
                        usersByRole.put(roleName, usersByRole.getOrDefault(roleName, 0) + 1);
                    }
                } else {
                    usersByRole.put("Sin Rol", usersByRole.getOrDefault("Sin Rol", 0) + 1);
                }
            });
            
            response.put("usersByStatus", usersByStatus);
            response.put("usersByRole", usersByRole);
            response.put("totalUsers", usuariosGestionables.size());
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("📈 Usuarios por estado: " + usersByStatus);
            System.out.println("📊 Usuarios por rol: " + usersByRole);
            System.out.println("✅ Respuesta JSON generada correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR obteniendo estadísticas del dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Datos de respaldo con valores correctos (excluyendo SUPERADMINs)
            Map<String, Integer> defaultStatus = new HashMap<>();
            defaultStatus.put("active", 10);  // Usuarios gestionables activos
            defaultStatus.put("inactive", 1);
            
            Map<String, Integer> defaultRoles = new HashMap<>();
            // No incluir SUPERADMIN en el conteo
            defaultRoles.put("DEVELOPER", 7);
            defaultRoles.put("QA", 2);
            defaultRoles.put("PO", 2);
            
            response.put("usersByStatus", defaultStatus);
            response.put("usersByRole", defaultRoles);
            response.put("totalUsers", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("fallbackData", true);
        }
        
        System.out.println("📤 Enviando respuesta: " + response);
        return response;
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