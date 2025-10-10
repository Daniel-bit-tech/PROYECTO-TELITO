package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.BacklogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class BacklogController {

    final UsuarioRepository usuarioRepository;
    final BacklogService backlogService;

    public BacklogController(UsuarioRepository usuarioRepository, BacklogService backlogService) {
        this.usuarioRepository = usuarioRepository;
        this.backlogService = backlogService;
    }

    @GetMapping("/backlog")
    public String showBacklogView(Model model, Authentication auth, HttpSession session,
                                  @RequestParam(name = "search", required = false) String search,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {

        // Obtener el usuario correcto considerando impersonación
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        // ✅ CAMBIO: Usar el service para paginación y búsqueda
        Page<Backlog> backlogPage = backlogService.getBacklogsPaginated(search, page, size);

        // ✅ CAMBIO: Enviar Page en lugar de List
        model.addAttribute("backlogPage", backlogPage);
        model.addAttribute("search", search == null ? "" : search);

        return "po/backlog";
    }

    @PostMapping("/backlog/marcar-como-resuelto/{id}")
    public String markAsResolved(@PathVariable("id") Integer id,
                                 @RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {

        // ✅ CAMBIO: Usar el service para marcar como resuelto
        backlogService.marcarComoResuelto(id);

        // ✅ CAMBIO: Redirigir manteniendo los parámetros de búsqueda y paginación
        StringBuilder redirectUrl = new StringBuilder("redirect:/po/backlog?page=").append(page).append("&size=").append(size);
        if (search != null && !search.isEmpty()) {
            redirectUrl.append("&search=").append(search);
        }

        return redirectUrl.toString();
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