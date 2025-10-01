package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.BacklogRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class BacklogController {

    final UsuarioRepository usuarioRepository;
    final BacklogRepository backlogRepository;
    public BacklogController(UsuarioRepository usuarioRepository, BacklogRepository backlogRepository) {
        this.usuarioRepository = usuarioRepository;
        this.backlogRepository = backlogRepository;
    }

    @GetMapping("/backlog")
    public String showBacklogView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        List<Backlog> backlogs = backlogRepository.findAll();
        model.addAttribute("backlogs", backlogs);
        return "po/backlog";
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
                    System.out.println("🎭 Backlog - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Backlog - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }

}
