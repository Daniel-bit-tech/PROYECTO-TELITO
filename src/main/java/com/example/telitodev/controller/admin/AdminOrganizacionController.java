package com.example.telitodev.controller.admin;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Organizacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.repository.OrganizacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import com.example.telitodev.service.OrganizacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/organizaciones")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminOrganizacionController extends BaseController {

    private final OrganizacionRepository organizacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final OrganizacionService organizacionService;

    public AdminOrganizacionController(OrganizacionRepository organizacionRepository,
                                      UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository,
                                      OrganizacionService organizacionService) {
        this.organizacionRepository = organizacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.organizacionService = organizacionService;
    }

    /**
     * Vista principal de gestión de organizaciones
     */
    @GetMapping
    public String gestionOrganizaciones(Model model, Authentication auth, HttpSession session) {
        try {
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            addImpersonationAttributes(model, session);

            // Obtener todas las organizaciones con estadísticas
            List<Organizacion> organizaciones = organizacionRepository.findAllOrderByNombre();
            
            // Crear lista de organizaciones con sus estadísticas
            List<Map<String, Object>> orgStats = new ArrayList<>();
            for (Organizacion org : organizaciones) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("organizacion", org);
                
                // Contar usuarios por rol
                List<Usuario> usuarios = usuarioRepository.findByOrganizacionIdWithRol(org.getIdOrganizacion());
                stats.put("totalUsuarios", usuarios.size());
                stats.put("usuariosActivos", usuarios.stream().filter(Usuario::getEstado).count());
                
                // Agrupar por roles
                Map<String, Long> usuariosPorRol = usuarios.stream()
                    .collect(Collectors.groupingBy(u -> u.getRol().getNombreRol(), Collectors.counting()));
                stats.put("usuariosPorRol", usuariosPorRol);
                
                orgStats.add(stats);
            }
            
            model.addAttribute("organizaciones", orgStats);
            model.addAttribute("roles", rolRepository.findAll());
            
            return "admin/gestion-organizaciones";
            
        } catch (Exception e) {
            System.err.println("❌ Error en gestión de organizaciones: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar organizaciones: " + e.getMessage());
            return "admin/gestion-organizaciones";
        }
    }

    /**
     * API: Obtener detalles de una organización con sus usuarios
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrganizacionDetalle(@PathVariable Integer id) {
        try {
            Optional<Organizacion> orgOpt = organizacionRepository.findById(id);
            if (!orgOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Organizacion org = orgOpt.get();
            List<Usuario> usuarios = usuarioRepository.findByOrganizacionIdWithRol(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("organizacion", Map.of(
                "id", org.getIdOrganizacion(),
                "nombre", org.getNombre(),
                "descripcion", org.getDescripcion() != null ? org.getDescripcion() : "",
                "dominioCorreo", org.getDominioCorreo() != null ? org.getDominioCorreo() : "",
                "publica", org.getPublica(),
                "fechaCreacion", org.getFechaCreacion()
            ));
            
            List<Map<String, Object>> usuariosData = usuarios.stream()
                .map(u -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("dni", u.getDni());
                    userData.put("nombre", u.getNombre() + " " + u.getApellidoPaterno() + " " + u.getApellidoMaterno());
                    userData.put("correo", u.getCorreo());
                    userData.put("rol", u.getRol().getNombreRol());
                    userData.put("estado", u.getEstado());
                    return userData;
                })
                .collect(Collectors.toList());
            
            response.put("usuarios", usuariosData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Mover usuario a otra organización
     */
    @PostMapping("/api/mover-usuario")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> moverUsuario(@RequestBody Map<String, Object> datos) {
        try {
            String dni = (String) datos.get("dni");
            Integer nuevaOrgId = Integer.valueOf(datos.get("nuevaOrganizacionId").toString());
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dni);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Optional<Organizacion> orgOpt = organizacionRepository.findById(nuevaOrgId);
            if (!orgOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Organización de destino no encontrada"));
            }
            
            Usuario usuario = usuarioOpt.get();
            Organizacion organizacionAntigua = usuario.getOrganizacion();
            Organizacion nuevaOrganizacion = orgOpt.get();
            
            usuario.setOrganizacion(nuevaOrganizacion);
            usuarioRepository.save(usuario);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario movido exitosamente de '" + 
                          (organizacionAntigua != null ? organizacionAntigua.getNombre() : "Sin organización") + 
                          "' a '" + nuevaOrganizacion.getNombre() + "'"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al mover usuario: " + e.getMessage()));
        }
    }

    /**
     * API: Eliminar/disolver organización
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> eliminarOrganizacion(@PathVariable Integer id) {
        try {
            Optional<Organizacion> orgOpt = organizacionRepository.findById(id);
            if (!orgOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Organizacion org = orgOpt.get();
            List<Usuario> usuarios = usuarioRepository.findByOrganizacionIdWithRol(id);
            
            if (!usuarios.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "No se puede eliminar la organización porque tiene " + 
                                usuarios.size() + " usuario(s) asignado(s). " +
                                "Primero mueva los usuarios a otra organización."
                    ));
            }
            
            organizacionRepository.delete(org);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Organización '" + org.getNombre() + "' eliminada exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al eliminar organización: " + e.getMessage()));
        }
    }

    /**
     * API: Crear nueva organización (solo la estructura, sin usuarios)
     */
    @PostMapping("/api/crear")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> crearOrganizacion(@RequestBody Map<String, Object> datos) {
        try {
            String nombre = (String) datos.get("nombre");
            String descripcion = datos.containsKey("descripcion") ? (String) datos.get("descripcion") : "";
            String dominioCorreo = datos.containsKey("dominioCorreo") ? (String) datos.get("dominioCorreo") : "";
            Boolean publica = datos.containsKey("publica") ? (Boolean) datos.get("publica") : false;
            
            // Validar nombre único
            if (organizacionRepository.existsByNombre(nombre)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ya existe una organización con ese nombre"));
            }
            
            // Validar dominio único si se proporciona
            if (!dominioCorreo.isEmpty() && organizacionRepository.existsByDominioCorreo(dominioCorreo)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ya existe una organización con ese dominio de correo"));
            }
            
            Organizacion nuevaOrg = new Organizacion();
            nuevaOrg.setNombre(nombre);
            nuevaOrg.setDescripcion(descripcion);
            nuevaOrg.setDominioCorreo(dominioCorreo.isEmpty() ? null : dominioCorreo);
            nuevaOrg.setPublica(publica);
            nuevaOrg.setFechaCreacion(new Date(System.currentTimeMillis()));
            
            Organizacion guardada = organizacionRepository.save(nuevaOrg);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Organización '" + nombre + "' creada exitosamente",
                "organizacion", Map.of(
                    "id", guardada.getIdOrganizacion(),
                    "nombre", guardada.getNombre()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al crear organización: " + e.getMessage()));
        }
    }

    /**
     * API: Actualizar datos de organización
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> actualizarOrganizacion(@PathVariable Integer id, 
                                                    @RequestBody Map<String, Object> datos) {
        try {
            Optional<Organizacion> orgOpt = organizacionRepository.findById(id);
            if (!orgOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Organizacion org = orgOpt.get();
            
            if (datos.containsKey("nombre")) {
                String nuevoNombre = (String) datos.get("nombre");
                if (!nuevoNombre.equals(org.getNombre()) && 
                    organizacionRepository.existsByNombre(nuevoNombre)) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ya existe una organización con ese nombre"));
                }
                org.setNombre(nuevoNombre);
            }
            
            if (datos.containsKey("descripcion")) {
                org.setDescripcion((String) datos.get("descripcion"));
            }
            
            if (datos.containsKey("publica")) {
                org.setPublica((Boolean) datos.get("publica"));
            }
            
            organizacionRepository.save(org);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Organización actualizada exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al actualizar organización: " + e.getMessage()));
        }
    }
}
