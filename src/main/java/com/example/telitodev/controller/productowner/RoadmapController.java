package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Roadmap;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;       // <- asegúrate de tenerlo
import com.example.telitodev.repository.RoadmapRepository;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class RoadmapController {

    private final UsuarioRepository usuarioRepository;
    private final RoadmapRepository roadmapRepository;
    private final ApiRepository apiRepository; // <- inyectamos para abrir nuevos tramos

    public RoadmapController(UsuarioRepository usuarioRepository,
                             RoadmapRepository roadmapRepository,
                             ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roadmapRepository = roadmapRepository;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/roadmap")
    public String showRoadmapView(Model model , Authentication auth, HttpSession session) {

        // Trae todos los tramos (cada fila = segmento del timeline)
        List<Roadmap> roadmapList = roadmapRepository.findAll();

        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = obtenerUsuarioActual(auth, session);
            model.addAttribute("usuario", usuario);
        }

        List<Map<String,Object>> roadmapMAP = new ArrayList<>();

        if (roadmapList == null || roadmapList.isEmpty()) {
            System.out.println("No hay roadmaps disponibles.");
        } else {
            // Mapeo a la estructura que tu gráfico ya consume: api / start / end / estado
            // IMPORTANTE: ahora "inicio"/"fin" son LocalDate
            roadmapMAP = roadmapList.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());                         // id del tramo (por si lo necesitas)
                m.put("apiId", r.getApi().getIdApi());          // para acciones en UI
                m.put("api", r.getApi().getNombre());
                m.put("start", r.getInicio().toString());
                m.put("end",   r.getFin() != null ? r.getFin().toString() : null);
                // etiqueta de estado para la gráfica (tu JS usa “Próxima/En desarrollo/Nueva”)
                m.put("estado", switch (r.getEstado()) {
                    case PROXIMA        -> "Próxima";
                    case EN_DESARROLLO  -> "En desarrollo";
                    case NUEVA          -> "Nueva";
                    default -> "OLD";
                });
                return m;
            }).toList();
        }

        model.addAttribute("roadmapData", roadmapMAP);
        return "po/roadmap";
    }

    /* =========================
       ENDPOINTS PARA EL UI
       ========================= */

    // DTO simple para PATCH
    public record CambiarEstadoReq(String nuevoEstado, String fecha) {}

    /**
     * Cambiar a estado activo (PROXIMA / EN_DESARROLLO / NUEVA)
     * Regla:
     *  - cerrar tramo abierto (si existe)
     *  - abrir nuevo tramo con inicio = fecha (o hoy) y fin = NULL
     *  - validar transición según flujo estricto
     */
    @PatchMapping("/roadmap/{apiId}/estado")
    @ResponseBody
    @Transactional
    public Map<String, Object> cambiarEstado(@PathVariable Integer apiId,
                                             @RequestBody CambiarEstadoReq req,
                                             Authentication auth, HttpSession session) {
        if (req.nuevoEstado() == null || req.nuevoEstado().isBlank()) {
            throw new IllegalArgumentException("nuevoEstado es requerido");
        }
        final Roadmap.EstadoEvolucion nuevo = parseEstado(req.nuevoEstado());

        // 1) Validar transición
        final String violacion = validarTransicionAdelante(apiId, nuevo);
        if (violacion != null) {
            throw new IllegalArgumentException("Transición no permitida: " + violacion);
        }

        // 2) Si el estado es "OLD", cerramos el tramo abierto (pausa) y no creamos uno nuevo
        LocalDate hoy = (req.fecha() != null && !req.fecha().isBlank())
                ? LocalDate.parse(req.fecha())
                : LocalDate.now();

        if (nuevo == Roadmap.EstadoEvolucion.OLD) {
            // Si el estado es "OLD", cerramos el tramo abierto sin crear uno nuevo
            roadmapRepository.cerrarTramoAbierto(apiId, hoy);
            return Map.of("ok", true, "mensaje", "Estado cambiado a 'OLD' (pausado)");
        }

        // 3) Si no es "OLD", cerramos el tramo abierto y creamos un nuevo tramo con el estado de evolución
        Api api = apiRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API no encontrada"));
        Usuario usuario = obtenerUsuarioActual(auth, session);

        // Cerrar el tramo abierto si existe
        roadmapRepository.cerrarTramoAbierto(apiId, hoy);

        // Crear un nuevo tramo con el estado de evolución (nuevo estado)
        Roadmap tramo = new Roadmap(api, nuevo, hoy, null, usuario);
        roadmapRepository.save(tramo);

        return Map.of("ok", true, "mensaje", "Estado actualizado correctamente");
    }

    /**
     * Terminar (OLD): cerrar tramo abierto y no abrir otro.
     * Resultado: la API queda sin tramo abierto (no monitoreada).
     */
    @PostMapping("/roadmap/{apiId}/terminar")
    @ResponseBody
    @Transactional
    public Map<String, Object> terminar(@PathVariable Integer apiId,
                                        @RequestParam(required = false) String fecha) {
        LocalDate hoy = (fecha != null && !fecha.isBlank())
                ? LocalDate.parse(fecha)
                : LocalDate.now();

        int n = roadmapRepository.cerrarTramoAbierto(apiId, hoy);
        if (n == 0) {
            // No había tramo abierto; ya estaba en OLD. No es error, devolvemos ok igualmente.
        }
        return Map.of("ok", true);
    }

    /* =========================
       Helpers
       ========================= */

    private Roadmap.EstadoEvolucion parseEstado(String s) {
        String u = s.trim().toUpperCase();
        // admitir valores desde la UI en español si hiciera falta
        if (u.contains("PROX")) return Roadmap.EstadoEvolucion.PROXIMA;
        if (u.contains("EN_DES")) return Roadmap.EstadoEvolucion.EN_DESARROLLO;
        if (u.contains("NUEV")) return Roadmap.EstadoEvolucion.NUEVA;
        return Roadmap.EstadoEvolucion.valueOf(u);
    }

    /**
     * Valida el flujo estricto:
     *   PROXIMA -> EN_DESARROLLO -> NUEVA -> OLD (cerrar)
     * Devuelve null si es válida; si no, devuelve un mensaje de violación.
     */
    private String validarTransicionAdelante(Integer apiId, Roadmap.EstadoEvolucion nuevo) {
        // ¿hay tramo abierto?
        var abiertoOpt = roadmapRepository.findByApi_IdApiAndFinIsNull(apiId);
        if (abiertoOpt.isPresent()) {
            Roadmap.EstadoEvolucion actual = abiertoOpt.get().getEstado();
            return switch (actual) {
                case PROXIMA        -> (nuevo == Roadmap.EstadoEvolucion.EN_DESARROLLO) ? null
                        : "PROXIMA → " + nuevo + " (solo se permite EN_DESARROLLO o elegir OLD)";
                case EN_DESARROLLO  -> (nuevo == Roadmap.EstadoEvolucion.NUEVA) ? null
                        : "EN_DESARROLLO → " + nuevo + " (solo se permite NUEVA o elegir OLD)";
                case NUEVA          -> "NUEVA solo puede ir a OLD (usa /terminar)";
                case OLD -> null;
            };
        } else {
            // No hay tramo abierto => está "OLD".
            // Reanudar debe ser "siguiente del último cerrado".
            var lastClosed = roadmapRepository.findTopByApi_IdApiAndFinIsNotNullOrderByFinDesc(apiId);
            if (lastClosed.isEmpty()) {
                // nunca tuvo tramo: el primer estado permitido es PROXIMA
                return (nuevo == Roadmap.EstadoEvolucion.PROXIMA) ? null
                        : "Primera transición debe iniciar en PROXIMA";
            }
            Roadmap.EstadoEvolucion ultimo = lastClosed.get().getEstado();
            return switch (ultimo) {
                case PROXIMA        -> (nuevo == Roadmap.EstadoEvolucion.EN_DESARROLLO) ? null
                        : "Reanudar después de PROXIMA debe ir a EN_DESARROLLO";
                case EN_DESARROLLO  -> (nuevo == Roadmap.EstadoEvolucion.NUEVA) ? null
                        : "Reanudar después de EN_DESARROLLO debe ir a NUEVA";
                case NUEVA          -> "Después de NUEVA, solo OLD. Si quieres iniciar un nuevo ciclo, define la regla (ej. volver a PROXIMA).";

                case OLD -> null; // debería ser unreachable
            };
        }
    }

    /** Helper para usuario actual (igual que el tuyo) */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        if (isImpersonating != null && isImpersonating) {
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    return impersonatedUser;
                }
            }
        }
        return usuarioRepository.findByCorreo(auth.getName());
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
