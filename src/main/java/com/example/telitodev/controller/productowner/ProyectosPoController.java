package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/po")
public class ProyectosPoController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final ApiRepository apiRepository;
    final ProyectoHasApiRepository proyHasApiRepository;
    final ProyectoRepository proyectoRepository;
    final EntornoRepository entornoRepository;

    public ProyectosPoController(UsuarioRepository usuarioRepository, ApiRepository apiRepository, 
                                ProyectoHasApiRepository proyHasApiRepository, ProyectoRepository proyectoRepository, 
                                EntornoRepository entornoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
        this.proyHasApiRepository = proyHasApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.entornoRepository = entornoRepository;
    }

    @GetMapping("/proyectos")
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
                listaProyectos = proyectoRepository.findByActivoAndOrganizacion_IdOrganizacion(true, organizacionId);
            } else if (filtro != null && filtro.equals("privados")) {
                listaProyectos = proyectoRepository.findByPublicoAndOrganizacion_IdOrganizacion(false, organizacionId);
            } else {
                listaProyectos = proyectoRepository.findByOrganizacion_IdOrganizacion(organizacionId);
            }
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentPortal", "po");

        return "po/proyectos";
    }

    @GetMapping("/proyectos/{id}")
    public String mostrarDetalleProyecto(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {
        
        Usuario usuario = getCurrentUser(auth, session);
        Optional<Proyecto> optProyecto = proyectoRepository.findById(id);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        if (optProyecto.isPresent()) {
            Proyecto proyecto = optProyecto.get();
            List<ProyectoHasApi> listaApis = proyHasApiRepository.findByProyecto_IdProyecto(proyecto.getIdProyecto());
            List<Api> apiList = apiRepository.findAll();
            
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("listaApis", listaApis);
            model.addAttribute("apiList", apiList);
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentPortal", "po");
            
            // Agregar atributos necesarios para el formulario de asociación de APIs
            model.addAttribute("apisDisponibles", apiRepository.findApisNotAssociatedWithProyecto(id));
            model.addAttribute("entornosDisponibles", entornoRepository.findAll());
            model.addAttribute("nuevaAsociacion", new ProyectoHasApi());
            
            return "po/proyecto-detalle";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado");
        }
    }
}