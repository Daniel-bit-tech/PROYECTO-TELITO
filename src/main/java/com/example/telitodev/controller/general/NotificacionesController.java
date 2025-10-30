package com.example.telitodev.controller.general;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.NotificacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
@PreAuthorize("isAuthenticated()") // Accesible para cualquier usuario logueado
public class NotificacionesController extends BaseController {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public NotificacionesController(UsuarioRepository usuarioRepository, NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Muestra la página dedicada con TODAS las notificaciones (leídas y no leídas)
     * de forma paginada.
     * URL ahora es genérica: /notificaciones
     */
    @GetMapping("/notificaciones")
    public String showAllNotificaciones(Model model, Authentication auth, HttpSession session,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "15") int size) {

        Usuario usuario = getCurrentUser(auth, session);
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notificacion> notificacionPage = notificacionRepository.findByUsuarioDniOrderByFechaDesc(usuario.getDni(), pageable);

        model.addAttribute("notificacionPage", notificacionPage);

        // --- INICIO: Lógica de Vista por Rol ---

        String userRole = usuario.getRol().getNombreRol();
        String viewName;

        // Decidimos qué plantilla HTML mostrar basado en el rol
        switch (userRole) {
            case "QA":
                viewName = "qa/notis_qa"; // Plantilla para QA
                break;
            case "PO":
                viewName = "po/notis_po"; // Plantilla para PO
                break;
            // case "DEV":
            //     viewName = "dev/notis_dev"; // Ejemplo para futura expansión
            //     break;
            default:
                // Una vista genérica por si el rol no tiene una específica
                viewName = "general/notificaciones";
        }

        return viewName;
    }


    @GetMapping("/notificaciones/marcarLeido/{idNotificacion}")
    public String marcarNotificacionLeida(@PathVariable("idNotificacion") Integer idNotificacion,
                                          Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(idNotificacion);

        // Verificación de seguridad
        if (notificacionOpt.isPresent() && notificacionOpt.get().getUsuario().getDni().equals(usuario.getDni())) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setLeido(true);
            notificacionRepository.save(notificacion);
            redirectAttributes.addFlashAttribute("success", "Notificación marcada como leída.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la notificación.");
        }

        return "redirect:/notificaciones"; // Redirige a la URL genérica
    }
}


/*
@GetMapping("/notificaciones")
    public String showAllNotificaciones(Model model, Authentication auth, HttpSession session,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "15") int size) {

        Usuario usuario = getCurrentUser(auth, session);
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notificacion> notificacionPage = notificacionRepository.findByUsuarioDniOrderByFechaDesc(usuario.getDni(), pageable);

        model.addAttribute("notificacionPage", notificacionPage);

        System.out.println("Imprimiendo rol que tiene este usuario: " + usuario.getRol().getNombreRol())
        if(usuario.getRol().getNombreRol().equals("PO")){
            return "po/notis_po";
        }
        else {
            return "qa/notis_qa"; // Retorna la nueva plantilla HTML
        }
    }

    @GetMapping("/notificaciones/marcarLeido/{idNotificacion}")
    public String marcarNotificacionLeida(@PathVariable("idNotificacion") Integer idNotificacion,
                                          Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(idNotificacion);

        // Verificación de seguridad: la notificación existe Y pertenece al usuario logueado
        if (notificacionOpt.isPresent() && notificacionOpt.get().getUsuario().getDni().equals(usuario.getDni())) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setLeido(true);
            notificacionRepository.save(notificacion);
            redirectAttributes.addFlashAttribute("success", "Notificación marcada como leída.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la notificación.");
        }

        return "redirect:/qa/notificaciones";
    }




 */
