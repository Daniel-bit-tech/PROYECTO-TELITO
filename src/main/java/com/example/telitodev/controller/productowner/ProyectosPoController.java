package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/po/proyectos")
public class ProyectosPoController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final ApiRepository apiRepository;
    final ProyectoHasApiRepository proyHasApiRepository;
    final ProyectoRepository proyectoRepository;
    final EntornoRepository entornoRepository;
    final CredencialApiRepository credencialApiRepository;
    final VersionApiRepository versionApiRepository;

    public ProyectosPoController(UsuarioRepository usuarioRepository, ApiRepository apiRepository, 
                                ProyectoHasApiRepository proyHasApiRepository, ProyectoRepository proyectoRepository, 
                                EntornoRepository entornoRepository, CredencialApiRepository credencialApiRepository
    , VersionApiRepository versionApiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
        this.proyHasApiRepository = proyHasApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.entornoRepository = entornoRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.versionApiRepository = versionApiRepository;
    }

    @GetMapping()
    public String mostrarListaProyectos(@RequestParam(value = "filter", required = false) String filtro,
                                       Model model, Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        List<Proyecto> listaProyectos = null;
        if (usuario.getRol().getNombreRol().equals("SUPERADMIN")) {
            if (filtro != null && filtro.equals("activos")) {
                listaProyectos = proyectoRepository.findByActivo(true);
            } else if (filtro != null && filtro.equals("privados")) {
                listaProyectos = proyectoRepository.findByPublico(false);
            } else {
                listaProyectos = proyectoRepository.findAll();
            }
        } else {
            // Para usuarios no-SuperAdmin, verificar que tengan organización asignada
            if (usuario.getOrganizacion() == null) {
                System.err.println("ERROR: Usuario " + usuario.getDni() + " no tiene organización asignada");
                model.addAttribute("error", "Usuario sin organización asignada. Contacte al administrador.");
                model.addAttribute("listaProyectos", List.of()); // Lista vacía para evitar errores en el template
                model.addAttribute("usuario", usuario);
                model.addAttribute("currentPortal", "po");
                return "po/proyectos";
            }
            
            // Mostrar proyectos de su organización
            Integer organizacionId = usuario.getOrganizacion().getIdOrganizacion();
            if (filtro != null && filtro.equals("activos")) {
                listaProyectos = proyectoRepository.findByActivoAndEquipo_Organizacion_IdOrganizacion(true, organizacionId);
            } else if (filtro != null && filtro.equals("privados")) {
                listaProyectos = proyectoRepository.findByPublicoAndEquipo_Organizacion_IdOrganizacion(false, organizacionId);
            } else {
                listaProyectos = proyectoRepository.findByEquipo_Organizacion_IdOrganizacion(organizacionId);
            }
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentPortal", "po");

        return "po/proyectos";
    }

    @GetMapping("/{id}")
    public String mostrarDetalleProyecto(@PathVariable Integer id, Model model,
                                         Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        addImpersonationAttributes(model, session);

        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        // ===== Validación: solo puede VER proyectos de su organización (excepto SUPERADMIN) =====
        if (!usuario.getRol().getNombreRol().equals("SUPERADMIN")) {
            if (usuario.getOrganizacion() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            if (proyecto.getEquipo() == null || proyecto.getEquipo().getOrganizacion() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            Integer orgUsuario = usuario.getOrganizacion().getIdOrganizacion();
            Integer orgProyecto = proyecto.getEquipo().getOrganizacion().getIdOrganizacion();

            if (orgProyecto == null || !orgProyecto.equals(orgUsuario)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        // ================================================================================

        // ====== PO ENCARGADO: sale del EQUIPO del proyecto (NO del proyecto) ======
        Usuario poEncargado = null;
        if (proyecto.getEquipo() != null && proyecto.getEquipo().getIdEquipo() != null) {
            Integer idEquipo = proyecto.getEquipo().getIdEquipo();
            poEncargado = usuarioRepository.findPoEncargadoByEquipo(idEquipo).orElse(null);
        }
        model.addAttribute("poEncargado", poEncargado);

        // ========================================================================

        // ====== Permiso para configurar: solo si el PO logueado es el PO encargado del equipo ======
        boolean canConfigure = false;
        if (poEncargado != null && usuario.getDni() != null) {
            canConfigure = poEncargado.getDni().equals(usuario.getDni());
        }
        model.addAttribute("canConfigure", canConfigure);

        // =========================================================================================
        boolean canManageApis = canManageProyecto(usuario, proyecto);
        model.addAttribute("canManageApis", canManageApis);

        // APIs asociadas / disponibles
        List<ProyectoHasApi> listaApis = proyHasApiRepository.findByProyecto_IdProyecto(proyecto.getIdProyecto());
        model.addAttribute("listaApis", listaApis);

        Integer idEquipo = proyecto.getEquipo().getIdEquipo();

        List<Integer> idsAsociados = listaApis.stream()
                .map(x -> x.getApi().getIdApi())
                .toList();

        List<Api> apisDisponibles = idsAsociados.isEmpty()
                ? apiRepository.findByEquipo_IdEquipo(idEquipo)
                : apiRepository.findByEquipo_IdEquipoAndIdApiNotIn(idEquipo, idsAsociados);

        model.addAttribute("apisDisponibles", apisDisponibles);

        model.addAttribute("entornosDisponibles", entornoRepository.findAll());
        model.addAttribute("proyecto", proyecto);

        System.out.println("Proyecto=" + id
                + " equipo=" + (proyecto.getEquipo() != null ? proyecto.getEquipo().getIdEquipo() : null)
                + " listaApis=" + listaApis.size()
                + " apisDisponibles=" + apisDisponibles.size());

        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);


        return "po/proyecto-detalle";
    }


    @GetMapping("/{id}/config")
    public String configurarProyecto(@PathVariable Integer id,
                                     Model model,
                                     Authentication auth,
                                     HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        addImpersonationAttributes(model, session);

        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        // ===== Regla: solo puede CONFIGURAR si es el PO encargado del equipo del proyecto (o SUPERADMIN) =====
        boolean esSuperAdmin = usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol());

        Usuario poEncargado = null;
        if (proyecto.getEquipo() != null && proyecto.getEquipo().getIdEquipo() != null) {
            poEncargado = usuarioRepository.findPoEncargadoByEquipo(proyecto.getEquipo().getIdEquipo())
                    .orElse(null);
        }

        boolean canConfigure = esSuperAdmin || (poEncargado != null
                && usuario.getDni() != null
                && poEncargado.getDni() != null
                && poEncargado.getDni().equals(usuario.getDni()));

        if (!canConfigure) {
            // como ya lo vienes haciendo: ocultar existencia => 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // ================================================================================================

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("poEncargado", poEncargado);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentPortal", "po");

        return "po/proyecto-config";
    }



    @PostMapping("/{idProy}/api/{idApi}/cambiar-entorno")
    @ResponseBody
    public ResponseEntity<?> cambiarEntornoApiProy(@PathVariable Integer idProy,
                                                   @PathVariable Integer idApi,
                                                   @RequestParam Integer idEntorno,
                                                   Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);

        ProyectoHasApiId idProyHasApi = new ProyectoHasApiId(idProy, idApi);
        ProyectoHasApi proyHasApi = proyHasApiRepository.findById(idProyHasApi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el proyecto o la API."));

        // ✅ BLOQUEO: solo PO encargado del equipo del proyecto (o SUPERADMIN)
        Proyecto proyecto = proyHasApi.getProyecto();
        if (!canManageProyecto(usuario, proyecto)) {
            // para "ocultar" el recurso (misma lógica que usaste antes)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            // o si prefieres explícito:
            // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cambiar el entorno.");
        }

        Integer currentEntornoId = proyHasApi.getEntorno().getIdEntorno();
        Integer newEntornoId = idEntorno;

        if (currentEntornoId.equals(newEntornoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La API ya está asociada al entorno " + proyHasApi.getEntorno().getNombre() + ".");
        }

        boolean isTransitionValid = false;
        String errorMessage = "Transición de entorno no válida.";

        if (currentEntornoId.equals(2)) {
            if (newEntornoId.equals(3)) {
                isTransitionValid = true;
            } else if (newEntornoId.equals(1)) {
                errorMessage = "No se puede saltar de 'Desarrollo' a 'Producción'. El siguiente paso debe ser 'QA'.";
            } else {
                errorMessage = "El entorno 'Desarrollo' solo puede avanzar a 'QA'.";
            }
        } else if (currentEntornoId.equals(3)) {
            if (newEntornoId.equals(1)) {
                isTransitionValid = true;
            } else if (newEntornoId.equals(2)) {
                errorMessage = "No se permite retroceder de 'QA' a 'Desarrollo'.";
            } else {
                errorMessage = "El entorno 'QA' solo puede avanzar a 'Producción'.";
            }
        } else if (currentEntornoId.equals(1)) {
            errorMessage = "El entorno 'Producción' es el estado final y no se permite cambiarlo.";
        }

        if (!isTransitionValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }

        // invalidar credenciales activas
        List<CredencialApi> credencialesActivas =
                credencialApiRepository.findByApi_IdApiAndEstado(idApi, true);

        for (CredencialApi credencial : credencialesActivas) {
            credencial.setEstado(false);
            credencialApiRepository.save(credencial);
        }

        // Guardar nuevo entorno
        Entorno newEntorno = entornoRepository.findById(idEntorno)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entorno no encontrado"));

        proyHasApi.setEntorno(newEntorno);
        proyHasApiRepository.save(proyHasApi);

        return ResponseEntity.ok(Map.of(
                "message", "API " + proyHasApi.getApi().getNombre()
                        + " cambió a entorno " + newEntorno.getNombre()
                        + " con éxito. Claves API obsoletas invalidadas."
        ));
    }

    @PostMapping("/{idProy}/addApis")
    public String addApisToProject(@PathVariable("idProy") Integer idProyecto,
                                   @RequestParam("apiId") Integer apiId,
                                   @RequestParam("proposito") String proposito,
                                   RedirectAttributes redirectAttributes,
                                   Authentication auth,
                                   HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);

        // 1) Cargar proyecto
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        // 2) Permisos (PO encargado del equipo o SUPERADMIN)
        if (!canManageProyecto(usuario, proyecto)) {
            redirectAttributes.addFlashAttribute("error",
                    "🔒 Solo lectura: no tienes permisos para agregar APIs en este proyecto.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        // 3) Validaciones
        if (apiId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar una API.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        if (proposito == null || proposito.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El propósito es obligatorio.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        // 4) Evitar duplicado en el MISMO proyecto
        if (proyHasApiRepository.existsByProyecto_IdProyectoAndApi_IdApi(idProyecto, apiId)) {
            redirectAttributes.addFlashAttribute("error", "Esa API ya está asociada a este proyecto.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        // 5) Cargar API
        Api api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API no encontrada"));

        // (Opcional pero recomendable) Asegurar que la API pertenezca al equipo del proyecto
        Integer idEquipoProyecto = proyecto.getEquipo().getIdEquipo();
        if (api.getEquipo() == null || api.getEquipo().getIdEquipo() == null
                || !api.getEquipo().getIdEquipo().equals(idEquipoProyecto)) {
            redirectAttributes.addFlashAttribute("error", "No puedes asociar una API de otro equipo.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        // 6) Entorno inicial fijo: Desarrollo (id=2)
        Entorno entornoDesarrollo = entornoRepository.findById(2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entorno Desarrollo no encontrado"));

        // 7) Version obligatoria (porque idVersion NOT NULL)
        VersionApi version = versionApiRepository.findTopByApi_IdApiOrderByIdVersionDesc(apiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La API seleccionada no tiene versiones. Crea una versión primero."));

        // 8) Crear asociación
        ProyectoHasApi pha = new ProyectoHasApi();
        pha.setProyectoHasApiId(new ProyectoHasApiId(idProyecto, apiId));
        pha.setProyecto(proyecto);
        pha.setApi(api);
        pha.setEntorno(entornoDesarrollo);
        pha.setVersion(version);
        pha.setProposito(proposito.trim());
        pha.setFechaAsociacion(new java.sql.Date(System.currentTimeMillis()));


        // ajusta si tu tipo es otro

        proyHasApiRepository.save(pha);

        redirectAttributes.addFlashAttribute("success",
                "API " + api.getNombre() + " agregada correctamente.");
        return "redirect:/po/proyectos/" + idProyecto;
    }



    @PostMapping("/{id}/config")
    public String guardarConfigProyecto(@PathVariable Integer id,
                                        @RequestParam String nombre,
                                        @RequestParam(required = false) String descripcion,
                                        @RequestParam(defaultValue = "false") boolean publico,
                                        @RequestParam(defaultValue = "false") boolean activo,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fechaFin,
                                        RedirectAttributes redirectAttributes,
                                        Authentication auth,
                                        HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);

        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        boolean esSuperAdmin = usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol());

        Usuario poEncargado = null;
        if (proyecto.getEquipo() != null && proyecto.getEquipo().getIdEquipo() != null) {
            poEncargado = usuarioRepository.findPoEncargadoByEquipo(proyecto.getEquipo().getIdEquipo())
                    .orElse(null);
        }

        boolean canConfigure = esSuperAdmin || (poEncargado != null
                && usuario.getDni() != null
                && poEncargado.getDni() != null
                && poEncargado.getDni().equals(usuario.getDni()));

        if (!canConfigure) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Validación simple
        if (nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre del proyecto no puede estar vacío.");
            return "redirect:/po/proyectos/" + id + "/config";
        }

        // Regla sugerida: si inactivo y no manda fecha fin, set hoy
        if (!activo && fechaFin == null) {
            fechaFin = java.time.LocalDate.now();
        }
        // Si activo, limpiar fecha fin (si quieres permitir fecha fin estando activo, comenta esto)
        if (activo) {
            fechaFin = null;
        }

        proyecto.setNombre(nombre.trim());
        proyecto.setDescripcion(descripcion != null ? descripcion.trim() : "");
        proyecto.setPublico(publico);
        proyecto.setActivo(activo);
        proyecto.setFechaFin(fechaFin);

        proyectoRepository.save(proyecto);

        redirectAttributes.addFlashAttribute("success", "Proyecto actualizado correctamente.");
        return "redirect:/po/proyectos/" + id + "/config";
    }

    /** Helper: valida que el usuario pueda ver el proyecto (misma org) y si puede configurar */
    private boolean puedeConfigurar(Usuario usuario, Proyecto proyecto) {
        // SUPERADMIN siempre puede
        if (usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol())) {
            return true;
        }

        // Debe tener organización
        if (usuario.getOrganizacion() == null) return false;

        // Proyecto debe tener equipo->organización
        if (proyecto.getEquipo() == null || proyecto.getEquipo().getOrganizacion() == null) return false;

        // Debe ser misma organización
        Integer orgUsuario = usuario.getOrganizacion().getIdOrganizacion();
        Integer orgProyecto = proyecto.getEquipo().getOrganizacion().getIdOrganizacion();
        if (orgProyecto == null || !orgProyecto.equals(orgUsuario)) return false;

        // Debe ser PO encargado del equipo del proyecto
        if (proyecto.getEquipo().getIdEquipo() == null) return false;

        Usuario poEncargado = usuarioRepository.findPoEncargadoByEquipo(proyecto.getEquipo().getIdEquipo())
                .orElse(null);

        return (poEncargado != null
                && usuario.getDni() != null
                && poEncargado.getDni() != null
                && poEncargado.getDni().equals(usuario.getDni()));
    }

    private boolean canManageProyecto(Usuario usuario, Proyecto proyecto) {
        if (usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol())) return true;

        if (proyecto == null || proyecto.getEquipo() == null || proyecto.getEquipo().getIdEquipo() == null) return false;

        Usuario poEncargado = usuarioRepository
                .findPoEncargadoByEquipo(proyecto.getEquipo().getIdEquipo())
                .orElse(null);

        return poEncargado != null
                && usuario.getDni() != null
                && usuario.getDni().equals(poEncargado.getDni());
    }

}