package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.ActividadReciente;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ActividadRecienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/po/actividad")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class ActividadRecienteController {

    private final ActividadRecienteService actividadRecienteService;
    private final UsuarioRepository usuarioRepository;

    public ActividadRecienteController(ActividadRecienteService actividadRecienteService, UsuarioRepository usuarioRepository) {
        this.actividadRecienteService = actividadRecienteService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listarActividades(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        List<ActividadReciente> listaActividades = actividadRecienteService.obtenerTodasActividades();
        model.addAttribute("listaActividades", listaActividades);
        return "po/actividad-reciente";
    }

    // NUEVO: Endpoint para cargar todas las actividades via AJAX
    @GetMapping("/todas")
    public String getTodasActividades(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        // Obtener TODAS las actividades del usuario
        List<ActividadReciente> todasActividades =
                actividadRecienteService.obtenerTodasActividadesPorUsuario(usuario.getDni());
        model.addAttribute("todasActividades", todasActividades);

        return "po/actividad-reciente :: actividades-content";
    }

    /**
     * Método helper para obtener el usuario correcto durante impersonación
     */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        // Verificar si hay impersonación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");

        if (isImpersonating != null && isImpersonating) {
            // Durante impersonación, obtener usuario por DNI del usuario impersonado
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    System.out.println("🎭 ActividadReciente - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }

        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 ActividadReciente - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }
}
