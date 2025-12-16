    package com.example.telitodev.controller.productowner;
    
    import com.example.telitodev.controller.BaseController;
    import com.example.telitodev.entity.*;
    import com.example.telitodev.repository.*;
    import com.example.telitodev.service.*;
    import jakarta.servlet.http.HttpSession;
    import org.springframework.http.HttpStatus;
    import org.springframework.security.core.Authentication;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.server.ResponseStatusException;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.sql.Date;
    import java.time.LocalDate;
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
        private final ApiRepository apiRepository;;
        private final EquipoRepository equipoRepository;

        // AGREGAR los nuevos servicios al constructor
        public OrganizacionController(UsuarioRepository usuarioRepository,
                                      OrganizacionRepository organizacionRepository,
                                      ProyectoRepository proyectoRepository,
                                      SolAccesoOrgService solAccesoOrgService,
                                      OrganizacionService organizacionService,
                                      ApiRepository apiRepository,EquipoRepository equipoRepository) {

            this.usuarioRepository = usuarioRepository;
            this.organizacionRepository = organizacionRepository;
            this.proyectoRepository = proyectoRepository;
            this.solAccesoOrgService = solAccesoOrgService;
            this.organizacionService = organizacionService;
            this.apiRepository = apiRepository;
            this.equipoRepository = equipoRepository;
        }

        @GetMapping("/organizacion")
        public String showOrganizacion(Model model, Authentication auth, HttpSession session) {
            try {
                // 1. Usuario logueado (soporta impersonación por BaseController)
                Usuario usuario = getCurrentUser(auth, session);
                model.addAttribute("usuario", usuario);
                addImpersonationAttributes(model, session);

                // 2. Equipo del usuario (nuevo foco de la vista)
                Equipo equipo = usuario.getEquipo();
                model.addAttribute("equipo", equipo);

                // 3. Organización (derivada del equipo o, en su defecto, del usuario)
                Organizacion organizacion = null;
                if (equipo != null) {
                    organizacion = equipo.getOrganizacion();
                } else {
                    // fallback por si el usuario tiene org pero aún no equipo
                    organizacion = usuario.getOrganizacion();
                }
                model.addAttribute("organizacion", organizacion);

                // 4. Miembros del equipo
                List<Usuario> miembros = Collections.emptyList();
                if (equipo != null && equipo.getUsuarios() != null) {
                    miembros = equipo.getUsuarios();
                }
                model.addAttribute("miembros", miembros);

                // 5. APIs del equipo
                List<Api> apis = Collections.emptyList();
                if (equipo != null && equipo.getApis() != null) {
                    apis = equipo.getApis();
                }
                model.addAttribute("apis", apis);

                // 6. Proyectos activos del equipo
                List<Proyecto> proyectosActivos = Collections.emptyList();
                if (equipo != null && equipo.getProyectos() != null) {
                    proyectosActivos = equipo.getProyectos()
                            .stream()
                            .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                            .collect(Collectors.toList());
                }
                model.addAttribute("proyectosActivos", proyectosActivos);

                // 7. Renderizar la misma vista (que ahora ya cambiamos a “MI Equipo”)
                return "po/organizacion";

            } catch (Exception e) {
                // En caso de error, mostrar la página pero al menos con el usuario cargado
                Usuario usuario = getCurrentUser(auth, session);
                model.addAttribute("usuario", usuario);
                addImpersonationAttributes(model, session);
                return "po/organizacion";
            }
        }

        @GetMapping("/solicitudAcceso")
        public String mostrarSolicitudAcceso(Model model,
                                             Authentication auth,
                                             HttpSession session) {
            try {
                // 1. Usuario logueado (soporta impersonación)
                Usuario usuario = getCurrentUser(auth, session);
                model.addAttribute("usuario", usuario);
                addImpersonationAttributes(model, session);

                // 2. Equipo del usuario
                Equipo equipo = usuario.getEquipo();
                model.addAttribute("equipo", equipo);

                // 3. Organización (del equipo o, de fallback, del usuario)
                Organizacion organizacion = null;
                if (equipo != null) {
                    organizacion = equipo.getOrganizacion();
                } else {
                    organizacion = usuario.getOrganizacion();
                }
                model.addAttribute("organizacion", organizacion);

                // 4. Miembros del equipo
                List<Usuario> miembros = Collections.emptyList();
                if (equipo != null && equipo.getUsuarios() != null) {
                    miembros = equipo.getUsuarios();
                }
                model.addAttribute("miembros", miembros);

                // 4.1 Estadísticas para las tarjetas
                long totalMiembros = miembros.size();
                long totalDevs = miembros.stream()
                        .filter(u -> u.getRol() != null &&
                                "DEV".equalsIgnoreCase(u.getRol().getNombreRol()))
                        .count();
                long totalQas = miembros.stream()
                        .filter(u -> u.getRol() != null &&
                                "QA".equalsIgnoreCase(u.getRol().getNombreRol()))
                        .count();

                model.addAttribute("totalMiembros", totalMiembros);
                model.addAttribute("totalDevs", totalDevs);
                model.addAttribute("totalQas", totalQas);

                // 5. Usuarios disponibles (misma organización, sin equipo, rol DEV o QA)
                List<Usuario> usuariosDisponibles = Collections.emptyList();
                if (organizacion != null) {
                    List<String> rolesPermitidos = Arrays.asList("DEV", "QA");
                    usuariosDisponibles =
                            usuarioRepository.findByOrganizacion_IdOrganizacionAndEquipoIsNullAndRol_NombreRolIn(
                                    organizacion.getIdOrganizacion(),
                                    rolesPermitidos
                            );
                }
                model.addAttribute("usuariosDisponibles", usuariosDisponibles);

                // 6. Renderizar la vista correcta
                return "po/solicitudAcceso";

            } catch (Exception e) {
                // En caso de error, al menos cargamos el usuario y mostramos el mensaje de "sin equipo"
                Usuario usuario = getCurrentUser(auth, session);
                model.addAttribute("usuario", usuario);
                addImpersonationAttributes(model, session);
                model.addAttribute("equipo", null);
                model.addAttribute("miembros", Collections.emptyList());
                model.addAttribute("usuariosDisponibles", Collections.emptyList());
                return "po/solicitudAcceso";
            }
        }



        @PostMapping("/equipo/agregarMiembro")
        public String agregarMiembroAlEquipo(@RequestParam("dniUsuario") String dniUsuario,
                                             Authentication auth,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {

            // 1. Usuario actual (PO)
            Usuario po = getCurrentUser(auth, session);
            Equipo equipo = po.getEquipo();

            if (equipo == null) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes un equipo asignado. Contacta al administrador.");
                // Puedes enviarlo a Mi Equipo o donde prefieras
                return "redirect:/po/organizacion";
            }

            // 2. Buscar usuario a agregar
            Optional<Usuario> optUsuario = usuarioRepository.findOptionalByDni(dniUsuario);
            if (optUsuario.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "No se encontró el usuario seleccionado.");
                // 👇 Volvemos a la vista de solicitud de acceso
                return "redirect:/po/solicitudAcceso";
            }

            Usuario usuarioAgregar = optUsuario.get();

            // 3. Validaciones de regla de negocio

            // 3.1 Ya tiene equipo
            if (usuarioAgregar.getEquipo() != null) {
                redirectAttributes.addFlashAttribute("error",
                        "El usuario seleccionado ya pertenece a un equipo.");
                return "redirect:/po/solicitudAcceso";
            }

            // 3.2 Debe pertenecer a la misma organización
            if (usuarioAgregar.getOrganizacion() == null ||
                    !usuarioAgregar.getOrganizacion().getIdOrganizacion()
                            .equals(equipo.getOrganizacion().getIdOrganizacion())) {

                redirectAttributes.addFlashAttribute("error",
                        "El usuario no pertenece a tu organización y no puede ser agregado.");
                return "redirect:/po/solicitudAcceso";
            }

            // 3.3 Solo se permite DEV o QA
            String nombreRol = (usuarioAgregar.getRol() != null)
                    ? usuarioAgregar.getRol().getNombreRol()
                    : null;

            if (nombreRol == null ||
                    (!"DEV".equalsIgnoreCase(nombreRol) && !"QA".equalsIgnoreCase(nombreRol))) {

                redirectAttributes.addFlashAttribute("error",
                        "Solo se pueden agregar usuarios con rol DEV o QA.");
                return "redirect:/po/solicitudAcceso";
            }

            // 4. Asignar al equipo y guardar
            usuarioAgregar.setEquipo(equipo);
            usuarioRepository.save(usuarioAgregar);

            redirectAttributes.addFlashAttribute("success",
                    "Miembro agregado correctamente al equipo.");

            // 👉 Después de agregar, puedes:
            //  a) Volver a Mi Equipo:
            // return "redirect:/po/organizacion";

            //  b) O seguir en la pantalla de solicitud de acceso:
            return "redirect:/po/solicitudAcceso";
        }

        // Dentro de OrganizacionController

        @GetMapping("/equipo/nuevo")
        public String mostrarFormularioCrearEquipo(Model model,
                                                   Authentication auth,
                                                   HttpSession session,
                                                   RedirectAttributes ra) {

            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            addImpersonationAttributes(model, session);

            Organizacion organizacion = usuario.getOrganizacion();
            model.addAttribute("organizacion", organizacion);

            // Reglas de negocio
            if (organizacion == null) {
                ra.addFlashAttribute("error", "No perteneces a ninguna organización.");
                return "redirect:/po/organizacion";
            }
            if (usuario.getEquipo() != null) {
                ra.addFlashAttribute("info", "Ya estás asignado a un equipo.");
                return "redirect:/po/organizacion";
            }

            model.addAttribute("equipo", new Equipo()); // para th:object
            return "po/equipo_nuevo";
        }

        @PostMapping("/equipo/crear")
        public String crearEquipo(@RequestParam("nombre") String nombre,
                                  @RequestParam(value = "descripcion", required = false) String descripcion,
                                  Authentication auth,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

            Usuario usuario = getCurrentUser(auth, session);

            // PO debe tener organización
            Organizacion organizacion = usuario.getOrganizacion();
            if (organizacion == null) {
                redirectAttributes.addFlashAttribute("error", "No tienes organización asignada.");
                return "redirect:/po/organizacion";
            }

            // Si ya tiene equipo, no permitir crear otro
            if (usuario.getEquipo() != null) {
                redirectAttributes.addFlashAttribute("error", "Ya tienes un equipo asignado.");
                return "redirect:/po/organizacion";
            }

            String nombreLimpio = (nombre != null) ? nombre.trim() : "";

            if (nombreLimpio.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorNombre", "El nombre del equipo es obligatorio.");
                redirectAttributes.addFlashAttribute("nombre", nombre);
                redirectAttributes.addFlashAttribute("descripcion", descripcion);
                return "redirect:/po/equipo/nuevo";
            }

            // ✅ VALIDACIÓN: nombre repetido en la misma organización
            boolean existe = equipoRepository.existsByNombreIgnoreCaseAndOrganizacion_IdOrganizacion(
                    nombreLimpio, organizacion.getIdOrganizacion()
            );

            if (existe) {
                redirectAttributes.addFlashAttribute("errorNombre", "Ya existe un equipo con ese nombre en tu organización.");
                // para repoblar el form
                redirectAttributes.addFlashAttribute("nombre", nombreLimpio);
                redirectAttributes.addFlashAttribute("descripcion", descripcion);
                return "redirect:/po/equipo/nuevo";
            }

            // Crear y guardar
            Equipo equipo = new Equipo();
            equipo.setNombre(nombreLimpio);
            equipo.setOrganizacion(organizacion);
            equipo.setFechaCreacion(Date.valueOf((LocalDate.now())));

            // OJO: tu entity Equipo NO tiene "descripcion".
            // Si en BD existe columna descripcion, agrégala al entity.
            // Si no existe, ignora esta variable.

            equipoRepository.save(equipo);

            // Asignar equipo al PO
            usuario.setEquipo(equipo);
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("success", "Equipo creado correctamente ✅");
            return "redirect:/po/organizacion";
        }




        /*
        @PostMapping("/{idEquipo}/miembros/agregar")
        public String agregarMiembro(@PathVariable Integer idEquipo,
                                     @RequestParam("dniUsuario") String dniUsuario,
                                     RedirectAttributes redirectAttrs) {

            Equipo equipo = equipoRepository.findById(idEquipo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            Usuario usuario = usuarioRepository.findById(dniUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            // ------- Validaciones del Caso B -------
            if (usuario.getEquipo() != null) {
                redirectAttrs.addFlashAttribute("error",
                        "El usuario ya pertenece a un equipo.");
                return "redirect:/equipos/" + idEquipo + "/detalle";
            }

            if (usuario.getOrganizacion() == null ||
                    !usuario.getOrganizacion().getIdOrganizacion()
                            .equals(equipo.getOrganizacion().getIdOrganizacion())) {
                redirectAttrs.addFlashAttribute("error",
                        "El usuario no pertenece a la misma organización del equipo.");
                return "redirect:/equipos/" + idEquipo + "/detalle";
            }

            // Solo DEV o QA
            usuario.setEquipo(equipo);
            usuarioRepository.save(usuario);      // UPDATE usu ario SET idEquipo = ?

            redirectAttrs.addFlashAttribute("success",
                    "Miembro agregado al equipo correctamente.");
            return "redirect:/equipos/" + idEquipo + "/detalle";
        }
            */
    // MÉTODO ORIGINAL - CON SOPORTE DE IMPERSONACIÓN

    
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

    
        /**
         * GET: Mostrar historial de solicitudes del usuario
         */
            @GetMapping("/misSolicitudes")
            public String mostrarMisSolicitudes(Model model, Authentication auth) {
                try {
                    Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
                    model.addAttribute("usuario", usuario);

                    // Obtener solicitudes del usuario
                    List<SolAccesoEquipo> solicitudes = solAccesoOrgService.obtenerSolicitudesPorUsuario(usuario.getDni());
                    model.addAttribute("solicitudes", solicitudes);

                    // Estadísticas
                    long totalSolicitudes = solicitudes.size();
                    long pendientes = solicitudes.stream()
                            .filter(sol -> sol.getEstado() == SolAccesoEquipo.EstadoSolicitud.PENDIENTE)
                            .count();
                    long aprobadas = solicitudes.stream()
                            .filter(sol -> sol.getEstado() == SolAccesoEquipo.EstadoSolicitud.APROBADA)
                            .count();
                    long rechazadas = solicitudes.stream()
                            .filter(sol -> sol.getEstado() == SolAccesoEquipo.EstadoSolicitud.RECHAZADA)
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
                List<SolAccesoEquipo> solicitudesPendientes = solAccesoOrgService.obtenerSolicitudesPendientes();
                model.addAttribute("solicitudesPendientes", solicitudesPendientes);
    
                // Obtener organizaciones para filtros
                List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
                model.addAttribute("organizaciones", organizaciones);
    
                // Estadísticas
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
                SolAccesoEquipo solicitud = solAccesoOrgService.aprobarSolicitud(id, admin.getDni(), comentarios);
    
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