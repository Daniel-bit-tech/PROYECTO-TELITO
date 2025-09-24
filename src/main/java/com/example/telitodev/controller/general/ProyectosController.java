package com.example.telitodev.controller.general;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/proyectos")
public class ProyectosController {

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
                           Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        List<Proyecto> listaProyectos = null;
        if (filtro != null && filtro.equals("activos")) {
            listaProyectos = proyectoRepository.findByActivoAndOrganizacion_Usuarios_Dni(true, usuario.getDni());
        } else if (filtro != null && filtro.equals("ocultos")) {
            listaProyectos = proyectoRepository.findByPublicoAndOrganizacion_Usuarios_Dni(false, usuario.getDni());
        } else {
            listaProyectos = proyectoRepository.findByOrganizacion_Usuarios_Dni(usuario.getDni());
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("filtro", filtro);

        model.addAttribute("usuario", usuario);

        return "desarrollador/proyectos";
    }


    @GetMapping("/{id}")
    public String mostrarDetalleProyecto(@PathVariable Integer id, Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (proyecto.getPublico() ||
                usuario.getRol().getNombreRol().equals("SUPERADMIN") ||
                (usuario.getRol().getNombreRol().equals("PO") && proyecto.getOrganizacion().equals(usuario.getOrganizacion()))) {
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
    public String mostrarFormEditar(@ModelAttribute("proyecto") Proyecto proyecto, Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        proyecto.setUsuarioLider(usuario);
        proyecto.setOrganizacion(usuario.getOrganizacion());
        proyecto.setFechaInicio(new Date(System.currentTimeMillis()));
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("usuario", usuario);
//        return "proyectos/formEditar";
        return "po/formEditarProy";

    }

    @GetMapping("{id}/config")
    @PreAuthorize("hasRole('PO')")
    public String mostrarFormCrear(@PathVariable Integer id, @ModelAttribute("proyecto") Proyecto proyecto,
                                   Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
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
    public String guardarProyecto(@Valid @ModelAttribute("proyecto") Proyecto proyecto,
                                     Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        if (usuario.getOrganizacion().equals(proyecto.getOrganizacion()) && proyecto.getUsuarioLider().equals(usuario)) {
            if (proyecto.getIdProyecto() != null) {
                // Edición
                Proyecto existente = proyectoRepository.findById(proyecto.getIdProyecto())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

                proyecto.setNombre(existente.getNombre());
                proyecto.setFechaInicio(existente.getFechaInicio());
                proyecto.setOrganizacion(existente.getOrganizacion());
                proyecto.setUsuarioLider(existente.getUsuarioLider());
            } else {
                // Creación
                proyecto.setOrganizacion(usuario.getOrganizacion());
                proyecto.setUsuarioLider(usuario);
            }
            proyectoRepository.save(proyecto);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proyecto");
        }

        model.addAttribute("usuario", usuario);
        return "redirect:/proyectos/" + proyecto.getIdProyecto();
    }

    @PostMapping("{id}/addApis")
    @PreAuthorize("hasRole('PO')")
    public String agregarApisProy(@Valid @ModelAttribute("nuevaAsociacion") ProyectoHasApi nuevaAsociacion,
                                  Model model, Authentication auth, @PathVariable Integer id) {

//        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
//
//        if (usuario.getOrganizacion().equals(proyecto.getOrganizacion()) && proyecto.getUsuarioLider().equals(usuario)) {
//
//                List<ProyectoHasApi> apisProyecto = proyHasApiRepository.findByProyecto_IdProyecto(proyecto.getIdProyecto());
//
//                for (ProyectoHasApi newApi : newApis) {
//                    for (ProyectoHasApi apiProy : apisProyecto) {
//                        if (!apiProy.equals(newApi)) {
//                            ProyectoHasApi newApiProy = new ProyectoHasApi(proyecto,newApi.getApi(),new Date(System.currentTimeMillis()),newApi.getApi().getNombre(),new VersionApi(), new Entorno());
//                            proyHasApiRepository.save(newApiProy);
//                        }
//                    }
//                }
//
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proyecto");
//        }
//
//        model.addAttribute("usuario", usuario);
//        return "redirect:/proyectos/" + id;


        // 1. Verificar que el proyecto existe
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));

        // 2. Verificar permisos (solo PO de la misma organización)
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        if (!usuario.getOrganizacion().equals(proyecto.getOrganizacion())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este proyecto");
        }

        // 3. Validar errores de binding
//        if (result.hasErrors()) {
//            // Si hay errores, redirigir de nuevo al detalle con mensaje
//            return "redirect:/proyectos/{id}/detalle?error=validacion";
//        }

        // 4. Completar la asociación
        nuevaAsociacion.setProyecto(proyecto); // asignar el proyecto
        if (nuevaAsociacion.getFechaAsociacion() == null) {
            nuevaAsociacion.setFechaAsociacion(new Date(System.currentTimeMillis())); // fecha por defecto si no se envía
        }

        // 5. Guardar
        proyHasApiRepository.save(nuevaAsociacion);

        // 6. Redirigir al detalle del proyecto
        return "redirect:/proyectos/{id}?success=apiAgregada";
    }

}
