package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/po")
public class OrganizacionController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final ProyectoRepository proyectoRepository;
    private final SolAccesoOrgService solAccesoOrgService;
    private final OrganizacionService organizacionService;
    private final ApiRepository apiRepository;

    public OrganizacionController(UsuarioRepository usuarioRepository,
                                  OrganizacionRepository organizacionRepository,
                                  ProyectoRepository proyectoRepository,
                                  SolAccesoOrgService solAccesoOrgService,
                                  OrganizacionService organizacionService,
                                  ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacionRepository = organizacionRepository;
        this.proyectoRepository = proyectoRepository;
        this.solAccesoOrgService = solAccesoOrgService;
        this.organizacionService = organizacionService;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/organizacion")
    public String showOrganizacion(Model model, Authentication auth, HttpSession session) {
        try {
            // 1. Obtener usuario logueado
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            // Agregar atributos de impersonación
            addImpersonationAttributes(model, session);

            // 2. Obtener organización del usuario
            Organizacion organizacion = organizacionRepository.findByUsuarioDni(usuario.getDni());

            if (organizacion == null) {
                // Si no tiene organización, mostrar página vacía
                return "po/organizacion";
            }

            model.addAttribute("organizacion", organizacion);

            // 3. Obtener miembros de la organización (con roles cargados)
            List<Usuario> miembros = usuarioRepository.findByOrganizacionIdWithRol(organizacion.getIdOrganizacion());
            model.addAttribute("miembros", miembros);

            // 4. Obtener proyectos activos de la organización
            List<Proyecto> proyectosActivos = proyectoRepository.findProyectosActivosByOrganizacionId(organizacion.getIdOrganizacion());
            model.addAttribute("proyectosActivos", proyectosActivos);

            // 5. Obtener APIs únicas de la organización
            List<Api> apisUnicas = apiRepository.findByOrganizacionId(organizacion.getIdOrganizacion());
            model.addAttribute("apis", apisUnicas);

            return "po/organizacion";

        } catch (Exception e) {
            // En caso de error, igual mostrar la página pero sin datos adicionales
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            addImpersonationAttributes(model, session);
            return "po/organizacion";
        }
    }

    @GetMapping("/historialSolicitudes")
    public String mostrarHistorialSolicitudes(Model model, Authentication auth, HttpSession session) {
        try {
            // 1. Obtener usuario logueado (considerando impersonación)
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);

            // Agregar atributos de impersonación
            addImpersonationAttributes(model, session);
            
            // 2. Obtener organización del usuario
            Organizacion organizacion = organizacionRepository.findByUsuarioDni(usuario.getDni());
            model.addAttribute("organizacion", organizacion);

            // 3. OBTENER SOLICITUDES REALES DEL PO
            List<SolAccesoEquipo> solicitudes = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());
            model.addAttribute("solicitudes", solicitudes);

            // 4. Calcular estadísticas reales
            long totalSolicitudes = solicitudes.size();
            long solicitudesPendientes = solicitudes.stream()
                    .filter(SolAccesoEquipo::isPendiente)
                    .count();
            long solicitudesAprobadas = solicitudes.stream()
                    .filter(SolAccesoEquipo::isAprobada)
                    .count();
            long solicitudesRechazadas = solicitudes.stream()
                    .filter(SolAccesoEquipo::isRechazada)
                    .count();

            model.addAttribute("totalSolicitudes", totalSolicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);
            model.addAttribute("solicitudesRechazadas", solicitudesRechazadas);

            return "po/historialSolicitudes";

        } catch (Exception e) {
            // En caso de error, mostrar la página básica
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            // Valores por defecto
            model.addAttribute("solicitudes", Collections.emptyList());
            model.addAttribute("totalSolicitudes", 0);
            model.addAttribute("solicitudesPendientes", 0);
            model.addAttribute("solicitudesAprobadas", 0);
            model.addAttribute("solicitudesRechazadas", 0);

            return "po/historialSolicitudes";
        }
    }

    @GetMapping("/solicitudAcceso")
    public String showSolicitudAcceso(Model model, Authentication auth, HttpSession session) {
        try {
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            addImpersonationAttributes(model, session);

            List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
            model.addAttribute("organizaciones", organizaciones != null ? organizaciones : new ArrayList<>());

            List<SolAccesoEquipo> solicitudesUsuario = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());

            long totalSolicitudes = solicitudesUsuario != null ? solicitudesUsuario.size() : 0;
            long solicitudesPendientes = solicitudesUsuario != null ? solicitudesUsuario.stream()
                    .filter(SolAccesoEquipo::isPendiente)
                    .count() : 0;
            long solicitudesAprobadas = solicitudesUsuario != null ? solicitudesUsuario.stream()
                    .filter(SolAccesoEquipo::isAprobada)
                    .count() : 0;

            model.addAttribute("totalSolicitudes", totalSolicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);

            boolean esAdmin = usuario.getRol() != null &&
                              (usuario.getRol().getIdRol() == 1 || usuario.getRol().getIdRol() == 2);
            model.addAttribute("esAdmin", esAdmin);

            if (esAdmin) {
                long totalPendientesGlobal = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoEquipo.EstadoSolicitud.PENDIENTE);
                model.addAttribute("totalPendientesGlobal", totalPendientesGlobal);
            }

            SolAccesoEquipo solicitud = new SolAccesoEquipo();
            model.addAttribute("solicitud", solicitud);

            return "po/solicitudAcceso";

        } catch (Exception e) {
            try {
                Usuario usuario = usuarioRepository.findByCorreo(auth != null ? auth.getName() : "");
                if (usuario != null) {
                    model.addAttribute("usuario", usuario);
                }
            } catch (Exception ex) {
                // Ignorar
            }

            model.addAttribute("organizaciones", new ArrayList<>());
            model.addAttribute("solicitud", new SolAccesoEquipo());
            model.addAttribute("totalSolicitudes", 0L);
            model.addAttribute("solicitudesPendientes", 0L);
            model.addAttribute("solicitudesAprobadas", 0L);
            model.addAttribute("esAdmin", false);
            model.addAttribute("error", "Error al cargar el formulario. Por favor, intenta nuevamente.");

            return "po/solicitudAcceso";
        }
    }

    @PostMapping("/solicitudAcceso")
    public String procesarSolicitudAcceso(
            @ModelAttribute("solicitud") SolAccesoEquipo solicitud,
            @RequestParam Integer idOrganizacionDestino,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioSolicitante = usuarioRepository.findByCorreo(auth.getName());
            Optional<Usuario> usuarioTargetOpt = usuarioRepository.findOptionalByDni(solicitud.getDni());

            if (usuarioTargetOpt.isPresent()) {
                Usuario usuarioTarget = usuarioTargetOpt.get();

                if (solAccesoOrgService.existeSolicitudPendienteParaDni(solicitud.getDni())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Ya existe una solicitud pendiente para el DNI: " + solicitud.getDni());
                    return "redirect:/po/solicitudAcceso";
                }

                if (solAccesoOrgService.existeSolicitudAprobadaParaDni(solicitud.getDni()))  {
                    redirectAttributes.addFlashAttribute("error",
                            "El usuario " + usuarioTarget.getNombre() + " " + usuarioTarget.getApellidoPaterno() +
                                    " ya pertenece a un equipo. No puede ser agregado a otro.");
                    return "redirect:/po/solicitudAcceso";
                }

                Equipo equipoDestino = new Equipo();
                equipoDestino.setIdEquipo(idOrganizacionDestino);
                solicitud.setEquipoDestino(equipoDestino);

                SolAccesoEquipo solicitudGuardada = solAccesoOrgService.crearSolicitudParaUsuarioExistente(
                        solicitud, usuarioSolicitante.getDni()
                );

                redirectAttributes.addFlashAttribute("success",
                        "✅ Solicitud enviada exitosamente para " + usuarioTarget.getNombre() +
                                " " + usuarioTarget.getApellidoPaterno() + ". ID: " + solicitudGuardada.getIdSolicitudEquipo());
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "El usuario con DNI " + solicitud.getDni() + " no está registrado en el sistema. " +
                                "Debe registrarse primero antes de solicitar acceso a un equipo.");
                return "redirect:/po/solicitudAcceso";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al enviar solicitud: " + e.getMessage());
        }

        return "redirect:/po/solicitudAcceso";
    }

    @GetMapping("/misSolicitudes")
    public String mostrarMisSolicitudes(Model model, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            List<SolAccesoEquipo> solicitudes = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());
            model.addAttribute("solicitudes", solicitudes);

            long totalSolicitudes = solicitudes.size();
            long pendientes = solicitudes.stream()
                    .filter(SolAccesoEquipo::isPendiente)
                    .count();
            long aprobadas = solicitudes.stream()
                    .filter(SolAccesoEquipo::isAprobada)
                    .count();
            long rechazadas = solicitudes.stream()
                    .filter(SolAccesoEquipo::isRechazada)
                    .count();

            model.addAttribute("totalSolicitudes", totalSolicitudes);
            model.addAttribute("solicitudesPendientes", pendientes);
            model.addAttribute("solicitudesAprobadas", aprobadas);
            model.addAttribute("solicitudesRechazadas", rechazadas);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar solicitudes: " + e.getMessage());
        }

        return "po/misSolicitudes";
    }

    @GetMapping("/gestionSolicitudes")
    public String mostrarGestionSolicitudes(Model model, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            boolean esAdmin = usuario.getRol().getIdRol() == 1 || usuario.getRol().getIdRol() == 2;
            if (!esAdmin) {
                model.addAttribute("error", "No tienes permisos para acceder a esta página");
                return "redirect:/po/organizacion";
            }

            List<SolAccesoEquipo> solicitudesPendientes = solAccesoOrgService.obtenerSolicitudesPendientes();
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);

            List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
            model.addAttribute("organizaciones", organizaciones);

            long totalPendientes = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoEquipo.EstadoSolicitud.PENDIENTE);
            long totalAprobadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoEquipo.EstadoSolicitud.APROBADA);
            long totalRechazadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoEquipo.EstadoSolicitud.RECHAZADA);

            model.addAttribute("totalPendientes", totalPendientes);
            model.addAttribute("totalAprobadas", totalAprobadas);
            model.addAttribute("totalRechazadas", totalRechazadas);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar solicitudes: " + e.getMessage());
        }

        return "po/gestionSolicitudes";
    }

    @PostMapping("/aprobarSolicitud/{id}")
    public String aprobarSolicitud(
            @PathVariable Integer id,
            @RequestParam(required = false) String comentarios,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario admin = usuarioRepository.findByCorreo(auth.getName());
            SolAccesoEquipo solicitud = solAccesoOrgService.aprobarSolicitud(id, admin.getDni(), comentarios);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Solicitud #" + id + " aprobada exitosamente. Se ha notificado al usuario.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al aprobar solicitud: " + e.getMessage());
        }

        return "redirect:/po/gestionSolicitudes";
    }

    @PostMapping("/rechazarSolicitud/{id}")
    public String rechazarSolicitud(
            @PathVariable Integer id,
            @RequestParam(required = false) String comentarios,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario admin = usuarioRepository.findByCorreo(auth.getName());
            SolAccesoEquipo solicitud = solAccesoOrgService.rechazarSolicitud(id, admin.getDni(), comentarios);

            redirectAttributes.addFlashAttribute("success",
                    "❌ Solicitud #" + id + " rechazada. Se ha notificado al usuario.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al rechazar solicitud: " + e.getMessage());
        }

        return "redirect:/po/gestionSolicitudes";
    }

    @GetMapping("/verificarUsuario")
    @ResponseBody
    public Map<String, Object> verificarUsuario(@RequestParam String dni) {
        Map<String, Object> response = new HashMap<>();

        Optional<Usuario> usuarioOpt = usuarioRepository.findOptionalByDni(dni);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            response.put("existe", true);
            response.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellidoPaterno());
            response.put("correo", usuario.getCorreo());

            boolean tieneOrganizacion = solAccesoOrgService.existeSolicitudAprobadaParaDni(dni);
            response.put("tieneOrganizacion", tieneOrganizacion);

        } else {
            response.put("existe", false);
            response.put("mensaje", "Usuario no registrado en el sistema");
        }

        return response;
    }
}
