package com.example.telitodev.controller.general;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/proyectos")
public class ProyectosController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProyectosController.class);

    final UsuarioRepository usuarioRepository;
    final ApiRepository apiRepository;
    final ProyectoHasApiRepository proyHasApiRepository;
    final ProyectoRepository proyectoRepository;
    final EntornoRepository entornoRepository;
    public ProyectosController(UsuarioRepository usuarioRepository, ApiRepository apiRepository, ProyectoHasApiRepository proyHasApiRepository, ProyectoRepository proyectoRepository, EntornoRepository entornoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
        this.proyHasApiRepository = proyHasApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.entornoRepository = entornoRepository;
    }

    @GetMapping()
    public String mostrarListaProyectos(@RequestParam(value = "filter", required = false) String filtro,
                           @RequestHeader(value = "referer", required = false) String referer,
                           Model model, Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        // Detectar el portal de origen basado en el referer o el rol del usuario
        String portalOrigen = detectarPortalOrigen(referer, usuario);
        model.addAttribute("portalOrigen", portalOrigen);

        List<Proyecto> listaProyectos = null;
        if (usuario.getRol().getNombreRol().equals("SUPERADMIN")) {
            if (filtro != null && filtro.equals("activos")) {
                listaProyectos = proyectoRepository.findByActivo(true);
            } else if (filtro != null && filtro.equals("ocultos")) {
                listaProyectos = proyectoRepository.findByPublico(false);
            } else {
                listaProyectos = proyectoRepository.findAll();
            }
        } else {

//            Integer organizacionId = usuario.getOrganizacion().getIdOrganizacion();
//            // Para usuarios no-SuperAdmin, verificar que tengan organización asignada
//            if (usuario.getOrganizacion() == null) {
//                System.err.println("ERROR: Usuario " + usuario.getDni() + " no tiene organización asignada");
//                model.addAttribute("error", "Usuario sin organización asignada. Contacte al administrador.");
//                model.addAttribute("listaProyectos", List.of()); // Lista vacía para evitar errores en el template
//                model.addAttribute("filtro", filtro);
//                model.addAttribute("usuario", usuario);
//                return "general/proyectos";
//            }

            // Mostrar proyectos de su organización
            Integer organizacionId = usuario.getOrganizacion().getIdOrganizacion();
            if (filtro != null && filtro.equals("activos")) {
//                listaProyectos = proyectoRepository.findByActivoAndEquipoOrganizacionId(true, organizacionId);
                listaProyectos = proyectoRepository.findByActivoAndEquipo_Organizacion_IdOrganizacion(true, organizacionId);
            } else if (filtro != null && filtro.equals("ocultos")) {
//                listaProyectos = proyectoRepository.findByPublicoAndEquipoOrganizacionId(false, organizacionId);
                listaProyectos = proyectoRepository.findByPublicoAndEquipo_Organizacion_IdOrganizacion(false, organizacionId);
            } else {
//                listaProyectos = proyectoRepository.findByEquipoOrganizacionId(organizacionId);
                listaProyectos = proyectoRepository.findByEquipo_Organizacion_IdOrganizacion(organizacionId);
            }
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("filtro", filtro);

        model.addAttribute("usuario", usuario);

        return "general/proyectos";
    }

    private String detectarPortalOrigen(String referer, Usuario usuario) {
        // Si hay referer, usamos eso para detectar el portal
        if (referer != null) {
            if (referer.contains("/dev/")) return "DEV";
            if (referer.contains("/qa/")) return "QA";
            if (referer.contains("/po/")) return "PO";
            if (referer.contains("/admin/")) return "ADMIN";
        }

        // Si no hay referer, usamos el rol del usuario (considerando impersonación)
        String rol = usuario.getRol().getNombreRol();
        switch (rol) {
            case "DEV": return "DEV";
            case "QA": return "QA";
            case "PO": return "PO";
            case "SUPERADMIN": return "ADMIN";
            default: return "PO"; // Por defecto
        }
    }


    @GetMapping("/{id}")
    public String mostrarDetalleProyecto(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (proyecto.getPublico() ||
                usuario.getRol().getNombreRol().equals("SUPERADMIN") ||
//                (usuario.getRol().getNombreRol().equals("PO") && proyecto.getEquipo().equals(usuario.getEquipo())))
                (usuario.getEquipo() != null && proyecto.getEquipo().equals(usuario.getEquipo()))) {
            model.addAttribute("proyecto", proyecto);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver los detalles de este proyecto");
        }
        model.addAttribute("listaApis", apiRepository.findAll());
        model.addAttribute("usuario", usuario);

        model.addAttribute("apisDisponibles", apiRepository.findApisNotAssociatedWithProyecto(id));
        model.addAttribute("entornosDisponibles", entornoRepository.findAll());
        model.addAttribute("nuevaAsociacion", new ProyectoHasApi());
        return "po/proyectoDetalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('PO')")
    public String mostrarFormEditar(@ModelAttribute("proyecto") Proyecto proyecto, Model model, Authentication auth, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        if (usuario.getEquipo()==null) {
            redirectAttributes.addFlashAttribute("msg", "No puedes crear proyectos hasta pertenecer a un equipo");
            return "redirect:/proyectos";
        }

        proyecto.setEquipo(usuario.getEquipo());
        proyecto.setFechaInicio(LocalDate.now());
        proyecto.setPublico(true);
        proyecto.setActivo(true);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("usuario", usuario);
//        return "proyectos/formEditar";
        return "po/formEditarProy";

    }

    @GetMapping("{id}/config")
    @PreAuthorize("hasRole('PO')")
    public String mostrarFormCrear(@PathVariable Integer id, @ModelAttribute("proyecto") Proyecto proyecto,
                                   Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Verificar que el usuario tenga una organización asignada
        if (usuario.getOrganizacion() == null) {
            logger.error("Usuario {} intenta configurar proyecto pero no tiene organización asignada", usuario.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes configurar proyectos porque no tienes una organización asignada. Contacta al administrador.");
        }

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        Optional<Proyecto> proyectoOptional = proyectoRepository.findById(id);
        if (proyectoOptional.isPresent() &&
                proyectoOptional.get().getEquipo().equals(usuario.getEquipo())) {
            model.addAttribute("proyecto", proyectoOptional.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tiene permiso para configurar este proyecto");
        }

        model.addAttribute("usuario", usuario);
//        return "proyectos/formEditar";
        return "po/formEditarProy";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('PO')")
    public String guardarProyecto(@Valid @ModelAttribute("proyecto") Proyecto proyecto, BindingResult result,
                                     Model model, Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        // Verificar que el usuario tenga una organización asignada
        if (usuario.getOrganizacion() == null) {
            logger.error("Usuario {} intenta crear/editar proyecto pero no tiene organización asignada", usuario.getCorreo());
            model.addAttribute("error", "No puedes crear o editar proyectos porque no tienes una organización asignada. Contacta al administrador.");
            model.addAttribute("proyecto", proyecto);
            return "po/formEditarProy";
        }

        if (result.hasErrors()) {
            return "po/formEditarProy";
        }

        if (usuario.getOrganizacion()==null) {
            redirectAttributes.addFlashAttribute("msg", "No puedes crear proyectos hasta pertenecer a una organización");
            return "redirect:/proyectos";
        }

        if (proyecto.getIdProyecto() != null) {
            //Edición

            Proyecto existente = proyectoRepository.findById(proyecto.getIdProyecto())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (usuario.getEquipo().equals(existente.getEquipo())) {
                proyecto.setNombre(existente.getNombre());
                proyecto.setFechaInicio(existente.getFechaInicio());
                proyecto.setEquipo(existente.getEquipo());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proyecto");
            }
        } else {

            System.out.println("Creando proy: "+proyecto.getNombre());
            // Creación
            proyecto.setEquipo(usuario.getEquipo());
        }

        proyectoRepository.save(proyecto);

        return "redirect:/proyectos/" + proyecto.getIdProyecto();
    }

    @PostMapping("{id}/addApis")
    @PreAuthorize("hasRole('PO')")
    public String agregarApisProy(@Valid @ModelAttribute("nuevaAsociacion") ProyectoHasApi nuevaAsociacion, RedirectAttributes redirectAttributes,
                                  Model model, Authentication auth, @PathVariable Integer id) {


        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Verificar que el usuario tenga un equipo asignado
        if (usuario.getEquipo() == null) {
            logger.error("Usuario {} intenta agregar APIs a proyecto pero no tiene equipo asignado", usuario.getCorreo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar proyectos porque no tienes un equipo asignado. Contacta al administrador.");
        }

        if (!usuario.getEquipo().equals(proyecto.getEquipo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este proyecto");
        }



        nuevaAsociacion.setProyecto(proyecto); // asignar el proyecto
        if (nuevaAsociacion.getFechaAsociacion() == null) {
            nuevaAsociacion.setFechaAsociacion(new Date(System.currentTimeMillis())); // fecha por defecto si no se envía
        }

        proyHasApiRepository.save(nuevaAsociacion);

        redirectAttributes.addFlashAttribute("msg", "Se agrego la nueva Api al Proyecto "+nuevaAsociacion.getProyecto().getNombre()+" exitosamente");

        return "redirect:/proyectos/{id}";
    }

}
