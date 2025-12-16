package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.BacklogRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

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
    public String showBacklogView(Model model, Authentication auth, HttpSession session,
                                  @RequestParam(name = "q", required = false, defaultValue = "") String q,
                                  @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                  @RequestParam(name = "size", required = false, defaultValue = "8") int size) {
        // Obtener el usuario correcto considerando impersonación
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }
        
        // Validar parámetros
        if (size <= 0 || size > 100) size = 8; // Límite máximo de 100 elementos por página
        if (page < 0) page = 0;
        
        // Crear objeto Pageable para la paginación
        Pageable pageable = PageRequest.of(page, size, Sort.by("idBacklog").descending());
        
        // Buscar con paginación
        Page<Backlog> backlogPage;
        if (q != null && !q.trim().isEmpty()) {
            backlogPage = backlogRepository.findBySearchTerm(q.trim(), pageable);
        } else {
            backlogPage = backlogRepository.findAll(pageable);
        }
        
        // Agregar atributos al modelo para la vista
        model.addAttribute("backlogs", backlogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", backlogPage.getTotalPages());
        model.addAttribute("totalElements", backlogPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("q", q);
        model.addAttribute("hasNext", backlogPage.hasNext());
        model.addAttribute("hasPrevious", backlogPage.hasPrevious());
        
        // Calcular rango de elementos mostrados
        int startElement = page * size + 1;
        int endElement = Math.min(startElement + size - 1, (int) backlogPage.getTotalElements());
        model.addAttribute("startElement", startElement);
        model.addAttribute("endElement", endElement);
        
        return "po/backlog";
    }

    @PostMapping("/backlog/marcar-como-resuelto/{id}")
    public String markAsResolved(@PathVariable("id") Integer id) {
        Backlog backlog = backlogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Backlog no encontrado"));
        backlog.setEstadoBacklog("Resuelto");  // Cambia el estado a "Resuelto"
        backlogRepository.save(backlog);        // Guarda el cambio
        return "redirect:/po/backlog";             // Recarga la misma vista
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
        if (usuario == null) {
            // Try corporate email if not found by personal email
            usuario = usuarioRepository.findByCorreoCorporativo(auth.getName());
            if (usuario != null) {
                System.out.println("👤 Backlog - Usuario autenticado por correo corporativo: " + usuario.getNombre());
            }
        } else {
            System.out.println("👤 Backlog - Usando datos del usuario autenticado: " + usuario.getNombre());
        }
        return usuario;
    }

}
