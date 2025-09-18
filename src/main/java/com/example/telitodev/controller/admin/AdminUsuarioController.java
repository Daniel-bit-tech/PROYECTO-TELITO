package com.example.telitodev.controller.admin;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/gestion-usuarios")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Página principal de gestión de usuarios - Versión simplificada
     */
    @GetMapping
    public String index(Model model) {
        try {
            // Obtener datos básicos
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Rol> roles = rolRepository.findAll();

            // Estadísticas básicas de forma segura
            int totalUsuarios = usuarios != null ? usuarios.size() : 0;
            int usuariosActivos = 0;
            int usuariosInactivos = 0;

            if (usuarios != null) {
                for (Usuario usuario : usuarios) {
                    if (usuario.getEstado() != null && usuario.getEstado()) {
                        usuariosActivos++;
                    } else {
                        usuariosInactivos++;
                    }
                }
            }

            // Datos seguros para el modelo
            model.addAttribute("usuarios", usuarios != null ? usuarios : new ArrayList<>());
            model.addAttribute("roles", roles != null ? roles : new ArrayList<>());
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuariosActivos", usuariosActivos);
            model.addAttribute("usuariosInactivos", usuariosInactivos);
            
            // Variables de paginación por defecto
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalElements", totalUsuarios);
            model.addAttribute("pageSize", 12);
            model.addAttribute("search", "");
            model.addAttribute("selectedRol", "");

            System.out.println("=== DEBUG GESTION USUARIOS ===");
            System.out.println("Usuarios encontrados: " + totalUsuarios);
            System.out.println("Roles encontrados: " + (roles != null ? roles.size() : "NULL"));

            return "admin/gestion-usuarios-dev-style";

        } catch (Exception e) {
            System.err.println("Error en gestión de usuarios: " + e.getMessage());
            e.printStackTrace();
            
            // Modelo de emergencia
            model.addAttribute("usuarios", new ArrayList<>());
            model.addAttribute("roles", new ArrayList<>());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("usuariosActivos", 0);
            model.addAttribute("usuariosInactivos", 0);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            
            return "admin/gestion-usuarios-simple";
        }
    }

    /**
     * API para obtener información de un usuario específico
     */
    @GetMapping("/{dni}")
    @ResponseBody
    public ResponseEntity<?> getUsuario(@PathVariable String dni) {
        try {
            Optional<Usuario> usuario = usuarioRepository.findById(dni);
            if (usuario.isPresent()) {
                return ResponseEntity.ok(usuario.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener usuario: " + e.getMessage());
        }
    }

    /**
     * API para crear un nuevo usuario
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, Object> datos) {
        try {
            // Validar datos obligatorios
            if (!datos.containsKey("dni") || !datos.containsKey("nombre") || 
                !datos.containsKey("apellidoPaterno") || !datos.containsKey("correo") || 
                !datos.containsKey("contrasena") || !datos.containsKey("idRol")) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            // Verificar si el usuario ya existe
            String dni = (String) datos.get("dni");
            if (usuarioRepository.existsById(dni)) {
                return ResponseEntity.badRequest().body("Ya existe un usuario con ese DNI");
            }

            String correo = (String) datos.get("correo");
            if (usuarioRepository.findByCorreo(correo) != null) {
                return ResponseEntity.badRequest().body("Ya existe un usuario con ese correo");
            }

            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDni(dni);
            nuevoUsuario.setNombre((String) datos.get("nombre"));
            nuevoUsuario.setApellidoPaterno((String) datos.get("apellidoPaterno"));
            nuevoUsuario.setApellidoMaterno((String) datos.get("apellidoMaterno"));
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setContrasena(passwordEncoder.encode((String) datos.get("contrasena")));
            nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));
            nuevoUsuario.setEstado(true); // Por defecto activo

            // Asignar rol
            Integer idRol = (Integer) datos.get("idRol");
            Optional<Rol> rol = rolRepository.findById(idRol);
            if (rol.isPresent()) {
                nuevoUsuario.setRol(rol.get());
            } else {
                return ResponseEntity.badRequest().body("El rol especificado no existe");
            }

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            return ResponseEntity.ok(usuarioGuardado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    /**
     * API para actualizar un usuario existente
     */
    @PutMapping("/{dni}")
    @ResponseBody
    public ResponseEntity<?> actualizarUsuario(@PathVariable String dni, @RequestBody Map<String, Object> datos) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();

            // Actualizar campos si están presentes
            if (datos.containsKey("nombre")) {
                usuario.setNombre((String) datos.get("nombre"));
            }
            if (datos.containsKey("apellidoPaterno")) {
                usuario.setApellidoPaterno((String) datos.get("apellidoPaterno"));
            }
            if (datos.containsKey("apellidoMaterno")) {
                usuario.setApellidoMaterno((String) datos.get("apellidoMaterno"));
            }
            if (datos.containsKey("correo")) {
                String nuevoCorreo = (String) datos.get("correo");
                if (!nuevoCorreo.equals(usuario.getCorreo())) {
                    Usuario existente = usuarioRepository.findByCorreo(nuevoCorreo);
                    if (existente != null && !existente.getDni().equals(dni)) {
                        return ResponseEntity.badRequest().body("Ya existe un usuario con ese correo");
                    }
                    usuario.setCorreo(nuevoCorreo);
                }
            }
            if (datos.containsKey("contrasena") && !((String) datos.get("contrasena")).isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode((String) datos.get("contrasena")));
            }
            if (datos.containsKey("idRol")) {
                Integer idRol = (Integer) datos.get("idRol");
                Optional<Rol> rol = rolRepository.findById(idRol);
                if (rol.isPresent()) {
                    usuario.setRol(rol.get());
                } else {
                    return ResponseEntity.badRequest().body("El rol especificado no existe");
                }
            }

            Usuario usuarioActualizado = usuarioRepository.save(usuario);
            return ResponseEntity.ok(usuarioActualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * API para cambiar el estado de un usuario (activar/desactivar)
     */
    @PutMapping("/{dni}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable String dni, @RequestBody Map<String, Boolean> datos) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            Boolean nuevoEstado = datos.get("estado");
            usuario.setEstado(nuevoEstado);
            
            usuarioRepository.save(usuario);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Estado actualizado correctamente",
                "nuevoEstado", nuevoEstado
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cambiar estado: " + e.getMessage());
        }
    }

    /**
     * API para eliminar un usuario
     */
    @DeleteMapping("/{dni}")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable String dni) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Verificar si el usuario tiene dependencias (sesiones, notificaciones, etc.)
            // Por seguridad, mejor desactivar que eliminar
            Usuario usuario = usuarioOpt.get();
            usuario.setEstado(false);
            usuarioRepository.save(usuario);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario desactivado correctamente (no eliminado por seguridad)",
                "accion", "desactivado"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * API para búsqueda rápida de usuarios
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarUsuarios(@RequestParam String termino) {
        try {
            List<Usuario> usuarios = usuarioRepository.findTop10ByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(
                termino, termino);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en búsqueda: " + e.getMessage());
        }
    }

    /**
     * Endpoint de prueba para verificar conectividad
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "mensaje", "Controlador funcionando correctamente",
            "timestamp", LocalDateTime.now().toString(),
            "endpoint", "/admin/gestion-usuarios/test"
        ));
    }

    /**
     * Endpoint de prueba para cambio de estado
     */
    @PutMapping("/test-estado/{dni}")
    @ResponseBody
    public ResponseEntity<?> testEstado(@PathVariable String dni, @RequestBody Map<String, Boolean> datos) {
        System.out.println("=== TEST ESTADO ===");
        System.out.println("DNI recibido: " + dni);
        System.out.println("Datos recibidos: " + datos);
        System.out.println("Estado en datos: " + datos.get("estado"));
        
        return ResponseEntity.ok(Map.of(
            "mensaje", "Test de cambio de estado exitoso",
            "dni", dni,
            "estadoRecibido", datos.get("estado"),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Verificar estado de impersonación
     */
    @GetMapping("/impersonation-status")
    @ResponseBody
    public ResponseEntity<?> verificarEstadoImpersonacion(HttpSession session) {
        try {
            Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            String originalAdmin = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
            
            System.out.println("=== VERIFICANDO IMPERSONACIÓN ===");
            System.out.println("IS_IMPERSONATING: " + isImpersonating);
            System.out.println("IMPERSONATED_USER_DNI: " + impersonatedUserDni);
            System.out.println("ORIGINAL_ADMIN_USERNAME: " + originalAdmin);
            
            if (isImpersonating != null && isImpersonating) {
                return ResponseEntity.ok(Map.of(
                    "isImpersonating", true,
                    "impersonatedUserDni", impersonatedUserDni != null ? impersonatedUserDni : "Desconocido",
                    "originalAdmin", originalAdmin != null ? originalAdmin : "Desconocido"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "isImpersonating", false
                ));
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando impersonación: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "isImpersonating", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Impersonar usuario - Cambiar sesión al usuario seleccionado
     */
    @PostMapping("/impersonate/{dni}")
    @ResponseBody
    public ResponseEntity<?> impersonarUsuario(@PathVariable String dni, HttpServletRequest request, HttpSession session) {
        try {
            System.out.println("=== INICIANDO IMPERSONACIÓN ===");
            System.out.println("DNI a impersonar: " + dni);
            System.out.println("Session ID: " + session.getId());
            
            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                System.out.println("❌ Usuario no encontrado: " + dni);
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado: " + usuario.getNombre() + " " + usuario.getApellidoPaterno());
            
            // Verificar que el usuario está activo
            if (!usuario.getEstado()) {
                System.out.println("❌ Usuario inactivo: " + dni);
                return ResponseEntity.badRequest().body("No se puede impersonar un usuario inactivo");
            }

            // Obtener admin actual
            String currentAdmin = request.getUserPrincipal().getName();
            System.out.println("👤 Admin actual: " + currentAdmin);

            // Guardar la sesión actual del admin (para poder volver)
            session.setAttribute("IS_IMPERSONATING", true);
            session.setAttribute("ORIGINAL_ADMIN_USERNAME", currentAdmin);
            session.setAttribute("IMPERSONATED_USER_DNI", dni);
            session.setAttribute("IMPERSONATED_USER_EMAIL", usuario.getCorreo());
            session.setAttribute("IMPERSONATED_USER_NAME", usuario.getNombre() + " " + usuario.getApellidoPaterno());
            session.setAttribute("IMPERSONATED_USER_ROLE", usuario.getRol().getNombreRol());
            
            // Verificar que se guardaron
            System.out.println("📊 Variables de sesión guardadas:");
            System.out.println("  - IS_IMPERSONATING: " + session.getAttribute("IS_IMPERSONATING"));
            System.out.println("  - ORIGINAL_ADMIN_USERNAME: " + session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("  - IMPERSONATED_USER_DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
            System.out.println("  - IMPERSONATED_USER_EMAIL: " + session.getAttribute("IMPERSONATED_USER_EMAIL"));
            System.out.println("  - IMPERSONATED_USER_NAME: " + session.getAttribute("IMPERSONATED_USER_NAME"));
            System.out.println("  - IMPERSONATED_USER_ROLE: " + session.getAttribute("IMPERSONATED_USER_ROLE"));
            
            String redirectUrl = getRedirectUrlForRole(usuario.getRol().getNombreRol());
            System.out.println("🔄 Redirigiendo a: " + redirectUrl);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Impersonación iniciada correctamente",
                "usuarioImpersonado", usuario.getNombre() + " " + usuario.getApellidoPaterno(),
                "rol", usuario.getRol().getNombreRol(),
                "redirectUrl", redirectUrl,
                "sessionId", session.getId()
            ));

        } catch (Exception e) {
            System.err.println("💥 Error en impersonación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al impersonar usuario: " + e.getMessage());
        }
    }

    /**
     * Detener impersonación - Volver a la sesión original del admin
     */
    @PostMapping("/stop-impersonation")
    @ResponseBody
    public ResponseEntity<?> detenerImpersonacion(HttpSession session) {
        try {
            System.out.println("=== DETENIENDO IMPERSONACIÓN ===");
            System.out.println("Session ID: " + session.getId());
            
            Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
            System.out.println("IS_IMPERSONATING actual: " + isImpersonating);
            
            if (isImpersonating == null || !isImpersonating) {
                System.out.println("❌ No hay impersonación activa");
                return ResponseEntity.badRequest().body("No hay impersonación activa");
            }

            String originalAdmin = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
            String impersonatedDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            String impersonatedName = (String) session.getAttribute("IMPERSONATED_USER_NAME");
            
            System.out.println("📊 Datos antes de limpiar:");
            System.out.println("  - Admin original: " + originalAdmin);
            System.out.println("  - Usuario impersonado: " + impersonatedName + " (DNI: " + impersonatedDni + ")");
            
            // Limpiar atributos de impersonación
            session.removeAttribute("IS_IMPERSONATING");
            session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
            session.removeAttribute("IMPERSONATED_USER_DNI");
            session.removeAttribute("IMPERSONATED_USER_EMAIL");
            session.removeAttribute("IMPERSONATED_USER_NAME");
            session.removeAttribute("IMPERSONATED_USER_ROLE");
            
            // Verificar limpieza
            System.out.println("🧹 Después de limpiar:");
            System.out.println("  - IS_IMPERSONATING: " + session.getAttribute("IS_IMPERSONATING"));
            System.out.println("  - IMPERSONATED_USER_DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
            
            System.out.println("✅ Impersonación terminada. Volviendo a: " + originalAdmin);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Impersonación terminada correctamente",
                "redirectUrl", "/admin/gestion-usuarios",
                "originalAdmin", originalAdmin != null ? originalAdmin : "SuperAdmin",
                "previousUser", impersonatedName != null ? impersonatedName : "Usuario desconocido"
            ));

        } catch (Exception e) {
            System.err.println("💥 Error al detener impersonación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al detener impersonación: " + e.getMessage());
        }
    }

    /**
     * Obtener URL de redirección según el rol
     */
    private String getRedirectUrlForRole(String roleName) {
        switch (roleName.toUpperCase()) {
            case "SUPERADMIN":
                return "/admin/home";
            case "DEVELOPER":
            case "DEV":
                return "/dev/home";
            case "QA":
                return "/qa/catalogo";
            case "PO":
            case "PRODUCT_OWNER":
                return "/po/Dashboard";
            default:
                return "/dev/home";
        }
    }
}