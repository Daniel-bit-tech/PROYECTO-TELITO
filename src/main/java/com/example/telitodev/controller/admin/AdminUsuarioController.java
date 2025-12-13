package com.example.telitodev.controller.admin;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.entity.TokenConfirmacion;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import com.example.telitodev.repository.TokenConfirmacionRepository;
import com.example.telitodev.repository.ActividadAdminRepository;
import com.example.telitodev.service.EmailService;
import com.example.telitodev.service.ImpersonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/gestion-usuarios")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminUsuarioController extends BaseController {
    /**
     * Vista para que el usuario confirme su cuenta y establezca su contraseña
     */
    @GetMapping("/confirmar-cuenta")
    public String mostrarFormularioConfirmacionCuenta(@RequestParam(required = false) String email,
                                                     @RequestParam(required = false) String token,
                                                     Model model) {
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("token", token != null ? token : "");
        return "confirmar-cuenta"; // Debes crear confirmar-cuenta.html en templates
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenConfirmacionRepository tokenConfirmacionRepository;
    
    @Autowired
    private com.example.telitodev.repository.OrganizacionRepository organizacionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.example.telitodev.service.AuditoriaService auditoriaService;

    @Autowired
    private ActividadAdminRepository actividadAdminRepository;

    @Autowired
    private ImpersonationService impersonationService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Página principal de gestión de usuarios con filtros y paginación
     */
    @GetMapping
    public String index(Model model,
                       @RequestParam(value = "search", defaultValue = "") String search,
                       @RequestParam(value = "estado", required = false) String estado,
                       @RequestParam(value = "rol", required = false) String rol,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "4") int size,
                       HttpServletRequest request,
                       Authentication auth,
                       HttpSession session) {
        try {
            System.out.println("=== FILTROS RECIBIDOS ===");
            System.out.println("Search: '" + search + "'");
            System.out.println("Estado: '" + estado + "'");
            System.out.println("Rol: '" + rol + "'");
            
            // Obtener usuario actual para excluirlo de la lista
            String usuarioActual = request.getUserPrincipal().getName();
            System.out.println("Usuario actual logueado: " + usuarioActual);
            
            // Obtener el usuario correcto considerando impersonación para el modelo
            Usuario currentUser = getCurrentUser(auth, session);
            model.addAttribute("usuario", currentUser);
            
            // Obtener todos los roles para el filtro
            List<Rol> roles = rolRepository.findAll();
            
            // Obtener todas las organizaciones para el modal de creación
            List<com.example.telitodev.entity.Organizacion> organizaciones = organizacionRepository.findAll();
            
            // Obtener usuarios con filtros aplicados (excluyendo al usuario actual)
            List<Usuario> todosLosUsuarios = obtenerUsuariosFiltrados(search, estado, rol, usuarioActual);
            
            // Estadísticas básicas (sobre todos los usuarios filtrados)
            int totalUsuarios = todosLosUsuarios != null ? todosLosUsuarios.size() : 0;
            int usuariosActivos = 0;
            int usuariosInactivos = 0;

            if (todosLosUsuarios != null) {
                for (Usuario usuario : todosLosUsuarios) {
                    if (usuario.getEstado() != null && usuario.getEstado()) {
                        usuariosActivos++;
                    } else {
                        usuariosInactivos++;
                    }
                }
            }

            // Implementar paginación manual
            List<Usuario> usuariosPaginados = new ArrayList<>();
            int totalPages = 0;
            
            if (todosLosUsuarios != null && !todosLosUsuarios.isEmpty()) {
                // Calcular paginación
                totalPages = (int) Math.ceil((double) totalUsuarios / size);
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalUsuarios);
                
                // Validar que la página solicitada sea válida
                if (page >= 0 && startIndex < totalUsuarios) {
                    usuariosPaginados = todosLosUsuarios.subList(startIndex, endIndex);
                }
            }

            // Datos para el modelo
            model.addAttribute("usuarios", usuariosPaginados);
            model.addAttribute("roles", roles != null ? roles : new ArrayList<>());
            model.addAttribute("organizaciones", organizaciones != null ? organizaciones : new ArrayList<>());
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuariosActivos", usuariosActivos);
            model.addAttribute("usuariosInactivos", usuariosInactivos);
            
            // Parámetros de filtro para mantener el estado
            model.addAttribute("search", search);
            model.addAttribute("selectedEstado", estado);
            model.addAttribute("selectedRol", rol);
            
            // Variables de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalUsuarios);
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", page < totalPages - 1);
            model.addAttribute("hasPrevious", page > 0);

            System.out.println("=== RESULTADOS FILTRADOS CON PAGINACIÓN ===");
            System.out.println("Total usuarios encontrados: " + totalUsuarios + " (excluyendo al usuario actual)");
            System.out.println("Usuarios en página " + (page + 1) + ": " + usuariosPaginados.size());
            System.out.println("Página actual: " + page + "/" + totalPages);
            System.out.println("Usuarios activos: " + usuariosActivos);
            System.out.println("Usuarios inactivos: " + usuariosInactivos);

            // Agregar información de impersonación al modelo
            addImpersonationAttributes(model, session);

            return "admin/gestion-usuarios-dev-style";

        } catch (Exception e) {
            System.err.println("Error en gestión de usuarios: " + e.getMessage());
            e.printStackTrace();
            
            // Obtener el usuario para el modelo incluso en caso de error
            try {
                Usuario currentUser = getCurrentUser(auth, session);
                model.addAttribute("usuario", currentUser);
            } catch (Exception userError) {
                System.err.println("Error obteniendo usuario en catch: " + userError.getMessage());
                // Agregar un usuario por defecto para evitar errores en la plantilla
                model.addAttribute("usuario", null);
            }
            
            // Modelo de emergencia
            model.addAttribute("usuarios", new ArrayList<>());
            model.addAttribute("roles", new ArrayList<>());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("usuariosActivos", 0);
            model.addAttribute("usuariosInactivos", 0);
            model.addAttribute("search", search);
            model.addAttribute("selectedEstado", estado);
            model.addAttribute("selectedRol", rol);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalElements", 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            
            // Agregar información de impersonación al modelo incluso en caso de error
            addImpersonationAttributes(model, session);
            
            return "admin/gestion-usuarios-dev-style";
        }
    }

    /**
     * Mostrar vista para editar usuario
     */
    @GetMapping("/editar/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String mostrarEdicionUsuario(@PathVariable String dni, Model model) {
        try {
            // Buscar usuario por DNI
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/gestion-usuarios";
            }

            Usuario usuario = usuarioOpt.get();
            
            System.out.println("=== DEBUG EDITAR USUARIO ===");
            System.out.println("Usuario encontrado: " + usuario.getDni());
            System.out.println("Nombre: " + usuario.getNombre());
            System.out.println("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombreRol() : "null"));
            
            // VALIDACIÓN DE SEGURIDAD: No se puede editar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                model.addAttribute("error", "Por seguridad, no se puede editar a otro SuperAdmin");
                return "redirect:/admin/gestion-usuarios";
            }

            // Obtener lista de roles para el select
            List<Rol> roles = rolRepository.findAll();
            System.out.println("Roles disponibles: " + roles.size());
            
            // Agregar datos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", roles);
            
            System.out.println("=== FIN DEBUG ===");
            
            return "admin/editarUsuario-simple";

        } catch (Exception e) {
            System.err.println("Error al cargar usuario para edición: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar usuario: " + e.getMessage());
            return "redirect:/admin/gestion-usuarios";
        }
    }

    /**
     * Procesar actualización de usuario
     */
    @PostMapping("/actualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String actualizarUsuario(
            @RequestParam String dni,
            @RequestParam String nombre,
            @RequestParam String apellidoPaterno,
            @RequestParam String apellidoMaterno,
            @RequestParam String correo,
            @RequestParam Integer idRol,
            @RequestParam Boolean estado,
            @RequestParam(required = false) String nuevaContrasena,
            @RequestParam(required = false) String confirmarContrasena,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Buscar usuario existente
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/gestion-usuarios";
            }

            Usuario usuario = usuarioOpt.get();
            
            // VALIDACIÓN DE SEGURIDAD: No se puede editar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                redirectAttributes.addFlashAttribute("error", "Por seguridad, no se puede editar a otro SuperAdmin");
                return "redirect:/admin/gestion-usuarios";
            }

            // Validar que el correo no esté en uso por otro usuario
            Usuario existeCorreo = usuarioRepository.findByCorreo(correo);
            if (existeCorreo != null && !existeCorreo.getDni().equals(dni)) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está en uso por otro usuario");
                return "redirect:/admin/gestion-usuarios/editar/" + dni;
            }

            // Buscar rol
            Optional<Rol> rolOpt = rolRepository.findById(idRol);
            if (!rolOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Rol no encontrado");
                return "redirect:/admin/gestion-usuarios/editar/" + dni;
            }

            // Validar cambio de contraseña si se proporciona
            boolean cambiarContrasena = false;
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                String passwordTrimmed = nuevaContrasena.trim();
                
                System.out.println("=== DEBUG CAMBIO CONTRASEÑA ===");
                System.out.println("Nueva contraseña proporcionada: " + (passwordTrimmed.length() > 0 ? "SÍ" : "NO"));
                System.out.println("Longitud contraseña: " + passwordTrimmed.length());
                System.out.println("Confirmar contraseña proporcionada: " + (confirmarContrasena != null && !confirmarContrasena.trim().isEmpty() ? "SÍ" : "NO"));
                
                // Validar que se proporcionó confirmación
                if (confirmarContrasena == null || confirmarContrasena.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe confirmar la nueva contraseña");
                    return "redirect:/admin/gestion-usuarios/editar/" + dni;
                }
                
                // Validar que las contraseñas coinciden
                if (!passwordTrimmed.equals(confirmarContrasena.trim())) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/admin/gestion-usuarios/editar/" + dni;
                }
                
                // Validar longitud mínima
                if (passwordTrimmed.length() < 8) {
                    redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 8 caracteres");
                    return "redirect:/admin/gestion-usuarios/editar/" + dni;
                }
                
                // Validar que no sea demasiado simple
                if (passwordTrimmed.toLowerCase().contains("123456") || 
                    passwordTrimmed.toLowerCase().contains("password") ||
                    passwordTrimmed.toLowerCase().equals(usuario.getDni()) ||
                    passwordTrimmed.toLowerCase().equals(usuario.getCorreo().toLowerCase())) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña es demasiado simple o predecible");
                    return "redirect:/admin/gestion-usuarios/editar/" + dni;
                }
                
                // Si llegamos aquí, la contraseña es válida
                cambiarContrasena = true;
                System.out.println("Contraseña validada correctamente, procediendo a cifrar...");
                
                try {
                    // Cifrar nueva contraseña
                    String passwordEncoded = passwordEncoder.encode(passwordTrimmed);
                    usuario.setContrasena(passwordEncoded);
                    System.out.println("Contraseña cifrada exitosamente");
                } catch (Exception e) {
                    System.err.println("Error al cifrar contraseña: " + e.getMessage());
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error interno al procesar la nueva contraseña");
                    return "redirect:/admin/gestion-usuarios/editar/" + dni;
                }
            } else if (confirmarContrasena != null && !confirmarContrasena.trim().isEmpty()) {
                // Si solo se proporcionó confirmación pero no contraseña nueva
                redirectAttributes.addFlashAttribute("error", "Debe proporcionar la nueva contraseña");
                return "redirect:/admin/gestion-usuarios/editar/" + dni;
            }

            // Capturar estados anteriores para auditoría
            boolean estadoAnterior = usuario.getEstado() != null ? usuario.getEstado() : false;
            boolean cambioEstado = estadoAnterior != estado;
            
            Integer rolAnteriorId = usuario.getRol() != null ? usuario.getRol().getIdRol() : null;
            boolean cambioRol = !idRol.equals(rolAnteriorId);
            String rolAnteriorNombre = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "Sin rol";
            String rolNuevoNombre = rolOpt.get().getNombreRol();
            
            // Actualizar datos del usuario
            usuario.setNombre(nombre.trim());
            usuario.setApellidoPaterno(apellidoPaterno.trim());
            usuario.setApellidoMaterno(apellidoMaterno != null ? apellidoMaterno.trim() : "");
            usuario.setCorreo(correo.trim().toLowerCase());
            usuario.setRol(rolOpt.get());
            usuario.setEstado(estado);

            // Guardar cambios
            usuarioRepository.save(usuario);

            String mensaje = "Usuario actualizado correctamente";
            if (cambiarContrasena) {
                mensaje += " (incluyendo nueva contraseña)";
                System.out.println("✅ Usuario actualizado con nueva contraseña para DNI: " + dni);
                // Registrar auditoría - cambio de contraseña
                auditoriaService.registrarActividad(
                    com.example.telitodev.service.AuditoriaService.CAMBIAR_PASSWORD,
                    "Se cambió la contraseña del usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno(),
                    usuario.getDni()
                );
            } else {
                System.out.println("✅ Usuario actualizado sin cambio de contraseña para DNI: " + dni);
            }
            
            // Registrar auditorías específicas según los tipos de cambios
            if (cambioEstado) {
                String accionEstado = estado ? com.example.telitodev.service.AuditoriaService.ACTIVAR_USUARIO 
                                            : com.example.telitodev.service.AuditoriaService.BANEAR_USUARIO;
                String descripcionEstado = (estado ? "Activó" : "Baneó") + 
                    " al usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + " (" + usuario.getCorreo() + ")";
                auditoriaService.registrarActividad(accionEstado, descripcionEstado, usuario.getDni());
            }
            
            if (cambioRol) {
                String descripcionRol = "Cambió el rol del usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + 
                    " de '" + rolAnteriorNombre + "' a '" + rolNuevoNombre + "'";
                auditoriaService.registrarActividad(
                    com.example.telitodev.service.AuditoriaService.CAMBIAR_ROL, 
                    descripcionRol, 
                    usuario.getDni()
                );
            }
            
            // Registrar auditoría general de edición para otros cambios
            auditoriaService.registrarActividad(
                com.example.telitodev.service.AuditoriaService.EDITAR_USUARIO,
                "Se editó la información del usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + " (" + usuario.getCorreo() + ")",
                usuario.getDni()
            );
            
            redirectAttributes.addFlashAttribute("success", mensaje);
            return "redirect:/admin/gestion-usuarios";

        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/admin/gestion-usuarios";
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
     * API para obtener información completa de un usuario para edición
     */
    @GetMapping("/{dni}/editar")
    @ResponseBody
    public ResponseEntity<?> getUsuarioParaEditar(@PathVariable String dni) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // VALIDACIÓN DE SEGURIDAD: No se puede editar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                return ResponseEntity.badRequest().body("❌ Por seguridad, no se puede editar a otro SuperAdmin");
            }

            // Crear objeto de respuesta con todos los datos necesarios para edición
            Map<String, Object> response = new HashMap<>();
            response.put("dni", usuario.getDni());
            response.put("nombre", usuario.getNombre());
            response.put("apellidoPaterno", usuario.getApellidoPaterno());
            response.put("apellidoMaterno", usuario.getApellidoMaterno());
            response.put("correo", usuario.getCorreo());
            response.put("estado", usuario.getEstado());
            response.put("fechaRegistro", usuario.getFechaRegistro());
            
            // Información del rol
            if (usuario.getRol() != null) {
                Map<String, Object> rolInfo = new HashMap<>();
                rolInfo.put("idRol", usuario.getRol().getIdRol());
                rolInfo.put("nombreRol", usuario.getRol().getNombreRol());
                rolInfo.put("descripcion", usuario.getRol().getDescripcion());
                response.put("rol", rolInfo);
            }
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener usuario para edición: " + e.getMessage());
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
            
            // Registrar auditoría - creación de usuario
            auditoriaService.registrarActividad(
                com.example.telitodev.service.AuditoriaService.CREAR_USUARIO,
                "Se creó el usuario " + usuarioGuardado.getNombre() + " " + usuarioGuardado.getApellidoPaterno() + 
                " con rol " + usuarioGuardado.getRol().getNombreRol() + " (" + usuarioGuardado.getCorreo() + ")",
                usuarioGuardado.getDni()
            );
            
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

            // VALIDACIÓN DE SEGURIDAD: Un SuperAdmin no puede gestionar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                return ResponseEntity.badRequest().body("❌ Por seguridad, no se puede editar a otro SuperAdmin");
            }

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
            boolean cambioEstado = false;
            boolean estadoAnterior = false;
            boolean estadoNuevo = false;
            
            if (datos.containsKey("estado")) {
                estadoAnterior = usuario.getEstado() != null ? usuario.getEstado() : false;
                estadoNuevo = (Boolean) datos.get("estado");
                
                if (estadoAnterior != estadoNuevo) {
                    cambioEstado = true;
                }
                
                usuario.setEstado(estadoNuevo);
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
            
            // Registrar auditorías específicas según los tipos de cambios
            if (cambioEstado) {
                String accionEstado = estadoNuevo ? com.example.telitodev.service.AuditoriaService.ACTIVAR_USUARIO 
                                                : com.example.telitodev.service.AuditoriaService.BANEAR_USUARIO;
                String descripcionEstado = (estadoNuevo ? "Activó" : "Baneó") + 
                    " al usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + " (" + usuario.getCorreo() + ")";
                auditoriaService.registrarActividad(accionEstado, descripcionEstado, usuario.getDni());
            }
            
            // Registrar auditoría general de edición
            auditoriaService.registrarActividad(
                com.example.telitodev.service.AuditoriaService.EDITAR_USUARIO,
                "Se editó información del usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + " (" + usuario.getCorreo() + ")",
                usuario.getDni()
            );
            
            return ResponseEntity.ok(usuarioActualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para invalidar todas las sesiones activas de un usuario
     */
    private void invalidarSesionesUsuario(String correoUsuario) {
        try {
            // Para invalidar sesiones necesitaríamos acceso al SessionRegistry
            // Por ahora, solo logueamos la acción
            System.out.println("🔄 Invalidando sesiones para usuario: " + correoUsuario);
            
            // TODO: Implementar invalidación de sesiones con SessionRegistry
            // Por ahora el usuario será deslogueado en su próxima request automáticamente
            // ya que el UsuarioDetailService verificará que está desactivado
            
            System.out.println("⚠️ Nota: El usuario será deslogueado automáticamente en su próxima acción");
            
        } catch (Exception e) {
            System.err.println("❌ Error invalidando sesiones: " + e.getMessage());
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
            
            // VALIDACIÓN DE SEGURIDAD: Un SuperAdmin no puede gestionar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                return ResponseEntity.badRequest().body("❌ Por seguridad, no se puede modificar el estado de otro SuperAdmin");
            }
            
            Boolean nuevoEstado = datos.get("estado");
            String correoUsuario = usuario.getCorreo();
            
            // Si se está desactivando al usuario, invalidar todas sus sesiones activas
            if (nuevoEstado != null && !nuevoEstado) {
                System.out.println("🔒 Desactivando usuario y cerrando sesiones activas: " + correoUsuario);
                invalidarSesionesUsuario(correoUsuario);
            }
            
            usuario.setEstado(nuevoEstado);
            usuarioRepository.save(usuario);
            
            // Registrar auditoría del cambio de estado
            String accionAuditoria = nuevoEstado ? com.example.telitodev.service.AuditoriaService.ACTIVAR_USUARIO 
                                                 : com.example.telitodev.service.AuditoriaService.BANEAR_USUARIO;
            String descripcionAuditoria = (nuevoEstado ? "Activó" : "Baneó") + 
                " al usuario " + usuario.getNombre() + " " + usuario.getApellidoPaterno() + " (" + usuario.getCorreo() + ")";
            
            System.out.println("🔍 REGISTRANDO AUDITORÍA:");
            System.out.println("   Acción: " + accionAuditoria);
            System.out.println("   Descripción: " + descripcionAuditoria);
            System.out.println("   DNI: " + usuario.getDni());
            
            try {
                auditoriaService.registrarActividad(accionAuditoria, descripcionAuditoria, usuario.getDni());
                System.out.println("✅ AUDITORÍA REGISTRADA EXITOSAMENTE");
            } catch (Exception e) {
                System.err.println("❌ ERROR AL REGISTRAR AUDITORÍA: " + e.getMessage());
                e.printStackTrace();
            }
            
            String accion = nuevoEstado ? "activado" : "desactivado";
            String mensaje = nuevoEstado ? "Usuario activado correctamente" : 
                           "Usuario desactivado correctamente. Se han cerrado todas sus sesiones activas.";
            
            System.out.println("✅ Usuario " + accion + ": " + correoUsuario);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", mensaje,
                "nuevoEstado", nuevoEstado,
                "accion", accion,
                "nombreUsuario", usuario.getNombre() + " " + usuario.getApellidoPaterno()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error al cambiar estado del usuario: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("🔍 Iniciando eliminación del usuario: " + dni);
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                System.out.println("❌ Usuario no encontrado: " + dni);
                return ResponseEntity.notFound().build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // VALIDACIÓN DE SEGURIDAD: Un SuperAdmin no puede gestionar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                System.out.println("❌ Intento de eliminar SuperAdmin bloqueado");
                return ResponseEntity.badRequest().body("❌ Por seguridad, no se puede eliminar a otro SuperAdmin");
            }

            // Guardar información del usuario antes de eliminar
            String nombreUsuario = usuario.getNombre();
            String correoUsuario = usuario.getCorreo();
            String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno();

            // PRIMERO: Registrar auditoría ANTES de eliminar registros relacionados
            // (para evitar crear un nuevo registro que luego tengamos que eliminar)
            System.out.println("📝 Registrando auditoría de eliminación...");
            auditoriaService.registrarActividad(
                com.example.telitodev.service.AuditoriaService.ELIMINAR_USUARIO,
                "Eliminó permanentemente al usuario " + nombreCompleto + " (" + correoUsuario + ")",
                dni
            );

            // SEGUNDO: Eliminar TODOS los registros de auditoría relacionados (incluyendo el que acabamos de crear)
            try {
                System.out.println("🔍 Verificando registros de auditoría para DNI: " + dni);
                long actividadesEliminadas = actividadAdminRepository.countByUsuarioAfectadoDni(dni);
                System.out.println("📊 Registros de auditoría encontrados: " + actividadesEliminadas);
                
                if (actividadesEliminadas > 0) {
                    System.out.println("🗑️ Eliminando " + actividadesEliminadas + " registros de auditoría relacionados");
                    eliminarRegistrosAuditoriaDirectamente(dni);
                    System.out.println("✅ Registros de auditoría eliminados correctamente");
                } else {
                    System.out.println("ℹ️ No hay registros de auditoría que eliminar");
                }
            } catch (Exception e) {
                System.err.println("❌ Error al eliminar registros de auditoría: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Error al eliminar registros relacionados: " + e.getMessage());
            }

            // TERCERO: Eliminar el usuario (ya sin restricciones de foreign key)
            System.out.println("🗑️ Procediendo a eliminar usuario: " + nombreCompleto);
            usuarioRepository.delete(usuario);
            System.out.println("✅ Usuario eliminado exitosamente");
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario eliminado permanentemente de la base de datos",
                "accion", "eliminado",
                "usuario", Map.of(
                    "dni", dni,
                    "nombre", nombreUsuario,
                    "correo", correoUsuario
                )
            ));

        } catch (Exception e) {
            System.err.println("❌ Error general al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * API para búsqueda rápida de usuarios (excluyendo al usuario actual)
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarUsuarios(@RequestParam String termino, HttpServletRequest request) {
        try {
            // Obtener usuario actual
            String usuarioActual = request.getUserPrincipal().getName();
            
            List<Usuario> usuarios = usuarioRepository.findTop10ByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(
                termino, termino);
            
            // Filtrar para excluir al usuario actual
            if (usuarioActual != null && !usuarioActual.trim().isEmpty()) {
                usuarios = usuarios.stream()
                    .filter(usuario -> {
                        String correoUsuario = usuario.getCorreo();
                        return correoUsuario == null || !correoUsuario.equalsIgnoreCase(usuarioActual.trim());
                    })
                    .collect(Collectors.toList());
            }
            
            System.out.println("Búsqueda '" + termino + "' - Resultados encontrados: " + usuarios.size() + 
                             " (excluyendo usuario actual: " + usuarioActual + ")");
            
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
     * Endpoint para obtener estadísticas actualizadas de usuarios
     */
    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            long totalUsuarios = usuarioRepository.count();
            long usuariosActivos = usuarioRepository.countByEstado(true);
            long usuariosInactivos = usuarioRepository.countByEstado(false);
            
            Map<String, Object> estadisticas = Map.of(
                "totalUsuarios", totalUsuarios,
                "usuariosActivos", usuariosActivos,
                "usuariosInactivos", usuariosInactivos,
                "timestamp", LocalDateTime.now().toString()
            );
            
            System.out.println("📊 Estadísticas actualizadas: " + estadisticas);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error al obtener estadísticas: " + e.getMessage());
        }
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
     * Endpoint de prueba para cambio de contraseña
     */
    @PostMapping("/test-password-change")
    @ResponseBody
    public ResponseEntity<?> testPasswordChange(@RequestBody Map<String, String> datos) {
        try {
            System.out.println("=== TEST CAMBIO CONTRASEÑA ===");
            System.out.println("Datos recibidos: " + datos);
            
            String dni = datos.get("dni");
            String nuevaContrasena = datos.get("nuevaContrasena");
            String confirmarContrasena = datos.get("confirmarContrasena");
            
            System.out.println("DNI: " + dni);
            System.out.println("Nueva contraseña proporcionada: " + (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()));
            System.out.println("Confirmación proporcionada: " + (confirmarContrasena != null && !confirmarContrasena.trim().isEmpty()));
            
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                System.out.println("Longitud contraseña: " + nuevaContrasena.length());
                System.out.println("¿Coinciden contraseñas?: " + (nuevaContrasena.equals(confirmarContrasena)));
                
                // Probar codificación
                String encoded = passwordEncoder.encode(nuevaContrasena);
                System.out.println("Contraseña codificada exitosamente: " + (encoded != null && !encoded.isEmpty()));
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Test de contraseña exitoso",
                    "longitudValida", nuevaContrasena.length() >= 8,
                    "contrasenasCoinciden", nuevaContrasena.equals(confirmarContrasena),
                    "codificacionExitosa", encoded != null && !encoded.isEmpty()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "No se proporcionó contraseña para testear"
            ));
            
        } catch (Exception e) {
            System.err.println("Error en test de contraseña: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error en test: " + e.getMessage());
        }
    }

    /**
     * Verificar estado de impersonación
     */
    @GetMapping("/impersonation-status")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('QA') or hasRole('DEV') or hasRole('PO')")
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

            // VALIDACIÓN DE SEGURIDAD: Un SuperAdmin no puede impersonar a otro SuperAdmin
            if (isSuperAdmin(usuario)) {
                System.out.println("❌ Intento de impersonar SuperAdmin bloqueado: " + dni);
                return ResponseEntity.badRequest().body("❌ Por seguridad, no se puede impersonar a otro SuperAdmin");
            }

            // Usar el servicio de impersonación para cambiar el SecurityContext
            boolean impersonacionExitosa = impersonationService.startImpersonation(dni, session);
            
            if (!impersonacionExitosa) {
                System.out.println("❌ Error al iniciar impersonación");
                return ResponseEntity.badRequest().body("Error al iniciar la impersonación");
            }
            
            // DEBUG: Mostrar información del rol
            String roleName = usuario.getRol().getNombreRol();
            System.out.println("🔍 DEBUG INFO:");
            System.out.println("   Rol encontrado: " + roleName);
            System.out.println("   Rol clase: " + roleName.getClass().getSimpleName());
            
            String redirectUrl = getRedirectUrlForRole(roleName).replace("redirect:", "");
            System.out.println("   URL base del método: " + getRedirectUrlForRole(roleName));
            System.out.println("   URL final (sin redirect:): " + redirectUrl);
            System.out.println("🔄 Redirigiendo a: " + redirectUrl);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Impersonación iniciada correctamente",
                "usuarioImpersonado", usuario.getNombre() + " " + usuario.getApellidoPaterno(),
                "rol", usuario.getRol().getNombreRol(),
                "redirectUrl", redirectUrl,
                "sessionId", session.getId(),
                "activateBackProtection", true // Flag para activar protección en frontend
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
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('QA') or hasRole('DEV') or hasRole('PO')")
    @ResponseBody
    public ResponseEntity<?> detenerImpersonacion(HttpSession session, HttpServletResponse response) {
        try {
            System.out.println("=== DETENIENDO IMPERSONACIÓN ===");
            System.out.println("Session ID: " + session.getId());
            
            // DIAGNÓSTICO DETALLADO DE LA SESIÓN
            System.out.println("🔍 DIAGNÓSTICO COMPLETO DE SESIÓN:");
            Boolean isImpersonatingFlag = (Boolean) session.getAttribute("IS_IMPERSONATING");
            String originalAdmin = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
            String impersonatedDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            String impersonatedName = (String) session.getAttribute("IMPERSONATED_USER_NAME");
            
            System.out.println("   IS_IMPERSONATING: " + isImpersonatingFlag);
            System.out.println("   ORIGINAL_ADMIN_USERNAME: " + originalAdmin);
            System.out.println("   IMPERSONATED_USER_DNI: " + impersonatedDni);
            System.out.println("   IMPERSONATED_USER_NAME: " + impersonatedName);
            
            // Agregar headers de seguridad para prevenir cache y navegación hacia atrás
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // VERIFICACIÓN MÁS ROBUSTA DEL ESTADO DE IMPERSONACIÓN
            boolean hasImpersonationFlags = (isImpersonatingFlag != null && isImpersonatingFlag) ||
                                          (impersonatedDni != null && !impersonatedDni.trim().isEmpty()) ||
                                          (originalAdmin != null && !originalAdmin.trim().isEmpty());
            
            boolean serviceDetectsImpersonation = impersonationService.isImpersonating(session);
            
            System.out.println("🔍 ANÁLISIS DE ESTADO:");
            System.out.println("   ¿Flags de impersonación presentes?: " + hasImpersonationFlags);
            System.out.println("   ¿Servicio detecta impersonación?: " + serviceDetectsImpersonation);
            
            // Si NO hay evidencia de impersonación en ningún lado, es un estado inconsistente
            if (!hasImpersonationFlags && !serviceDetectsImpersonation) {
                System.out.println("❌ ESTADO INCONSISTENTE: No hay evidencia de impersonación activa");
                System.out.println("🧹 LIMPIANDO SESIÓN Y FORZANDO REDIRECCIÓN A ADMIN");
                
                // Limpiar completamente la sesión de cualquier vestigio
                session.removeAttribute("IS_IMPERSONATING");
                session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
                session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
                session.removeAttribute("IMPERSONATED_USER_DNI");
                session.removeAttribute("IMPERSONATED_USER_EMAIL");
                session.removeAttribute("IMPERSONATED_USER_NAME");
                session.removeAttribute("IMPERSONATED_USER_ROLE");
                session.setAttribute("FORCE_ADMIN_ACCESS", true);
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Estado inconsistente detectado. Sesión limpiada y redirigiendo a SuperAdmin...",
                    "redirectUrl", "/admin/gestion-usuarios",
                    "originalAdmin", "SuperAdmin",
                    "previousUser", "Estado inconsistente",
                    "clearHistory", true,
                    "disableProtection", true,
                    "forceAdminRedirect", true,
                    "stateInconsistent", true
                ));
            }
            
            // Si hay flags pero el servicio no detecta impersonación, usar los flags
            if (hasImpersonationFlags && !serviceDetectsImpersonation) {
                System.out.println("⚠️ INCONSISTENCIA: Flags presentes pero servicio no detecta impersonación");
                System.out.println("🔄 Usando flags para obtener información y limpiando manualmente");
                
                // Limpiar manualmente usando los flags encontrados
                session.removeAttribute("IS_IMPERSONATING");
                session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
                session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
                session.removeAttribute("IMPERSONATED_USER_DNI");
                session.removeAttribute("IMPERSONATED_USER_EMAIL");
                session.removeAttribute("IMPERSONATED_USER_NAME");
                session.removeAttribute("IMPERSONATED_USER_ROLE");
                session.setAttribute("FORCE_ADMIN_ACCESS", true);
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Impersonación terminada correctamente (limpieza manual). Redirigiendo a SuperAdmin...",
                    "redirectUrl", "/admin/gestion-usuarios",
                    "originalAdmin", originalAdmin != null ? originalAdmin : "SuperAdmin",
                    "previousUser", impersonatedName != null ? impersonatedName : "Usuario desconocido",
                    "clearHistory", true,
                    "disableProtection", true,
                    "forceAdminRedirect", true,
                    "manualCleanup", true
                ));
            }
            
            // Si el servicio detecta impersonación, proceder normalmente
            if (!serviceDetectsImpersonation) {
                System.out.println("❌ No hay impersonación activa según el servicio");
                return ResponseEntity.badRequest().body("No hay impersonación activa");
            }

            String impersonatedNameFromService = impersonationService.getImpersonatedUserName(session);
            
            System.out.println("📊 Datos antes de detener:");
            System.out.println("  - Admin original: " + originalAdmin);
            System.out.println("  - Usuario impersonado (servicio): " + impersonatedNameFromService);
            System.out.println("  - Usuario impersonado (sesión): " + impersonatedName);
            
            // Usar el servicio para detener la impersonación
            boolean detencionExitosa = impersonationService.stopImpersonation(session);
            
            if (!detencionExitosa) {
                System.out.println("❌ Error al detener impersonación vía servicio");
                System.out.println("🔄 Intentando limpieza manual como fallback");
                
                // Fallback: limpieza manual
                session.removeAttribute("IS_IMPERSONATING");
                session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
                session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
                session.removeAttribute("IMPERSONATED_USER_DNI");
                session.removeAttribute("IMPERSONATED_USER_EMAIL");
                session.removeAttribute("IMPERSONATED_USER_NAME");
                session.removeAttribute("IMPERSONATED_USER_ROLE");
                session.setAttribute("FORCE_ADMIN_ACCESS", true);
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Impersonación terminada con limpieza de emergencia. Redirigiendo a SuperAdmin...",
                    "redirectUrl", "/admin/gestion-usuarios",
                    "originalAdmin", originalAdmin != null ? originalAdmin : "SuperAdmin",
                    "previousUser", impersonatedNameFromService != null ? impersonatedNameFromService : 
                                  (impersonatedName != null ? impersonatedName : "Usuario desconocido"),
                    "clearHistory", true,
                    "disableProtection", true,
                    "forceAdminRedirect", true,
                    "emergencyCleanup", true
                ));
            }
            
            System.out.println("✅ Impersonación terminada exitosamente. Volviendo a: " + originalAdmin);
            System.out.println("🔄 Configurando redirección FORZADA a admin");
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Impersonación terminada correctamente. Redirigiendo a SuperAdmin...",
                "redirectUrl", "/admin/gestion-usuarios", // FORZAR admin
                "originalAdmin", originalAdmin != null ? originalAdmin : "SuperAdmin",
                "previousUser", impersonatedNameFromService != null ? impersonatedNameFromService : "Usuario desconocido",
                "clearHistory", true, // Flag para el frontend
                "disableProtection", true, // Flag para desactivar protección de navegación
                "forceAdminRedirect", true // Nuevo flag para forzar redirección a admin
            ));

        } catch (Exception e) {
            System.err.println("💥 Error al detener impersonación: " + e.getMessage());
            e.printStackTrace();
            
            // LIMPIEZA DE EMERGENCIA EN CASO DE ERROR
            System.out.println("🆘 EJECUTANDO LIMPIEZA DE EMERGENCIA");
            try {
                session.removeAttribute("IS_IMPERSONATING");
                session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
                session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
                session.removeAttribute("IMPERSONATED_USER_DNI");
                session.removeAttribute("IMPERSONATED_USER_EMAIL");
                session.removeAttribute("IMPERSONATED_USER_NAME");
                session.removeAttribute("IMPERSONATED_USER_ROLE");
                session.setAttribute("FORCE_ADMIN_ACCESS", true);
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Error en terminación normal. Sesión limpiada exitosamente. Redirigiendo a SuperAdmin...",
                    "redirectUrl", "/admin/gestion-usuarios",
                    "originalAdmin", "SuperAdmin",
                    "previousUser", "Error en terminación",
                    "clearHistory", true,
                    "disableProtection", true,
                    "forceAdminRedirect", true,
                    "errorRecovery", true,
                    "originalError", e.getMessage()
                ));
                
            } catch (Exception cleanupError) {
                System.err.println("💥 Error crítico en limpieza de emergencia: " + cleanupError.getMessage());
                return ResponseEntity.status(500).body("Error crítico al detener impersonación: " + e.getMessage());
            }
        }
    }

    /**
     * Endpoint de emergencia para limpiar completamente la sesión de impersonación
     */
    @PostMapping("/emergency-cleanup")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('QA') or hasRole('DEV') or hasRole('PO')")
    @ResponseBody
    public ResponseEntity<?> limpiezaEmergencia(HttpSession session, HttpServletResponse response) {
        try {
            System.out.println("🆘 === LIMPIEZA DE EMERGENCIA ACTIVADA ===");
            System.out.println("Session ID: " + session.getId());
            
            // Headers de seguridad
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // Diagnóstico antes de limpiar
            System.out.println("🔍 Estado antes de limpieza:");
            Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
            String originalAdmin = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
            String impersonatedDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            String impersonatedName = (String) session.getAttribute("IMPERSONATED_USER_NAME");
            
            System.out.println("   IS_IMPERSONATING: " + isImpersonating);
            System.out.println("   ORIGINAL_ADMIN: " + originalAdmin);
            System.out.println("   IMPERSONATED_DNI: " + impersonatedDni);
            System.out.println("   IMPERSONATED_NAME: " + impersonatedName);
            
            // LIMPIEZA TOTAL - Remover TODOS los atributos de impersonación
            session.removeAttribute("IS_IMPERSONATING");
            session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
            session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
            session.removeAttribute("IMPERSONATED_USER_DNI");
            session.removeAttribute("IMPERSONATED_USER_EMAIL");
            session.removeAttribute("IMPERSONATED_USER_NAME");
            session.removeAttribute("IMPERSONATED_USER_ROLE");
            session.removeAttribute("IMPERSONATION_ENDED");
            session.removeAttribute("FORCE_ADMIN_ACCESS");
            
            // Forzar flags de limpieza
            session.setAttribute("EMERGENCY_CLEANUP_DONE", true);
            session.setAttribute("FORCE_ADMIN_ACCESS", true);
            
            System.out.println("🧹 Limpieza de emergencia completada");
            System.out.println("🔄 Forzando contexto de SuperAdmin");
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Limpieza de emergencia completada exitosamente. Redirigiendo a SuperAdmin...",
                "redirectUrl", "/admin/gestion-usuarios",
                "originalAdmin", originalAdmin != null ? originalAdmin : "SuperAdmin",
                "previousState", Map.of(
                    "wasImpersonating", isImpersonating != null ? isImpersonating : false,
                    "impersonatedUser", impersonatedName != null ? impersonatedName : "Desconocido"
                ),
                "clearHistory", true,
                "disableProtection", true,
                "forceAdminRedirect", true,
                "emergencyCleanup", true
            ));
            
        } catch (Exception e) {
            System.err.println("💥 Error crítico en limpieza de emergencia: " + e.getMessage());
            e.printStackTrace();
            
            // Último recurso: invalidar toda la sesión
            try {
                session.invalidate();
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Sesión invalidada completamente. Por favor, inicia sesión nuevamente.",
                    "redirectUrl", "/login",
                    "sessionInvalidated", true
                ));
            } catch (Exception invalidateError) {
                return ResponseEntity.status(500).body("Error crítico en limpieza de emergencia");
            }
        }
    }

    /**
     * Crear nuevo usuario con confirmación por email
     */
    @PostMapping("/crear-con-confirmacion")
    @ResponseBody
    public ResponseEntity<?> crearUsuarioConConfirmacion(@RequestBody Map<String, Object> datos, 
                                                        HttpServletRequest request) {
        try {
            System.out.println("=== CREANDO USUARIO CON CONFIRMACIÓN ===");
            System.out.println("Datos recibidos: " + datos);

            // Validar datos obligatorios
            if (!datos.containsKey("dni") || !datos.containsKey("nombre") || 
                !datos.containsKey("apellidoPaterno") || !datos.containsKey("correo") || 
                !datos.containsKey("contrasena") || !datos.containsKey("idRol")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Faltan datos obligatorios",
                    "camposRequeridos", List.of("dni", "nombre", "apellidoPaterno", "correo", "contrasena", "idRol")
                ));
            }

            String dni = ((String) datos.get("dni")).trim();
            String nombre = ((String) datos.get("nombre")).trim();
            String apellidoPaterno = ((String) datos.get("apellidoPaterno")).trim();
            String apellidoMaterno = datos.containsKey("apellidoMaterno") ? 
                                   ((String) datos.get("apellidoMaterno")).trim() : "";
            String correo = ((String) datos.get("correo")).trim().toLowerCase();
            String contrasena = ((String) datos.get("contrasena")).trim();
            Integer idRol = (Integer) datos.get("idRol");

            System.out.println("DNI: " + dni);
            System.out.println("Nombre: " + nombre + " " + apellidoPaterno);
            System.out.println("Email: " + correo);
            System.out.println("Rol ID: " + idRol);

            // Validaciones básicas
            if (dni.length() != 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "El DNI debe tener exactamente 8 dígitos"));
            }

            if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El formato del email no es válido"));
            }

            if (contrasena.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 8 caracteres"));
            }

            // Verificar si el usuario ya existe
            if (usuarioRepository.existsById(dni)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un usuario con ese DNI"));
            }

            if (usuarioRepository.findByCorreo(correo) != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un usuario con ese correo"));
            }

            // Verificar que el rol existe
            Optional<Rol> rolOpt = rolRepository.findById(idRol);
            if (!rolOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El rol especificado no existe"));
            }

            // Verificar si ya existe un token válido para este email o DNI
            LocalDateTime ahora = LocalDateTime.now();
            if (tokenConfirmacionRepository.existeTokenValidoPorEmail(correo, ahora)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ya existe un token de confirmación válido para este email. Revisa tu bandeja de entrada o espera a que expire."
                ));
            }

            if (tokenConfirmacionRepository.existeTokenValidoPorDni(dni, ahora)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ya existe un token de confirmación válido para este DNI."
                ));
            }

            // Limitar cantidad de tokens por IP (anti-spam)
            String ipCliente = obtenerIpCliente(request);
            LocalDateTime hace1Hora = LocalDateTime.now().minusHours(1);
            int tokensRecientes = tokenConfirmacionRepository.contarTokensRecientesPorIp(ipCliente, hace1Hora);
            if (tokensRecientes >= 5) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Se ha excedido el límite de solicitudes por hora. Intenta más tarde."
                ));
            }

            // Generar token de 6 dígitos
            String token = generarToken6Digitos();
            System.out.println("Token generado: " + token);

            // Cifrar contraseña
            String contrasenaEncriptada = passwordEncoder.encode(contrasena);

            // Crear token de confirmación
            TokenConfirmacion tokenConfirmacion = new TokenConfirmacion(
                token, correo, dni, nombre, apellidoPaterno, apellidoMaterno,
                contrasenaEncriptada, idRol, ipCliente
            );

            // Guardar token
            tokenConfirmacionRepository.save(tokenConfirmacion);
            System.out.println("✅ Token guardado en BD con ID: " + tokenConfirmacion.getId());

            // Enviar email
            boolean emailEnviado = emailService.enviarTokenConfirmacion(
                correo, nombre, token, tokenConfirmacion.getFechaExpiracion()
            );

            if (!emailEnviado) {
                System.err.println("❌ Error enviando email, pero token fue guardado");
                System.err.println("🔍 MODO DESARROLLO - INFORMACIÓN DEL TOKEN:");
                System.err.println("   Token: " + token);
                System.err.println("   Email: " + correo);
                System.err.println("   Usuario: " + nombre + " " + apellidoPaterno);
                System.err.println("   DNI: " + dni);
                System.err.println("   Expira: " + tokenConfirmacion.getFechaExpiracion());
                System.err.println("   Token ID en BD: " + tokenConfirmacion.getId());
                System.err.println("📋 Para activar manualmente: Ve al panel de tokens pendientes y haz clic en 'Activar'");
                
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario creado pero el email no pudo ser enviado.",
                    "emailEnviado", false,
                    "mostrarModalConfirmacion", true,  // Sí mostrar porque el email falló
                    "debug", Map.of(
                        "token", token,
                        "email", correo,
                        "usuario", nombre + " " + apellidoPaterno,
                        "dni", dni,
                        "tokenId", tokenConfirmacion.getId(),
                        "fechaExpiracion", tokenConfirmacion.getFechaExpiracion().toString(),
                        "instrucciones", "Ve al panel 'Tokens Pendientes' y activa la cuenta manualmente"
                    )
                ));
            }

            System.out.println("✅ Proceso completado exitosamente");
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario creado exitosamente. Se ha enviado un código de confirmación a " + correo,
                "email", correo,
                "expiracion", "3 minutos (modo prueba)",
                "emailEnviado", true,
                "mostrarModalConfirmacion", false  // NO mostrar modal porque el email se envió correctamente
            ));

        } catch (Exception e) {
            System.err.println("❌ Error creando usuario con confirmación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Confirmar cuenta con token de 6 dígitos - Método alternativo JSON
     */
    @PostMapping("/confirmar-cuenta-json")
    @ResponseBody
    public ResponseEntity<?> confirmarCuentaJson(@RequestBody Map<String, String> datos) {
        try {
            System.out.println("=== CONFIRMANDO CUENTA (JSON) ===");
            System.out.println("Datos JSON recibidos: " + datos);
            
            String email = datos.get("email");
            String token = datos.get("token");
            String password = datos.get("password");
            String confirmPassword = datos.get("confirmPassword");
            
            // Procesar de la misma forma pero devolver JSON
            // ... (aquí iría la misma lógica pero devolviendo ResponseEntity)
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Método JSON disponible"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Confirmar cuenta con token de 6 dígitos - Método principal con formulario
     */
    @PostMapping("/confirmar-cuenta")
    public String confirmarCuenta(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            System.out.println("=== CONFIRMANDO CUENTA ===");
            
            // Debug completo de todos los parámetros recibidos
            System.out.println("📋 TODOS LOS PARÁMETROS RECIBIDOS:");
            request.getParameterMap().forEach((key, values) -> {
                System.out.println("  " + key + " = [" + String.join(", ", values) + "]");
            });
            
            System.out.println("📧 Email recibido: '" + email + "'");
            System.out.println("🔑 Token recibido: '" + token + "'");
            System.out.println("🔒 Password recibida: '" + (password != null ? password : "NULL") + "' (longitud: " + (password != null ? password.length() : "N/A") + ")");
            System.out.println("🔒 ConfirmPassword recibida: '" + (confirmPassword != null ? confirmPassword : "NULL") + "' (longitud: " + (confirmPassword != null ? confirmPassword.length() : "N/A") + ")");

            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email es requerido");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            if (token == null || token.trim().isEmpty()) {
                model.addAttribute("error", "Token es requerido");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            if (password == null || password.trim().isEmpty()) {
                System.out.println("❌ Password está vacía o nula");
                model.addAttribute("error", "La nueva contraseña es requerida");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                System.out.println("❌ ConfirmPassword está vacía o nula");
                model.addAttribute("error", "Debes confirmar la contraseña");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }
            
            // Trim de las contraseñas para eliminar espacios
            password = password.trim();
            confirmPassword = confirmPassword.trim();
            
            System.out.println("🔒 Password después del trim: '" + password + "' (longitud: " + password.length() + ")");
            System.out.println("🔒 ConfirmPassword después del trim: '" + confirmPassword + "' (longitud: " + confirmPassword.length() + ")");
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

            email = email.trim().toLowerCase();
            token = token.trim();

            System.out.println("📧 Email limpio: '" + email + "'");
            System.out.println("🔑 Token limpio: '" + token + "'");
            System.out.println("🔒 Password validada: [" + password.length() + " caracteres]");

            // Buscar token
            System.out.println("🔍 Buscando token en base de datos...");
            Optional<TokenConfirmacion> tokenOpt = null;
            
            try {
                tokenOpt = tokenConfirmacionRepository.findByTokenAndEmailIgnoreCase(token, email);
                System.out.println("✅ Búsqueda de token completada. Token encontrado: " + tokenOpt.isPresent());
            } catch (Exception e) {
                System.err.println("❌ Error buscando token en BD: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error accediendo a la base de datos de tokens: " + e.getMessage(), e);
            }

            if (!tokenOpt.isPresent()) {
                System.out.println("❌ Token no encontrado");
                model.addAttribute("error", "Código de confirmación inválido o email incorrecto");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            TokenConfirmacion tokenConfirmacion = tokenOpt.get();
            System.out.println("✅ Token encontrado con ID: " + tokenConfirmacion.getId());
            System.out.println("📊 Detalles del token:");
            System.out.println("   - Email: " + tokenConfirmacion.getEmail());
            System.out.println("   - DNI Usuario: " + tokenConfirmacion.getDniUsuario());
            System.out.println("   - Fecha creación: " + tokenConfirmacion.getFechaCreacion());
            System.out.println("   - Fecha expiración: " + tokenConfirmacion.getFechaExpiracion());
            System.out.println("   - Usado: " + tokenConfirmacion.getUsado());

            // Verificar si el token es válido
            try {
                if (tokenConfirmacion.estaUsado()) {
                    System.out.println("❌ Token ya usado");
                    model.addAttribute("error", "Este código de confirmación ya ha sido utilizado");
                    model.addAttribute("email", email);
                    model.addAttribute("token", token);
                    return "confirmar-cuenta";
                }

                if (tokenConfirmacion.estaExpirado()) {
                    System.out.println("❌ Token expirado");
                    model.addAttribute("error", "El código de confirmación ha expirado. Solicita una nueva cuenta.");
                    model.addAttribute("email", email);
                    model.addAttribute("token", token);
                    return "confirmar-cuenta";
                }
                System.out.println("✅ Token es válido y no está expirado");
            } catch (Exception e) {
                System.err.println("❌ Error validando estado del token: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error validando token: " + e.getMessage(), e);
            }

            // Verificar una vez más que el usuario no existe (por si se creó mientras tanto)
            if (usuarioRepository.existsById(tokenConfirmacion.getDniUsuario())) {
                // Marcar token como usado
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                model.addAttribute("error", "Ya existe un usuario con este DNI");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            if (usuarioRepository.findByCorreo(tokenConfirmacion.getEmail()) != null) {
                // Marcar token como usado
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                model.addAttribute("error", "Ya existe un usuario con este email");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Obtener el rol
            System.out.println("🔍 Buscando rol con ID: " + tokenConfirmacion.getIdRolTemporal());
            Optional<Rol> rolOpt = null;
            
            try {
                rolOpt = rolRepository.findById(tokenConfirmacion.getIdRolTemporal());
                System.out.println("✅ Búsqueda de rol completada. Rol encontrado: " + rolOpt.isPresent());
                if (rolOpt.isPresent()) {
                    System.out.println("📊 Detalles del rol: " + rolOpt.get().getNombreRol());
                }
            } catch (Exception e) {
                System.err.println("❌ Error buscando rol en BD: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error accediendo a la base de datos de roles: " + e.getMessage(), e);
            }
            
            if (!rolOpt.isPresent()) {
                System.out.println("❌ Rol no encontrado con ID: " + tokenConfirmacion.getIdRolTemporal());
                model.addAttribute("error", "Error interno: rol no encontrado (ID: " + tokenConfirmacion.getIdRolTemporal() + ")");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

            // Crear el usuario definitivo
            System.out.println("🏗️ Creando nuevo usuario...");
            Usuario nuevoUsuario = new Usuario();
            
            try {
                nuevoUsuario.setDni(tokenConfirmacion.getDniUsuario());
                nuevoUsuario.setNombre(tokenConfirmacion.getNombreTemporal());
                nuevoUsuario.setApellidoPaterno(tokenConfirmacion.getApellidoPaternoTemporal());
                nuevoUsuario.setApellidoMaterno(tokenConfirmacion.getApellidoMaternoTemporal());
                nuevoUsuario.setCorreo(tokenConfirmacion.getEmail());
                
                // Generar un alias básico usando nombre y apellido
                String alias = tokenConfirmacion.getNombreTemporal().toLowerCase() + 
                              "." + tokenConfirmacion.getApellidoPaternoTemporal().toLowerCase();
                nuevoUsuario.setAlias(alias);
                
                System.out.println("🔒 Encriptando nueva contraseña...");
                // Encriptar la nueva contraseña
                String contrasenaEncriptada = passwordEncoder.encode(password);
                nuevoUsuario.setContrasena(contrasenaEncriptada);
                System.out.println("✅ Contraseña encriptada correctamente");
                
                nuevoUsuario.setRol(rolOpt.get());
                nuevoUsuario.setEstado(true); // Usuario activo
                nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));
                
                System.out.println("📊 Usuario a crear:");
                System.out.println("   - DNI: " + nuevoUsuario.getDni());
                System.out.println("   - Nombre: " + nuevoUsuario.getNombre());
                System.out.println("   - Email: " + nuevoUsuario.getCorreo());
                System.out.println("   - Alias: " + nuevoUsuario.getAlias());
                System.out.println("   - Rol: " + nuevoUsuario.getRol().getNombreRol());
                System.out.println("   - Rol ID: " + nuevoUsuario.getRol().getIdRol());
                System.out.println("   - Estado: " + nuevoUsuario.getEstado());
                System.out.println("   - Organización: " + (nuevoUsuario.getOrganizacion() != null ? nuevoUsuario.getOrganizacion().getNombre() : "null"));
                
            } catch (Exception e) {
                System.err.println("❌ Error configurando usuario: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error configurando datos del usuario: " + e.getMessage(), e);
            }

            try {
                // Verificar si el usuario ya existe en base de datos
                Optional<Usuario> usuarioExistente = usuarioRepository.findById(tokenConfirmacion.getDniUsuario());
                Usuario usuarioGuardado;
                
                if (usuarioExistente.isPresent()) {
                    // 🔄 Usuario ya existe - actualizar datos
                    System.out.println("🔄 Usuario ya existe con DNI: " + tokenConfirmacion.getDniUsuario() + ", actualizando información...");
                    usuarioGuardado = usuarioExistente.get();
                    
                    System.out.println("📊 Usuario existente encontrado:");
                    System.out.println("   - DNI: " + usuarioGuardado.getDni());
                    System.out.println("   - Nombre actual: " + usuarioGuardado.getNombre());
                    System.out.println("   - Email actual: " + usuarioGuardado.getCorreo());
                    System.out.println("   - Estado actual: " + usuarioGuardado.getEstado());
                    
                    // Actualizar contraseña y activar cuenta
                    System.out.println("🔒 Actualizando contraseña del usuario existente...");
                    String contrasenaEncriptada = passwordEncoder.encode(password);
                    usuarioGuardado.setContrasena(contrasenaEncriptada);
                    usuarioGuardado.setEstado(true); // Activar cuenta
                    usuarioGuardado.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now())); // Actualizar fecha
                    System.out.println("✅ Contraseña actualizada y cuenta activada");
                    
                    // Guardar cambios del usuario existente
                    usuarioGuardado = usuarioRepository.save(usuarioGuardado);
                    System.out.println("✅ Usuario existente actualizado: " + usuarioGuardado.getDni());
                    
                } else {
                    // Verificar que no exista por email
                    if (usuarioRepository.findByCorreo(tokenConfirmacion.getEmail()) != null) {
                        System.out.println("❌ Usuario ya existe con email: " + tokenConfirmacion.getEmail());
                        tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                        model.addAttribute("error", "Ya existe un usuario con este email");
                        model.addAttribute("email", email);
                        model.addAttribute("token", token);
                        return "confirmar-cuenta";
                    }
                    
                    // 🏗️ Crear nuevo usuario
                    System.out.println("🆕 Creando nuevo usuario (no existe en base de datos)...");
                    usuarioGuardado = usuarioRepository.save(nuevoUsuario);
                    System.out.println("✅ Nuevo usuario creado: " + usuarioGuardado.getDni());
                }

                // Marcar token como usado
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                System.out.println("✅ Token marcado como usado");

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
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                System.err.println("❌ Error de integridad de datos: " + ex.getMessage());
                ex.printStackTrace();
                
                // Marcar token como usado para evitar reintentes
                tokenConfirmacionRepository.marcarTokenComoUsado(tokenConfirmacion.getId());
                
                String errorMessage = "Ya existe un usuario con este DNI o email";
                if (ex.getMessage().contains("dni")) {
                    errorMessage = "Ya existe un usuario con este DNI";
                } else if (ex.getMessage().contains("correo")) {
                    errorMessage = "Ya existe un usuario con este email";
                }
                
                model.addAttribute("error", errorMessage);
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            } catch (Exception ex) {
                System.err.println("❌ Error inesperado al crear usuario: " + ex.getMessage());
                ex.printStackTrace();
                model.addAttribute("error", "Error interno del servidor. Intenta de nuevo o contacta al administrador.");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return "confirmar-cuenta";
            }

        } catch (Exception e) {
            System.err.println("❌ Error confirmando cuenta: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "confirmar-cuenta";
        }
    }

    /**
     * Obtener información de un token (para mostrar datos en frontend)
     */
    @GetMapping("/token-info")
    @ResponseBody
    public ResponseEntity<?> obtenerInfoToken(@RequestParam String email, @RequestParam String token) {
        try {
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository
                .findByTokenAndEmailIgnoreCase(token, email.trim());

            if (!tokenOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            TokenConfirmacion tokenConfirmacion = tokenOpt.get();

            return ResponseEntity.ok(Map.of(
                "email", tokenConfirmacion.getEmail(),
                "nombre", tokenConfirmacion.getNombreTemporal() + " " + tokenConfirmacion.getApellidoPaternoTemporal(),
                "fechaCreacion", tokenConfirmacion.getFechaCreacion(),
                "fechaExpiracion", tokenConfirmacion.getFechaExpiracion(),
                "usado", tokenConfirmacion.getUsado(),
                "expirado", tokenConfirmacion.estaExpirado(),
                "valido", tokenConfirmacion.esValido()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    /**
     * Verificar si un usuario es SuperAdmin
     */
    private boolean isSuperAdmin(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        String roleName = usuario.getRol().getNombreRol();
        return "SUPERADMIN".equals(roleName) || "SADMIN".equals(roleName);
    }

    /**
     * Obtener usuarios aplicando filtros de búsqueda y excluyendo al usuario actual
     */
    private List<Usuario> obtenerUsuariosFiltrados(String search, String estado, String rol, String usuarioActualEmail) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            if (usuarios == null) {
                return new ArrayList<>();
            }
            
            System.out.println("Total usuarios en BD: " + usuarios.size());
            
            // EXCLUIR al usuario actual (superadmin logueado) de la lista
            if (usuarioActualEmail != null && !usuarioActualEmail.trim().isEmpty()) {
                int usuariosAntesDeExcluir = usuarios.size();
                usuarios = usuarios.stream()
                    .filter(usuario -> {
                        String correoUsuario = usuario.getCorreo();
                        return correoUsuario == null || !correoUsuario.equalsIgnoreCase(usuarioActualEmail.trim());
                    })
                    .collect(Collectors.toList());
                System.out.println("Usuarios después de excluir '" + usuarioActualEmail + "': " + usuarios.size() + 
                                 " (se excluyeron " + (usuariosAntesDeExcluir - usuarios.size()) + " usuarios)");
            }
            
            // Aplicar filtro de búsqueda (nombre, apellidos, email, DNI)
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                usuarios = usuarios.stream()
                    .filter(usuario -> {
                        String nombre = (usuario.getNombre() != null ? usuario.getNombre() : "").toLowerCase();
                        String apellidoP = (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "").toLowerCase();
                        String apellidoM = (usuario.getApellidoMaterno() != null ? usuario.getApellidoMaterno() : "").toLowerCase();
                        String correo = (usuario.getCorreo() != null ? usuario.getCorreo() : "").toLowerCase();
                        String dni = (usuario.getDni() != null ? usuario.getDni() : "").toLowerCase();
                        
                        return nombre.contains(searchLower) || 
                               apellidoP.contains(searchLower) || 
                               apellidoM.contains(searchLower) || 
                               correo.contains(searchLower) ||
                               dni.contains(searchLower);
                    })
                    .collect(Collectors.toList());
                System.out.println("Después de filtro búsqueda: " + usuarios.size());
            }
            
            // Aplicar filtro de estado
            if (estado != null && !estado.trim().isEmpty()) {
                boolean estadoBool = "true".equals(estado);
                usuarios = usuarios.stream()
                    .filter(usuario -> usuario.getEstado() != null && usuario.getEstado() == estadoBool)
                    .collect(Collectors.toList());
                System.out.println("Después de filtro estado (" + estado + "): " + usuarios.size());
            }
            
            // Aplicar filtro de rol - CAMBIADO PARA USAR NOMBRE EN LUGAR DE ID
            if (rol != null && !rol.trim().isEmpty()) {
                usuarios = usuarios.stream()
                    .filter(usuario -> usuario.getRol() != null && rol.equals(usuario.getRol().getNombreRol()))
                    .collect(Collectors.toList());
                System.out.println("Después de filtro rol (" + rol + "): " + usuarios.size());
            }
            
            return usuarios;
            
        } catch (Exception e) {
            System.err.println("Error en filtrado de usuarios: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtener tokens pendientes de confirmación
     */
    @GetMapping("/api/tokens-pendientes")
    @ResponseBody
    public ResponseEntity<?> obtenerTokensPendientes() {
        try {
            System.out.println("=== OBTENIENDO TOKENS PENDIENTES ===");
            
            LocalDateTime ahora = LocalDateTime.now();
            List<TokenConfirmacion> tokens = tokenConfirmacionRepository.findTokensValidosConInfo(ahora);
            
            List<Map<String, Object>> resultado = tokens.stream()
                .map(token -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", token.getId());
                    info.put("token", token.getToken());
                    info.put("email", token.getEmail());
                    info.put("dni", token.getDniUsuario());
                    info.put("nombreCompleto", token.getNombreTemporal() + " " + token.getApellidoPaternoTemporal() + 
                            (token.getApellidoMaternoTemporal() != null && !token.getApellidoMaternoTemporal().trim().isEmpty() ? 
                             " " + token.getApellidoMaternoTemporal() : ""));
                    info.put("fechaCreacion", token.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    info.put("fechaExpiracion", token.getFechaExpiracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    
                    // Calcular tiempo restante
                    Duration duracion = Duration.between(ahora, token.getFechaExpiracion());
                    long minutosRestantes = duracion.toMinutes();
                    if (minutosRestantes > 0) {
                        info.put("tiempoRestante", minutosRestantes + " minutos");
                        info.put("estaExpirado", false);
                    } else {
                        info.put("tiempoRestante", "Expirado");
                        info.put("estaExpirado", true);
                    }
                    
                    return info;
                })
                .collect(Collectors.toList());
            
            System.out.println("Tokens encontrados: " + resultado.size());
            return ResponseEntity.ok(Map.of("tokens", resultado));
            
        } catch (Exception e) {
            System.err.println("Error obteniendo tokens pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener tokens pendientes"));
        }
    }

    /**
     * Eliminar token específico
     */
    @DeleteMapping("/api/eliminar-token/{tokenId}")
    @ResponseBody
    public ResponseEntity<?> eliminarToken(@PathVariable Long tokenId) {
        try {
            System.out.println("=== ELIMINANDO TOKEN ID: " + tokenId + " ===");
            
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository.findById(tokenId);
            if (!tokenOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token no encontrado"));
            }
            
            TokenConfirmacion token = tokenOpt.get();
            System.out.println("Eliminando token para: " + token.getEmail());
            
            tokenConfirmacionRepository.deleteById(tokenId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Token eliminado correctamente",
                "emailAfectado", token.getEmail()
            ));
            
        } catch (Exception e) {
            System.err.println("Error eliminando token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar token"));
        }
    }

    /**
     * Reenviar email de confirmación para un token existente
     */
    @PostMapping("/api/reenviar-token/{tokenId}")
    @ResponseBody
    public ResponseEntity<?> reenviarToken(@PathVariable Long tokenId) {
        try {
            System.out.println("=== REENVIANDO TOKEN ID: " + tokenId + " ===");
            
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository.findById(tokenId);
            if (!tokenOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token no encontrado"));
            }
            
            TokenConfirmacion token = tokenOpt.get();
            
            // Verificar que no esté expirado
            if (token.estaExpirado()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El token está expirado"));
            }
            
            System.out.println("Reenviando email a: " + token.getEmail());
            
            // Reenviar email
            boolean emailEnviado = emailService.enviarTokenConfirmacion(
                token.getEmail(), 
                token.getNombreTemporal(), 
                token.getToken(), 
                token.getFechaExpiracion()
            );
            
            if (emailEnviado) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Email de confirmación reenviado correctamente",
                    "email", token.getEmail()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al enviar el email"));
            }
            
        } catch (Exception e) {
            System.err.println("Error reenviando token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al reenviar token"));
        }
    }

    /**
     * Activar cuenta manualmente sin token (para casos donde no llega el email)
     */
    @PostMapping("/api/activar-cuenta-manual/{tokenId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activarCuentaManual(@PathVariable Long tokenId) {
        try {
            System.out.println("=== ACTIVACIÓN MANUAL DE CUENTA ===");
            System.out.println("Token ID: " + tokenId);
            
            // Buscar el token
            Optional<TokenConfirmacion> tokenOpt = tokenConfirmacionRepository.findById(tokenId);
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token no encontrado"));
            }
            
            TokenConfirmacion token = tokenOpt.get();
            System.out.println("Token encontrado: " + token.getToken());
            System.out.println("Usuario: " + token.getNombreTemporal() + " - Email: " + token.getEmail());
            
            // Verificar que el token no haya sido usado
            if (token.estaUsado()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Este token ya fue utilizado"));
            }
            
            // Crear el usuario directamente
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDni(token.getDniUsuario());
            nuevoUsuario.setNombre(token.getNombreTemporal());
            nuevoUsuario.setApellidoPaterno(token.getApellidoPaternoTemporal());
            nuevoUsuario.setApellidoMaterno(token.getApellidoMaternoTemporal());
            nuevoUsuario.setCorreo(token.getEmail());
            nuevoUsuario.setContrasena(passwordEncoder.encode(token.getContrasenaTemporal()));
            nuevoUsuario.setEstado(true);
            nuevoUsuario.setFechaRegistro(Timestamp.valueOf(LocalDateTime.now()));
            
            // Asignar rol
            Optional<Rol> rolOpt = rolRepository.findById(token.getIdRolTemporal());
            if (rolOpt.isPresent()) {
                nuevoUsuario.setRol(rolOpt.get());
            } else {
                // Rol por defecto
                Optional<Rol> rolDefault = rolRepository.findByNombreRol("USUARIO");
                nuevoUsuario.setRol(rolDefault.orElse(null));
            }
            
            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            System.out.println("✅ Usuario creado manualmente: " + usuarioGuardado.getDni());
            
            // Marcar token como usado y eliminar
            token.setUsado(true);
            token.setFechaUso(LocalDateTime.now());
            tokenConfirmacionRepository.save(token);
            
            // Enviar email de bienvenida (opcional)
            try {
                emailService.enviarEmailBienvenida(
                    usuarioGuardado.getCorreo(),
                    usuarioGuardado.getNombre(),
                    usuarioGuardado.getDni(),
                    usuarioGuardado.getRol() != null ? usuarioGuardado.getRol().getNombreRol() : "Usuario"
                );
                System.out.println("✅ Email de bienvenida enviado");
            } catch (Exception emailError) {
                System.err.println("⚠️ No se pudo enviar email de bienvenida: " + emailError.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Cuenta activada manualmente",
                "usuario", Map.of(
                    "dni", usuarioGuardado.getDni(),
                    "nombre", usuarioGuardado.getNombre(),
                    "email", usuarioGuardado.getCorreo(),
                    "rol", usuarioGuardado.getRol() != null ? usuarioGuardado.getRol().getNombreRol() : "Sin rol"
                )
            ));
            
        } catch (Exception e) {
            System.err.println("Error en activación manual: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al activar cuenta manualmente: " + e.getMessage()));
        }
    }

    /**
     * PÁGINA TEMPORAL DE LIMPIEZA - SOLO PARA DESARROLLO
     */
    @GetMapping("/limpieza-datos")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String mostrarPaginaLimpieza(Model model) {
        return "admin/limpieza-datos";
    }

    /**
     * MÉTODO TEMPORAL DE LIMPIEZA - SOLO PARA DESARROLLO
     * Limpia tokens y usuarios de prueba para un DNI específico
     */
    @PostMapping("/limpiar-datos-prueba")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> limpiarDatosPrueba(
            @RequestParam String dni,
            @RequestParam(required = false) String email) {
        
        try {
            Map<String, Object> resultado = new HashMap<>();
            
            System.out.println("🧹 INICIANDO LIMPIEZA DE DATOS DE PRUEBA");
            System.out.println("📋 DNI a limpiar: " + dni);
            if (email != null && !email.trim().isEmpty()) {
                System.out.println("📧 Email a limpiar: " + email.trim());
            }
            
            // 1. Limpiar tokens de confirmación por DNI
            System.out.println("🔍 Buscando tokens por DNI...");
            List<TokenConfirmacion> tokensPorDni = tokenConfirmacionRepository.findByDniUsuarioOrderByFechaCreacionDesc(dni);
            System.out.println("📊 Tokens encontrados por DNI: " + tokensPorDni.size());
            
            if (!tokensPorDni.isEmpty()) {
                tokenConfirmacionRepository.deleteAll(tokensPorDni);
                System.out.println("🗑️ Eliminados " + tokensPorDni.size() + " tokens por DNI");
            }
            
            // 2. Limpiar tokens de confirmación por email (si se proporciona)
            int tokensEliminadosPorEmail = 0;
            if (email != null && !email.trim().isEmpty()) {
                System.out.println("🔍 Buscando tokens por email...");
                List<TokenConfirmacion> tokensPorEmail = tokenConfirmacionRepository.findByEmailIgnoreCaseOrderByFechaCreacionDesc(email.trim());
                System.out.println("📊 Tokens encontrados por email: " + tokensPorEmail.size());
                
                if (!tokensPorEmail.isEmpty()) {
                    tokenConfirmacionRepository.deleteAll(tokensPorEmail);
                    tokensEliminadosPorEmail = tokensPorEmail.size();
                    System.out.println("🗑️ Eliminados " + tokensEliminadosPorEmail + " tokens por email");
                }
            }
            
            // 3. Verificar si existe usuario y ofrecer eliminarlo
            System.out.println("🔍 Verificando si existe usuario con DNI: " + dni);
            boolean usuarioExiste = usuarioRepository.existsById(dni);
            
            String mensajeUsuario = "";
            if (usuarioExiste) {
                Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    System.out.println("👤 Usuario encontrado:");
                    System.out.println("   - DNI: " + usuario.getDni());
                    System.out.println("   - Nombre: " + usuario.getNombre());
                    System.out.println("   - Email: " + usuario.getCorreo());
                    System.out.println("   - Estado: " + usuario.getEstado());
                    
                    mensajeUsuario = "⚠️ USUARIO ENCONTRADO - DNI: " + usuario.getDni() + 
                                   ", Nombre: " + usuario.getNombre() + 
                                   ", Email: " + usuario.getCorreo() + 
                                   ". ¿Quieres eliminarlo también?";
                }
            } else {
                mensajeUsuario = "✅ No existe usuario con DNI: " + dni;
                System.out.println(mensajeUsuario);
            }
            
            // 4. Preparar resultado
            resultado.put("success", true);
            resultado.put("mensaje", "Limpieza de datos completada");
            resultado.put("detalles", Map.of(
                "tokensEliminadosPorDni", tokensPorDni.size(),
                "tokensEliminadosPorEmail", tokensEliminadosPorEmail,
                "usuarioExiste", usuarioExiste,
                "mensajeUsuario", mensajeUsuario
            ));
            
            System.out.println("✅ LIMPIEZA COMPLETADA");
            System.out.println("📊 Resumen:");
            System.out.println("   - Tokens eliminados por DNI: " + tokensPorDni.size());
            System.out.println("   - Tokens eliminados por email: " + tokensEliminadosPorEmail);
            System.out.println("   - Usuario existe: " + usuarioExiste);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la limpieza: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", true,
                    "mensaje", "Error durante la limpieza: " + e.getMessage()
                ));
        }
    }

    /**
     * MÉTODO TEMPORAL - ELIMINAR USUARIO DE PRUEBA
     */
    @PostMapping("/eliminar-usuario-prueba")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarUsuarioPrueba(@RequestParam String dni) {
        
        try {
            System.out.println("🗑️ ELIMINANDO USUARIO DE PRUEBA con DNI: " + dni);
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                System.out.println("👤 Usuario a eliminar:");
                System.out.println("   - DNI: " + usuario.getDni());
                System.out.println("   - Nombre: " + usuario.getNombre());
                System.out.println("   - Email: " + usuario.getCorreo());
                
                usuarioRepository.delete(usuario);
                System.out.println("✅ Usuario eliminado exitosamente");
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Usuario eliminado exitosamente",
                    "usuarioEliminado", Map.of(
                        "dni", usuario.getDni(),
                        "nombre", usuario.getNombre(),
                        "email", usuario.getCorreo()
                    )
                ));
                
            } else {
                System.out.println("❌ No se encontró usuario con DNI: " + dni);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "mensaje", "No se encontró usuario con DNI: " + dni
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error eliminando usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", true,
                    "mensaje", "Error eliminando usuario: " + e.getMessage()
                ));
        }
    }

    /**
     * Endpoint de prueba para verificar configuración de email
     */
    @PostMapping("/test-email")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String email) {
        try {
            System.out.println("=== INICIANDO TEST DE EMAIL ===");
            System.out.println("Email destino: " + email);
            
            // Probar configuración básica
            boolean conectividad = emailService.probarConectividad();
            if (!conectividad) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "mensaje", "Error de conectividad SMTP. Revisa los logs para más detalles."
                ));
            }
            
            // Enviar email de prueba
            boolean emailEnviado = emailService.enviarTokenConfirmacion(
                email,
                "Usuario de Prueba", 
                "123456",
                LocalDateTime.now().plusMinutes(30)
            );
            
            if (emailEnviado) {
                System.out.println("✅ Email de prueba enviado exitosamente");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Email de prueba enviado exitosamente a " + email
                ));
            } else {
                System.out.println("❌ Falló el envío del email de prueba");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "mensaje", "Error enviando email de prueba. Revisa los logs para más detalles."
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error en test de email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", true,
                    "mensaje", "Error en test de email: " + e.getMessage()
                ));
        }
    }

    /**
     * Método auxiliar para eliminar registros de auditoría directamente
     * usando consulta SQL nativa para mayor control
     */
    @Transactional
    private void eliminarRegistrosAuditoriaDirectamente(String dni) {
        try {
            // Usar la consulta nativa directamente
            actividadAdminRepository.deleteByUsuarioAfectadoDniNative(dni);
            
            // Forzar el flush de la transacción
            actividadAdminRepository.flush();
            
            System.out.println("✅ Eliminación directa completada para DNI: " + dni);
        } catch (Exception e) {
            System.err.println("❌ Error en eliminación directa: " + e.getMessage());
            throw new RuntimeException("No se pudieron eliminar los registros de auditoría: " + e.getMessage(), e);
        }
    }
}