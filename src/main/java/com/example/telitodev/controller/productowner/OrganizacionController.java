package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/po")
public class OrganizacionController {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final ProyectoRepository proyectoRepository;
    private final SolAccesoOrgService solAccesoOrgService;
    private final OrganizacionService organizacionService;

    // AGREGAR los nuevos servicios al constructor
    public OrganizacionController(UsuarioRepository usuarioRepository,
                                  OrganizacionRepository organizacionRepository,
                                  ProyectoRepository proyectoRepository,
                                  SolAccesoOrgService solAccesoOrgService,
                                  OrganizacionService organizacionService) {
        this.usuarioRepository = usuarioRepository;
        this.organizacionRepository = organizacionRepository;
        this.proyectoRepository = proyectoRepository;
        this.solAccesoOrgService = solAccesoOrgService;
        this.organizacionService = organizacionService;
    }

    @GetMapping("/organizacion")
    public String showOrganizacion(Model model, Authentication auth) {
        try {
            // 1. Obtener usuario logueado
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

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
            List<Api> apisUnicas = obtenerApisUnicasDeOrganizacion(organizacion);
            model.addAttribute("apis", apisUnicas);

            return "po/organizacion";

        } catch (Exception e) {
            // En caso de error, igual mostrar la página pero sin datos adicionales
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);
            return "po/organizacion";
        }
    }

    // MÉTODO ORIGINAL - SIN CAMBIOS
    @GetMapping("/solicitudAcceso")
    public String showSolicitudAcceso(Model model, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            // 1. Cargar organizaciones para el dropdown
            List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
            model.addAttribute("organizaciones", organizaciones);

            // 2. Cargar estadísticas de solicitudes del usuario
            List<SolAccesoOrg> solicitudesUsuario = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());

            long totalSolicitudes = solicitudesUsuario.size();
            long solicitudesPendientes = solicitudesUsuario.stream()
                    .filter(SolAccesoOrg::isPendiente)
                    .count();
            long solicitudesAprobadas = solicitudesUsuario.stream()
                    .filter(SolAccesoOrg::isAprobada)
                    .count();

            model.addAttribute("totalSolicitudes", totalSolicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);

            // 3. Verificar si el usuario es administrador para mostrar enlace de gestión
            boolean esAdmin = usuario.getRol().getIdRol() == 1 || usuario.getRol().getIdRol() == 2;
            model.addAttribute("esAdmin", esAdmin);

            if (esAdmin) {
                // 4. Si es admin, cargar estadísticas globales
                long totalPendientesGlobal = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.PENDIENTE);
                model.addAttribute("totalPendientesGlobal", totalPendientesGlobal);
            }

            // 5. Crear objeto vacío para el formulario (para th:object)
            SolAccesoOrg solicitud = new SolAccesoOrg();
            model.addAttribute("solicitud", solicitud);

            return "po/solicitudAcceso";

        } catch (Exception e) {
            // En caso de error, cargar datos básicos
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "po/solicitudAcceso";
        }
    }

    // Método auxiliar para obtener APIs únicas de la organización
    private List<Api> obtenerApisUnicasDeOrganizacion(Organizacion organizacion) {
        try {
            // Opción 1: Usar el método del repository si existe
            List<Proyecto> proyectosConApis = proyectoRepository.findByOrganizacionIdWithApis(organizacion.getIdOrganizacion());

            if (proyectosConApis != null && !proyectosConApis.isEmpty()) {
                return proyectosConApis.stream()
                        .filter(proyecto -> proyecto.getProyectoHasApis() != null)
                        .flatMap(proyecto -> proyecto.getProyectoHasApis().stream())
                        .map(ProyectoHasApi::getApi)
                        .distinct()
                        .collect(Collectors.toList());
            }

            // Opción 2: Si no hay proyectos con APIs, usar las relaciones lazy
            if (organizacion.getProyectos() != null) {
                return organizacion.getProyectos().stream()
                        .filter(proyecto -> proyecto.getProyectoHasApis() != null)
                        .flatMap(proyecto -> proyecto.getProyectoHasApis().stream())
                        .map(ProyectoHasApi::getApi)
                        .distinct()
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            // Si hay error de lazy loading, devolver lista vacía
        }

        return new ArrayList<>();
    }


    // ==============================================
    // MÉTODOS NUEVOS PARA SOLICITUDES DE ACCESO
    // ==============================================

    /**
     * POST: Procesar el formulario de solicitud de acceso
     */
    /**
     * POST: Procesar el formulario de solicitud de acceso (VERSIÓN CORREGIDA)
     */
    /**
     * POST: Procesar el formulario de solicitud de acceso (VERSIÓN CORREGIDA - 3 CASOS)
     */
    @PostMapping("/solicitudAcceso")
    public String procesarSolicitudAcceso(
            @ModelAttribute("solicitud") SolAccesoOrg solicitud,
            @RequestParam Integer idOrganizacionDestino,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DEBUG INICIO ===");
        System.out.println("DNI: " + solicitud.getDni());
        System.out.println("Nombre: " + solicitud.getNombre());
        System.out.println("Apellido: " + solicitud.getApellido());
        System.out.println("Correo: " + solicitud.getCorreo());
        System.out.println("Organización Destino: " + idOrganizacionDestino);
        System.out.println("Usuario autenticado: " + auth.getName());

        try {
            // Obtener usuario que envía la solicitud (PO)
            Usuario usuarioSolicitante = usuarioRepository.findByCorreo(auth.getName());
            System.out.println("Usuario solicitante encontrado: " + usuarioSolicitante.getDni());

            // ========== IMPLEMENTACIÓN DE LOS 3 CASOS ==========

            // CASO 1 y 2: Verificar si el usuario EXISTE en el sistema
            Optional<Usuario> usuarioTargetOpt = usuarioRepository.findOptionalByDni(solicitud.getDni());

            if (usuarioTargetOpt.isPresent()) {
                // USUARIO EXISTE - Caso 1 o 2
                Usuario usuarioTarget = usuarioTargetOpt.get();
                System.out.println("✅ Usuario encontrado: " + usuarioTarget.getNombre() + " " + usuarioTarget.getApellidoPaterno());

                // Verificar si YA TIENE ORGANIZACIÓN APROBADA
                if (solAccesoOrgService.existeSolicitudPendienteParaDni(solicitud.getDni())) {
                    System.out.println("❌ Ya existe solicitud pendiente para DNI: " + solicitud.getDni());
                    redirectAttributes.addFlashAttribute("error",
                            "Ya existe una solicitud pendiente para el DNI: " + solicitud.getDni());
                    return "redirect:/po/solicitudAcceso";
                }

                // Verificar si YA TIENE ACCESO APROBADO
                if (solAccesoOrgService.existeSolicitudAprobadaParaDni(solicitud.getDni()))  {
                    System.out.println("❌ Usuario ya tiene organización aprobada: " + solicitud.getDni());
                    redirectAttributes.addFlashAttribute("error",
                            "El usuario " + usuarioTarget.getNombre() + " " + usuarioTarget.getApellidoPaterno() +
                                    " ya pertenece a una organización. No puede ser agregado a otra.");
                    return "redirect:/po/solicitudAcceso";
                }

                // CASO 1: Usuario EXISTE + SIN organización → CREAR SOLICITUD
                System.out.println("✅ Usuario existe y NO tiene organización - Creando solicitud...");

                // Crear organización destino
                Organizacion organizacionDestino = new Organizacion();
                organizacionDestino.setIdOrganizacion(idOrganizacionDestino);
                solicitud.setOrganizacionDestino(organizacionDestino);

                // Usar el NUEVO método para usuarios existentes
                SolAccesoOrg solicitudGuardada = solAccesoOrgService.crearSolicitudParaUsuarioExistente(
                        solicitud, usuarioSolicitante.getDni()
                );

                System.out.println("✅ SOLICITUD GUARDADA CON ID: " + solicitudGuardada.getIdSolicitudOrg());
                redirectAttributes.addFlashAttribute("success",
                        "✅ Solicitud enviada exitosamente para " + usuarioTarget.getNombre() +
                                " " + usuarioTarget.getApellidoPaterno() + ". ID: " + solicitudGuardada.getIdSolicitudOrg());

            } else {
                // CASO 3: Usuario NO EXISTE → ERROR
                System.out.println("❌ Usuario NO registrado en el sistema: " + solicitud.getDni());
                redirectAttributes.addFlashAttribute("error",
                        "El usuario con DNI " + solicitud.getDni() + " no está registrado en el sistema. " +
                                "Debe registrarse primero antes de solicitar acceso a una organización.");
                return "redirect:/po/solicitudAcceso";
            }

        } catch (Exception e) {
            System.out.println("=== ERROR CAPTURADO ===");
            System.out.println("Tipo de error: " + e.getClass().getName());
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println("=== STACK TRACE ===");
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al enviar solicitud: " + e.getMessage());
        }

        return "redirect:/po/solicitudAcceso";
    }

    /**
     * GET: Mostrar historial de solicitudes del usuario
     */
    @GetMapping("/misSolicitudes")
    public String mostrarMisSolicitudes(Model model, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            // Obtener solicitudes del usuario
            List<SolAccesoOrg> solicitudes = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());
            model.addAttribute("solicitudes", solicitudes);

            // Estadísticas
            long totalSolicitudes = solicitudes.size();
            long pendientes = solicitudes.stream()
                    .filter(SolAccesoOrg::isPendiente)
                    .count();
            long aprobadas = solicitudes.stream()
                    .filter(SolAccesoOrg::isAprobada)
                    .count();
            long rechazadas = solicitudes.stream()
                    .filter(SolAccesoOrg::isRechazada)
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

    /**
     * GET: Vista para administradores - Ver todas las solicitudes pendientes
     */
    @GetMapping("/gestionSolicitudes")
    public String mostrarGestionSolicitudes(Model model, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            // Verificar si el usuario es administrador
            boolean esAdmin = usuario.getRol().getIdRol() == 1 || usuario.getRol().getIdRol() == 2;
            if (!esAdmin) {
                model.addAttribute("error", "No tienes permisos para acceder a esta página");
                return "redirect:/po/organizacion";
            }

            // Obtener solicitudes pendientes
            List<SolAccesoOrg> solicitudesPendientes = solAccesoOrgService.obtenerSolicitudesPendientes();
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);

            // Obtener organizaciones para filtros
            List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
            model.addAttribute("organizaciones", organizaciones);

            // Estadísticas
            long totalPendientes = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.PENDIENTE);
            long totalAprobadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.APROBADA);
            long totalRechazadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.RECHAZADA);

            model.addAttribute("totalPendientes", totalPendientes);
            model.addAttribute("totalAprobadas", totalAprobadas);
            model.addAttribute("totalRechazadas", totalRechazadas);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar solicitudes: " + e.getMessage());
        }

        return "po/gestionSolicitudes";
    }

    /**
     * POST: Aprobar una solicitud (para administradores)
     */
    @PostMapping("/aprobarSolicitud/{id}")
    public String aprobarSolicitud(
            @PathVariable Integer id,
            @RequestParam(required = false) String comentarios,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario admin = usuarioRepository.findByCorreo(auth.getName());
            SolAccesoOrg solicitud = solAccesoOrgService.aprobarSolicitud(id, admin.getDni(), comentarios);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Solicitud #" + id + " aprobada exitosamente. Se ha notificado al usuario.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al aprobar solicitud: " + e.getMessage());
        }

        return "redirect:/po/gestionSolicitudes";
    }

    /**
     * POST: Rechazar una solicitud (para administradores)
     */
    @PostMapping("/rechazarSolicitud/{id}")
    public String rechazarSolicitud(
            @PathVariable Integer id,
            @RequestParam(required = false) String comentarios,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario admin = usuarioRepository.findByCorreo(auth.getName());
            SolAccesoOrg solicitud = solAccesoOrgService.rechazarSolicitud(id, admin.getDni(), comentarios);

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

        System.out.println("=== VERIFICANDO USUARIO CON DNI: " + dni + " ===");

        // Buscar usuario por DNI
        Optional<Usuario> usuarioOpt = usuarioRepository.findOptionalByDni(dni);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ USUARIO ENCONTRADO: " + usuario.getNombre() + " " + usuario.getApellidoPaterno());

            response.put("existe", true);
            response.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellidoPaterno());
            response.put("correo", usuario.getCorreo());

            // Verificar si ya tiene organización aprobada
            boolean tieneOrganizacion = solAccesoOrgService.existeSolicitudAprobadaParaDni(dni);
            response.put("tieneOrganizacion", tieneOrganizacion);

            System.out.println("¿Tiene organización? " + tieneOrganizacion);

        } else {
            System.out.println("❌ USUARIO NO ENCONTRADO para DNI: " + dni);
            response.put("existe", false);
            response.put("mensaje", "Usuario no registrado en el sistema");
        }

        return response;
    }

}