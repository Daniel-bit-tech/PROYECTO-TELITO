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

    public ProyectosPoController(UsuarioRepository usuarioRepository, ApiRepository apiRepository, 
                                ProyectoHasApiRepository proyHasApiRepository, ProyectoRepository proyectoRepository, 
                                EntornoRepository entornoRepository, CredencialApiRepository credencialApiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiRepository = apiRepository;
        this.proyHasApiRepository = proyHasApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.entornoRepository = entornoRepository;
        this.credencialApiRepository = credencialApiRepository;
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
                listaProyectos = proyectoRepository.findByActivoAndEquipoOrganizacionId(true, organizacionId);
            } else if (filtro != null && filtro.equals("privados")) {
                listaProyectos = proyectoRepository.findByPublicoAndEquipoOrganizacionId(false, organizacionId);
            } else {
                listaProyectos = proyectoRepository.findByEquipoOrganizacionId(organizacionId);
            }
        }

        model.addAttribute("listaProyectos", listaProyectos);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentPortal", "po");

        return "po/proyectos";
    }

    @GetMapping("/{id}")
    public String mostrarDetalleProyecto(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        Optional<Proyecto> optProyecto = proyectoRepository.findById(id);

        addImpersonationAttributes(model, session);

        if (optProyecto.isPresent()) {
            Proyecto proyecto = optProyecto.get();
            List<ProyectoHasApi> listaApis = proyHasApiRepository.findByProyecto_IdProyecto(proyecto.getIdProyecto());
            List<Api> apiList = apiRepository.findAll();

            ProyectoHasApi nuevaAsociacion = new ProyectoHasApi();
            Optional<Entorno> optEntornoDefecto = entornoRepository.findById(2);

            if (optEntornoDefecto.isPresent()) {
                nuevaAsociacion.setEntorno(optEntornoDefecto.get());
            }

            model.addAttribute("proyecto", proyecto);
            model.addAttribute("listaApis", listaApis);
            model.addAttribute("apiList", apiList);
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentPortal", "po");

            model.addAttribute("apisDisponibles", apiRepository.findApisNotAssociatedWithProyecto(id));
            model.addAttribute("entornosDisponibles", entornoRepository.findAll());

            model.addAttribute("nuevaAsociacion", nuevaAsociacion);

            return "po/proyecto-detalle";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado");
        }
    }

    @PostMapping("/{idProy}/api/{idApi}/cambiar-entorno")
    @ResponseBody
    public ResponseEntity<?> cambiarEntornoApiProy(@PathVariable Integer idProy, @PathVariable Integer idApi, @RequestParam Integer idEntorno,
                                                   Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);

        ProyectoHasApiId idProyHasApi = new ProyectoHasApiId(idProy, idApi);
        ProyectoHasApi proyHasApi = proyHasApiRepository.findById(idProyHasApi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proyecto o la API."));

        if (!proyHasApi.getProyecto().getEquipo().equals(usuario.getEquipo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este proyecto.");
        }

        Integer currentEntornoId = proyHasApi.getEntorno().getIdEntorno();
        Integer newEntornoId = idEntorno;

        if (currentEntornoId.equals(newEntornoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La API ya está asociada al entorno " + proyHasApi.getEntorno().getNombre() + ".");
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

        List<CredencialApi> credencialesActivas = credencialApiRepository
                .findByApi_IdApiAndEstado(idApi, true);

        for (CredencialApi credencial : credencialesActivas) {
            credencial.setEstado(false);
            credencialApiRepository.save(credencial);
        }

        // 6. Guardar el Nuevo Entorno
        Entorno newEntorno = entornoRepository.findById(idEntorno)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entorno no encontrado"));

        proyHasApi.setEntorno(newEntorno);
        proyHasApiRepository.save(proyHasApi);

        return ResponseEntity.ok(Map.of("message", "API "+proyHasApi.getApi().getNombre()+" cambió a entorno "+newEntorno.getNombre()+" con éxito. Claves API obsoletas invalidadas."));
    }


    @PostMapping("/{idProy}/addApis")
    public String addApiToProject(@PathVariable("idProy") Integer idProyecto,
                                  @ModelAttribute("nuevaAsociacion") ProyectoHasApi nuevaAsociacion,
                                  RedirectAttributes redirectAttributes,
                                  Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);

        Integer entornoInicialId = nuevaAsociacion.getEntorno().getIdEntorno();

        if (!entornoInicialId.equals(2)) {
            redirectAttributes.addFlashAttribute("error",
                    "ERROR DE REGLA: La asociación inicial de una API debe comenzar obligatoriamente en el entorno 'Desarrollo'.");
            return "redirect:/po/proyectos/" + idProyecto;
        }

        try {
            Proyecto proyecto = proyectoRepository.findById(idProyecto)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            Api api = apiRepository.findById(nuevaAsociacion.getApi().getIdApi())
                    .orElseThrow(() -> new RuntimeException("API no encontrada"));

            ProyectoHasApiId idClave = new ProyectoHasApiId(proyecto.getIdProyecto(), api.getIdApi());

            nuevaAsociacion.setProyectoHasApiId(idClave);

            nuevaAsociacion.setProyecto(proyecto);
            nuevaAsociacion.setApi(api);


            proyHasApiRepository.save(nuevaAsociacion);

            redirectAttributes.addFlashAttribute("success", "API " + api.getNombre() + " asociada a Desarrollo con éxito.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar asociar la API: " + e.getMessage());
        }

        return "redirect:/po/proyectos/" + idProyecto;
    }

}