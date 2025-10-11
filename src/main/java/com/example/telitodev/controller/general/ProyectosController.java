package com.example.telitodev.controller.general;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
@PreAuthorize("hasAnyRole('DEV','QA','SUPERADMIN','DEVELOPER')")
@RequestMapping("/proyectos")
public class ProyectosController extends BaseController {

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
                           Model model, Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

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
            if (filtro != null && filtro.equals("activos")) {
                listaProyectos = proyectoRepository.findByActivoAndOrganizacion_Usuarios_Dni(true, usuario.getDni());
            } else if (filtro != null && filtro.equals("ocultos")) {
                listaProyectos = proyectoRepository.findByPublicoAndOrganizacion_Usuarios_Dni(false, usuario.getDni());
            } else {
                listaProyectos = proyectoRepository.findByOrganizacion_Usuarios_Dni(usuario.getDni());
            }
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("filtro", filtro);

        model.addAttribute("usuario", usuario);

        return "general/proyectos";
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
//                (usuario.getRol().getNombreRol().equals("PO") && proyecto.getOrganizacion().equals(usuario.getOrganizacion()))) {
                (proyecto.getOrganizacion().equals(usuario.getOrganizacion()))) {
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
    public String mostrarFormEditar(@ModelAttribute("proyecto") Proyecto proyecto, Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        proyecto.setUsuarioLider(usuario);
        proyecto.setOrganizacion(usuario.getOrganizacion());
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
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        Optional<Proyecto> proyectoOptional = proyectoRepository.findById(id);
        if (proyectoOptional.isPresent() &&
                proyectoOptional.get().getOrganizacion().equals(usuario.getOrganizacion()) &&
                proyectoOptional.get().getUsuarioLider().equals(usuario)) {
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

        if (result.hasErrors()) {
            return "po/formEditarProy";
        }

        if (proyecto.getIdProyecto() != null) {
            //Edición
            if (usuario.getOrganizacion().equals(proyecto.getOrganizacion()) && proyecto.getUsuarioLider().equals(usuario)) {

                Proyecto existente = proyectoRepository.findById(proyecto.getIdProyecto())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

                proyecto.setNombre(existente.getNombre());
                proyecto.setFechaInicio(existente.getFechaInicio());
                proyecto.setOrganizacion(existente.getOrganizacion());
                proyecto.setUsuarioLider(existente.getUsuarioLider());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proyecto");
            }
        } else {

            System.out.println("Creando proy: "+proyecto.getNombre());
            // Creación
            proyecto.setOrganizacion(usuario.getOrganizacion());
            proyecto.setUsuarioLider(usuario);
        }

        proyectoRepository.save(proyecto);

        return "redirect:/proyectos/" + proyecto.getIdProyecto();
    }

    @PostMapping("{id}/addApis")
    @PreAuthorize("hasRole('PO')")
    public String agregarApisProy(@Valid @ModelAttribute("nuevaAsociacion") ProyectoHasApi nuevaAsociacion,
                                  Model model, Authentication auth, @PathVariable Integer id) {

        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        if (!usuario.getOrganizacion().equals(proyecto.getOrganizacion())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este proyecto");
        }


        nuevaAsociacion.setProyecto(proyecto); // asignar el proyecto
        if (nuevaAsociacion.getFechaAsociacion() == null) {
            nuevaAsociacion.setFechaAsociacion(new Date(System.currentTimeMillis())); // fecha por defecto si no se envía
        }

        proyHasApiRepository.save(nuevaAsociacion);

        return "redirect:/proyectos/{id}?success=apiAgregada";
    }

}
