package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpServletRequest;
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
            System.out.println("👤 Usuario (para roadmap): " + usuario.getNombre());

            // 🔹 AQUÍ filtramos las APIs según equipo/organización
            List<Api> allApis;

            if (usuario.getEquipo() != null) {
                // Caso normal: PO con equipo asignado → solo APIs de su equipo
                System.out.println("📌 Usuario con equipo: " + usuario.getEquipo().getNombre());
                allApis = apiRepository.findByEquipo(usuario.getEquipo());

            } else if (usuario.getOrganizacion() != null) {
                // Si por diseño tienes PO a nivel organización (sin equipo concreto)
                System.out.println("🏢 Usuario sin equipo pero con organización: "
                        + usuario.getOrganizacion().getNombre());
                allApis = apiRepository.findApisDisponiblesParaOrganizacion(
                        usuario.getOrganizacion().getIdOrganizacion()
                );

            } else {
                // Usuario sin equipo ni organización (caso raro)
                System.out.println("⚠️ Usuario sin equipo ni organización, no se mostrarán APIs");
                allApis = Collections.emptyList();
            }

            System.out.println("📊 Total de APIs visibles para el usuario: " + allApis.size());

            // A partir de aquí tu código tal cual, usando allApis
            List<Map<String,Object>> roadmapData = new ArrayList<>();
            List<Map<String,Object>> roadmapControls = new ArrayList<>();

            for (Api api : allApis) {
                Map<String, Object> control = new HashMap<>();
                control.put("apiId", api.getIdApi());
                control.put("apiNombre", api.getNombre());

                Optional<Roadmap> roadmapOpt = roadmapRepository.findByApiIdApi(api.getIdApi());
                String estadoActual;
                if (roadmapOpt.isPresent()) {
                    estadoActual = roadmapOpt.get().getEstado();
                    System.out.println("✅ API " + api.getNombre() + " - Estado: " + estadoActual);

                    if (!"Sin estado".equals(estadoActual)) {
                        Map<String, Object> roadmapItem = new HashMap<>();
                        roadmapItem.put("api", api.getNombre());
                        roadmapItem.put("apiId", api.getIdApi());

                        Date fechaModificacion = roadmapOpt.get().getFechaModificacion();
                        Date startDate;
                        Date endDate;

                        switch (estadoActual) {
                            case "Próxima":
                                startDate = fechaModificacion;
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(fechaModificacion);
                                cal.add(Calendar.DAY_OF_MONTH, 15);
                                endDate = cal.getTime();
                                break;

                            case "En desarrollo":
                                startDate = fechaModificacion;
                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(fechaModificacion);
                                cal2.add(Calendar.DAY_OF_MONTH, 30);
                                endDate = cal2.getTime();
                                break;

                            case "Nueva":
                                startDate = fechaModificacion;
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

        // Si por algún motivo no hay auth
        return "redirect:/login";
    }


    @PostMapping("/roadmap/{apiId}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoApi(
            @PathVariable Integer apiId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        System.out.println("=== 🚀 SOLICITUD CAMBIAR ESTADO ===");
        System.out.println("📝 API ID: " + apiId);
        System.out.println("🌐 Método: " + httpRequest.getMethod());
        System.out.println("📍 URL: " + httpRequest.getRequestURL());
        System.out.println("📦 Request Body: " + request);

        try {
            String nuevoEstado = request.get("nuevoEstado");

            System.out.println("🎯 Nuevo estado recibido: " + nuevoEstado);

            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                System.out.println("❌ ERROR: Estado nulo o vacío");
                return ResponseEntity.badRequest().body("El campo 'nuevoEstado' es requerido");
            }

            // Validar estado
            if (!isEstadoValido(nuevoEstado)) {
                System.out.println("❌ ERROR: Estado no válido: " + nuevoEstado);
                return ResponseEntity.badRequest().body("Estado no válido: " + nuevoEstado);
            }

            // Buscar la API
            Optional<Api> apiOpt = apiRepository.findById(apiId);
            if (!apiOpt.isPresent()) {
                System.out.println("❌ ERROR: API no encontrada con ID: " + apiId);
                return ResponseEntity.notFound().build();
            }

            Api api = apiOpt.get();
            System.out.println("📝 API encontrada: " + api.getNombre());

            // Buscar si ya existe un roadmap para esta API
            Optional<Roadmap> roadmapOpt = roadmapRepository.findFirstByApiIdApiOrderByFechaModificacionDesc(apiId);

            Roadmap roadmap;
            if (roadmapOpt.isPresent()) {
                // Actualizar estado existente
                roadmap = roadmapOpt.get();
                System.out.println("📝 Actualizando roadmap existente - Estado anterior: " + roadmap.getEstado());
                roadmap.setEstado(nuevoEstado);
                roadmap.setFechaModificacion(new Date());
            } else {
                // Crear nuevo roadmap
                System.out.println("🆕 Creando nuevo roadmap");
                roadmap = new Roadmap(api, nuevoEstado);
            }

            Roadmap saved = roadmapRepository.save(roadmap);
            System.out.println("💾 Roadmap guardado - ID: " + saved.getId() + ", Estado: " + saved.getEstado());

            Map<String, Object> response = Map.of(
                    "message", "Estado actualizado correctamente",
                    "apiId", apiId,
                    "nuevoEstado", nuevoEstado,
                    "apiNombre", api.getNombre()
            );

            System.out.println("✅ RESPUESTA EXITOSA: " + response);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR EXCEPCIÓN: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @PostMapping("/roadmap/{apiId}/restablecer")
    @ResponseBody
    public ResponseEntity<?> restablecerApi(@PathVariable Integer apiId, HttpServletRequest httpRequest) {
        System.out.println("=== 🔄 SOLICITUD RESTABLECER API ===");
        System.out.println("📝 API ID: " + apiId);
        System.out.println("🌐 Método: " + httpRequest.getMethod());
        System.out.println("📍 URL: " + httpRequest.getRequestURL());

        try {
            // Buscar si existe un roadmap para esta API
            Optional<Roadmap> roadmapOpt = roadmapRepository.findFirstByApiIdApiOrderByFechaModificacionDesc(apiId);

            if (roadmapOpt.isPresent()) {
                Roadmap roadmap = roadmapOpt.get();
                System.out.println("📝 Roadmap encontrado - Estado anterior: " + roadmap.getEstado());
                roadmap.setEstado("Sin estado");
                roadmap.setFechaModificacion(new Date());
                Roadmap saved = roadmapRepository.save(roadmap);
                System.out.println("💾 Roadmap actualizado - Estado nuevo: " + saved.getEstado());
            } else {
                System.out.println("ℹ️ No existe roadmap, ya está en estado por defecto");
            }

            Map<String, Object> response = Map.of(
                    "message", "API restablecida a 'Sin estado'",
                    "apiId", apiId
            );

            System.out.println("✅ RESPUESTA EXITOSA: " + response);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            System.out.println("❌ ERROR EXCEPCIÓN: " + e.getMessage());
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
        if (usuario == null) {
            // Try corporate email if not found by personal email
            usuario = usuarioRepository.findByCorreoCorporativo(auth.getName());
            if (usuario != null) {
                System.out.println("👤 Roadmap - Usuario autenticado por correo corporativo: " + usuario.getNombre());
            }
        } else {
            System.out.println("👤 Roadmap - Usando datos del usuario autenticado: " + usuario.getNombre());
        }
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