package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.ActividadReciente; // Importa esta clase
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.NotificacionService;
import com.example.telitodev.service.ActividadRecienteService; // Importa el servicio
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class PoController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;
    final NotificacionService notificacionService;
    final ActividadRecienteService actividadRecienteService;

    public PoController(UsuarioRepository usuarioRepository, ApiService apiService, NotificacionService notificacionService, ActividadRecienteService actividadRecienteService) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.notificacionService = notificacionService;
        this.actividadRecienteService = actividadRecienteService;
    }
    @GetMapping("/Dashboard")
    public String showDashboardView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        if (isImpersonating != null && isImpersonating) {
            model.addAttribute("isImpersonating", true);
            model.addAttribute("impersonatedUserDni", session.getAttribute("IMPERSONATED_USER_DNI"));
            model.addAttribute("originalAdminUsername", session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("🎭 PO - Modo impersonación detectado para DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
        } else {
            model.addAttribute("isImpersonating", false);
        }
        
        return "po/home";
    }

    @GetMapping("/verPerfil")
    public String showverPerfilView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        return "po/verPerfil";
    }

    @GetMapping("/home")
    public String showHomeView(Model model, Authentication auth, HttpSession session, HttpServletRequest request) {
        List<Api> recentApis = apiService.getRecentApis();
        model.addAttribute("recentApis", recentApis);

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesPorUsuario(usuario.getDni());
        model.addAttribute("notificaciones", notificaciones);

        List<ActividadReciente> actividadesRecientes = actividadRecienteService.obtenerActividadesRecientesPorUsuario(usuario.getDni());
        model.addAttribute("actividadesRecientes", actividadesRecientes);

        // Debug: Verificar token CSRF
        Object csrfToken = request.getAttribute("_csrf");
        System.out.println("🔑 CSRF Token en controller: " + (csrfToken != null ? "Presente" : "Ausente"));
        if (csrfToken != null) {
            System.out.println("🔑 CSRF Token details: " + csrfToken.toString());
        }

        return "po/home";
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
                    System.out.println("🎭 PO - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 PO - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }
}