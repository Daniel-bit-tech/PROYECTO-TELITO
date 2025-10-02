package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.*;
import com.example.telitodev.service.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.time.ZoneId;
import java.util.*;


@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class RoadmapController {

    final UsuarioRepository usuarioRepository;
    final RoadmapRepository roadmapRepository;
    public RoadmapController(UsuarioRepository usuarioRepository, RoadmapRepository roadmapRepository) {

        this.usuarioRepository = usuarioRepository;
        this.roadmapRepository = roadmapRepository;
    }

    @GetMapping("/roadmap")
    public String showRoadmapView(Model model , Authentication auth, HttpSession session) {

        // Obtener todos los roadmaps desde el repository
        List<Roadmap> roadmapList = roadmapRepository.findAll();

        if (auth != null && auth.isAuthenticated()) {
            // Obtener el usuario correcto considerando impersonación
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        List<Map<String,Object>> roadmapMAP = new ArrayList<>();

        // Verificación de datos antes de pasarlos a la vista
        if (roadmapList == null || roadmapList.isEmpty()) {
            System.out.println("No hay roadmaps disponibles.");
        } else {
            for (Roadmap roadmap : roadmapList) {
                System.out.println("Roadmap - API: " + roadmap.getApi() + ", Estado: " + roadmap.getEstado() + ", Fecha Inicio: " + roadmap.getFechaInicio() + ", Fecha Fin: " + roadmap.getFechaFin());
            }
            roadmapMAP = roadmapList.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("api", r.getApi().getNombre());
                m.put("start", r.getFechaInicio().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().toString());
                m.put("end", r.getFechaFin() != null
                        ? r.getFechaFin().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().toString()
                        : null);
                m.put("estado", r.getEstado());
                return m;
            }).toList();
        }

        model.addAttribute("roadmapData", roadmapMAP); // Pasar los datos al modelo
        return "po/roadmap"; // Vista donde se muestra el roadmap
    }


    // obtener solo un roadmap


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
                    System.out.println("🎭 Roadmap - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }

        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Roadmap - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }


    // ----------------------------- IGNORA ESTO POR AHORA ---------------------------------------
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        return "po/roadmapGestion";
    }
    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        return "po/roadmapDetalle";
    }
    // -------------------------------------------------------------------------------------------

}
