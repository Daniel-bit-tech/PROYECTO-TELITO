package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
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
public class RoadmapController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final RoadmapRepository roadmapRepository;
    public RoadmapController(UsuarioRepository usuarioRepository, RoadmapRepository roadmapRepository) {

        this.usuarioRepository = usuarioRepository;
        this.roadmapRepository = roadmapRepository;
    }

    @GetMapping("/roadmap")
    public String showRoadmapView(Model model , Authentication auth, HttpSession session) {
        try {
            if (auth != null && auth.isAuthenticated()) {
                // Obtener el usuario correcto considerando impersonación
                Usuario usuario = getCurrentUser(auth, session);
                
                // Agregar atributos de impersonación
                addImpersonationAttributes(model, session);
                
                model.addAttribute("usuario", usuario);
            }

            // Obtener todos los roadmaps desde el repository
            List<Roadmap> roadmapList = roadmapRepository.findAll();
            
            // Crear una lista simple para evitar problemas con el enum
            List<Map<String,Object>> roadmapMAP = new ArrayList<>();

            if (roadmapList != null && !roadmapList.isEmpty()) {
                for (Roadmap roadmap : roadmapList) {
                    try {
                        Map<String, Object> m = new HashMap<>();
                        
                        // Usar valores seguros
                        m.put("api", roadmap.getApi() != null ? roadmap.getApi().getNombre() : "Sin API");
                        
                        if (roadmap.getFechaInicio() != null) {
                            m.put("start", roadmap.getFechaInicio().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate().toString());
                        } else {
                            m.put("start", "");
                        }
                        
                        if (roadmap.getFechaFin() != null) {
                            m.put("end", roadmap.getFechaFin().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate().toString());
                        } else {
                            m.put("end", "");
                        }
                        
                        // Manejo seguro del estado - usar un valor por defecto si hay problemas
                        try {
                            m.put("estado", roadmap.getEstado() != null ? roadmap.getEstado().getDisplayName() : "Sin estado");
                        } catch (Exception e) {
                            m.put("estado", "Estado desconocido");
                            System.err.println("Error al obtener estado del roadmap: " + e.getMessage());
                        }
                        
                        roadmapMAP.add(m);
                    } catch (Exception e) {
                        System.err.println("Error procesando roadmap individual: " + e.getMessage());
                        // Continuar con el siguiente roadmap
                    }
                }
            }

            model.addAttribute("roadmapData", roadmapMAP);
            return "po/roadmap";
            
        } catch (Exception e) {
            System.err.println("Error en showRoadmapView: " + e.getMessage());
            e.printStackTrace();
            
            // En caso de error, mostrar una vista vacía
            model.addAttribute("roadmapData", new ArrayList<>());
            model.addAttribute("error", "Error al cargar el roadmap: " + e.getMessage());
            return "po/roadmap";
        }
    }


    // obtener solo un roadmap


    // ----------------------------- IGNORA ESTO POR AHORA ---------------------------------------
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "po/roadmapGestion";
    }
    
    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "po/roadmapDetalle";
    }
    // -------------------------------------------------------------------------------------------

}
