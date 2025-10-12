package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class RoadmapController {

    final UsuarioRepository usuarioRepository;
    final RoadmapRepository roadmapRepository;
    final ApiRepository apiRepository;

    public RoadmapController(UsuarioRepository usuarioRepository,
                             RoadmapRepository roadmapRepository,
                             ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roadmapRepository = roadmapRepository;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/roadmap")
    public String showRoadmapView(Model model, Authentication auth, HttpSession session) {
        System.out.println("🔍 Iniciando carga del roadmap...");

        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
            System.out.println("👤 Usuario: " + usuario.getNombre());
        }

        // Obtener todas las APIs
        List<Api> allApis = apiRepository.findAll();
        System.out.println("📊 Total de APIs encontradas: " + allApis.size());

        List<Map<String,Object>> roadmapData = new ArrayList<>();
        List<Map<String,Object>> roadmapControls = new ArrayList<>();

        // Obtener datos para los controles (todas las APIs)
        for (Api api : allApis) {
            Map<String, Object> control = new HashMap<>();
            control.put("apiId", api.getIdApi());
            control.put("apiNombre", api.getNombre());

            // Buscar el estado actual de la API
            Optional<Roadmap> roadmapOpt = roadmapRepository.findByApiIdApi(api.getIdApi());
            String estadoActual;
            if (roadmapOpt.isPresent()) {
                estadoActual = roadmapOpt.get().getEstadoString();
                System.out.println("✅ API " + api.getNombre() + " - Estado: " + estadoActual);

                // Solo agregar a roadmapData si no está en "Sin estado"
                if (!"Sin estado".equals(estadoActual)) {
                    Map<String, Object> roadmapItem = new HashMap<>();
                    roadmapItem.put("api", api.getNombre());
                    roadmapItem.put("apiId", api.getIdApi());

                    // Generar fechas basadas en el estado actual
                    Date fechaModificacion = roadmapOpt.get().getFechaModificacion();
                    Date startDate;
                    Date endDate;

                    switch(estadoActual) {
                        case "Próxima":
                            // Próxima: empieza cuando se asignó el estado, dura 15 días
                            startDate = fechaModificacion;
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(fechaModificacion);
                            cal.add(Calendar.DAY_OF_MONTH, 15);
                            endDate = cal.getTime();
                            break;

                        case "En desarrollo":
                            // En desarrollo: empieza CUANDO SE CAMBIÓ a este estado, dura 30 días
                            startDate = fechaModificacion; // Fecha cuando se cambió a "En desarrollo"
                            Calendar cal2 = Calendar.getInstance();
                            cal2.setTime(fechaModificacion);
                            cal2.add(Calendar.DAY_OF_MONTH, 30);
                            endDate = cal2.getTime();
                            break;

                        case "Nueva":
                            // Nueva: empieza CUANDO SE CAMBIÓ a este estado, dura 15 días
                            startDate = fechaModificacion; // Fecha cuando se cambió a "Nueva"
                            Calendar cal3 = Calendar.getInstance();
                            cal3.setTime(fechaModificacion);
                            cal3.add(Calendar.DAY_OF_MONTH, 15);
                            endDate = cal3.getTime();
                            break;

                        default:
                            startDate = fechaModificacion;
                            endDate = fechaModificacion;
                    }

                    roadmapItem.put("start", startDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate().toString());
                    roadmapItem.put("end", endDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate().toString());
                    roadmapItem.put("estado", estadoActual);

                    roadmapData.add(roadmapItem);
                }
            } else {
                estadoActual = "Sin estado";
                System.out.println("❌ API " + api.getNombre() + " - Sin roadmap, estado por defecto: " + estadoActual);
            }

            control.put("currentEstado", estadoActual);
            roadmapControls.add(control);
        }

        System.out.println("📈 Roadmap Data: " + roadmapData.size() + " elementos");
        System.out.println("🎮 Roadmap Controls: " + roadmapControls.size() + " elementos");

        model.addAttribute("roadmapData", roadmapData);
        model.addAttribute("roadmapControls", roadmapControls);

        return "po/roadmap";
    }

    // Endpoint para cambiar el estado de una API
    @PatchMapping("/roadmap/{apiId}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoApi(
            @PathVariable Integer apiId,
            @RequestBody Map<String, String> request) {

        System.out.println("🔄 Cambiando estado de API: " + apiId);

        try {
            String nuevoEstado = request.get("nuevoEstado");
            System.out.println("🎯 Nuevo estado: " + nuevoEstado);

            // Validar estado
            if (!isEstadoValido(nuevoEstado)) {
                return ResponseEntity.badRequest().body("Estado no válido: " + nuevoEstado);
            }

            // Buscar la API
            Optional<Api> apiOpt = apiRepository.findById(apiId);
            if (!apiOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Api api = apiOpt.get();
            System.out.println("📝 API encontrada: " + api.getNombre());

            // Buscar si ya existe un roadmap para esta API
            Optional<Roadmap> roadmapOpt = roadmapRepository.findByApiIdApi(apiId);

            Roadmap roadmap;
            if (roadmapOpt.isPresent()) {
                // Actualizar estado existente
                roadmap = roadmapOpt.get();
                System.out.println("📝 Actualizando roadmap existente - Estado anterior: " + roadmap.getEstado());
                roadmap.setEstadoString(nuevoEstado);
            } else {
                // Crear nuevo roadmap
                System.out.println("🆕 Creando nuevo roadmap");
                roadmap = new Roadmap(nuevoEstado, api);
            }

            Roadmap saved = roadmapRepository.save(roadmap);
            System.out.println("💾 Roadmap guardado - ID: " + saved.getId() + ", Estado: " + saved.getEstado());

            return ResponseEntity.ok().body(Map.of(
                    "message", "Estado actualizado correctamente",
                    "apiId", apiId,
                    "nuevoEstado", nuevoEstado
            ));

        } catch (Exception e) {
            System.out.println("❌ Error al actualizar estado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar estado: " + e.getMessage());
        }
    }

    // Endpoint para restablecer una API a "Sin estado"
    @PostMapping("/roadmap/{apiId}/restablecer")
    @ResponseBody
    public ResponseEntity<?> restablecerApi(@PathVariable Integer apiId) {
        System.out.println("🔄 Restableciendo API: " + apiId);

        try {
            // Buscar si existe un roadmap para esta API
            Optional<Roadmap> roadmapOpt = roadmapRepository.findByApiIdApi(apiId);

            if (roadmapOpt.isPresent()) {
                Roadmap roadmap = roadmapOpt.get();
                System.out.println("📝 Roadmap encontrado - Estado anterior: " + roadmap.getEstado());
                roadmap.setEstadoString("Sin estado");
                Roadmap saved = roadmapRepository.save(roadmap);
                System.out.println("💾 Roadmap actualizado - Estado nuevo: " + saved.getEstado());
            } else {
                System.out.println("ℹ️ No existe roadmap, ya está en estado por defecto");
            }

            return ResponseEntity.ok().body(Map.of(
                    "message", "API restablecida a 'Sin estado'",
                    "apiId", apiId
            ));

        } catch (Exception e) {
            System.out.println("❌ Error al restablecer API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al restablecer API: " + e.getMessage());
        }
    }

    // Método para validar estados
    private boolean isEstadoValido(String estado) {
        return Arrays.asList("Nueva", "En desarrollo", "Próxima", "Sin estado")
                .contains(estado);
    }

    /**
     * Método helper para obtener el usuario correcto durante impersonación
     */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        // Verificar si hay impersonación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");

        if (isImpersonating != null && isImpersonating) {
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    System.out.println("🎭 Roadmap - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Roadmap - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }

    // ----------------------------- IGNORA ESTO POR AHORA ---------------------------------------
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        return "po/roadmapGestion";
    }

    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);
        return "po/roadmapDetalle";
    }
    // -------------------------------------------------------------------------------------------
}