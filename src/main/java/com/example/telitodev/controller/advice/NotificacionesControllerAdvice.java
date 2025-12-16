package com.example.telitodev.controller.advice;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.NotificacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ControllerAdvice para agregar automáticamente el contador de notificaciones no leídas
 * a todas las vistas de la aplicación.
 */
@ControllerAdvice
public class NotificacionesControllerAdvice {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionesControllerAdvice(NotificacionRepository notificacionRepository, 
                                         UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Agrega el contador de notificaciones no leídas al modelo de todas las vistas.
     * Solo se ejecuta si el usuario está autenticado.
     */
    @ModelAttribute
    public void addNotificacionesNoLeidas(Model model, Authentication auth, HttpSession session) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Verificar si hay impersonación activa
                String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
                String dni;
                
                if (impersonatedUserDni != null) {
                    // Si hay impersonación, usar el DNI del usuario impersonado
                    dni = impersonatedUserDni;
                } else {
                    // Si no hay impersonación, obtener el usuario actual
                    Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
                    if (usuario != null) {
                        dni = usuario.getDni();
                    } else {
                        return; // Si no se encuentra el usuario, salir
                    }
                }
                
                // Contar notificaciones no leídas
                long notificacionesNoLeidas = notificacionRepository.findByUsuarioDniAndLeidoFalse(dni).size();
                model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
                
            } catch (Exception e) {
                // En caso de error, simplemente no agregar el atributo
                // Esto evita que se rompa la aplicación si hay algún problema
            }
        }
    }
}
