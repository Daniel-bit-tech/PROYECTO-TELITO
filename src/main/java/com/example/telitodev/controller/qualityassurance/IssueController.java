package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SUPERADMIN')")
public class IssueController extends BaseController {
    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ReporteRepository reporteRepository;
    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private AdjuntoRepository adjuntoRepository;
    @Autowired
    private EvidenciaRepository evidenciaRepository;
    @Autowired
    private NotificacionRepository notificacionRepository;
    @Autowired
    private ActividadRecienteRepository actividadRecienteRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @GetMapping("/issues")
    public String showIssueView(Model model,
                                Authentication auth,
                                HttpSession session,
                                @RequestParam(value = "tags", required = false) List<String> estados,
                                @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                @RequestParam(value = "nombre", required = false) String nombre,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        Timestamp inicio = null;
        Timestamp fin = null;
        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) inicio = Timestamp.valueOf(fechaInicio + " 00:00:00");
            if (fechaFin != null && !fechaFin.isEmpty()) fin = Timestamp.valueOf(fechaFin + " 23:59:59");
        } catch (Exception e) {
            // Manejar el error, por ejemplo, loggearlo o mostrar un mensaje al usuario
            e.printStackTrace();
        }

        // Validar y ajustar el número de página antes de crear el Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Issue> issuePage = issueRepository.findByFiltersForTeam(estados, inicio, fin, nombre, usuario.getDni(), pageable);

        // Si la página solicitada está fuera de rango, ajustar a la última página válida
        if (page >= issuePage.getTotalPages() && issuePage.getTotalPages() > 0) {
            page = issuePage.getTotalPages() - 1;
            pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            issuePage = issueRepository.findByFiltersForTeam(estados, inicio, fin, nombre, usuario.getDni(), pageable);
        }

        // Pre-inicializar las relaciones de API para evitar EntityNotFoundException en la vista
        // Creamos un mapa con información segura de las APIs
        java.util.Map<Integer, java.util.Map<String, String>> apiSafeData = new java.util.HashMap<>();
        for (Issue issue : issuePage.getContent()) {
            Integer issueId = issue.getId().getIdIssue();
            java.util.Map<String, String> apiData = new java.util.HashMap<>();
            try {
                if (issue.getReporte() != null && issue.getReporte().getApi() != null) {
                    Api api = issue.getReporte().getApi();
                    apiData.put("nombre", api.getNombre());
                    apiData.put("dominio", api.getDominio() != null ? 
                        api.getDominio().getNombre() : "N/A");
                    apiData.put("tag", api.getTag() != null ? 
                        api.getTag().getNombre() : "N/A");
                } else {
                    apiData.put("nombre", "API no disponible");
                    apiData.put("dominio", "N/A");
                    apiData.put("tag", "N/A");
                }
            } catch (Exception e) {
                // Capturar cualquier excepción de lazy loading o entidad no encontrada
                apiData.put("nombre", "API no disponible");
                apiData.put("dominio", "N/A");
                apiData.put("tag", "N/A");
            }
            apiSafeData.put(issueId, apiData);
        }
        model.addAttribute("apiSafeData", apiSafeData);

        // Pasamos el objeto Page completo a la vista para mayor consistencia
        model.addAttribute("issuePage", issuePage);
        model.addAttribute("currentPage", issuePage.getNumber());
        model.addAttribute("totalPages", issuePage.getTotalPages());

        // Devolvemos los parámetros de filtro a la vista para mantener su estado
        model.addAttribute("selectedEstados", estados);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("nombre", nombre);

        return "qa/issues";
    }

    @GetMapping("/issueDetalle/{idIssue}/{idReporte}")
    public String showIssueDetalleView(Model model, Authentication auth, HttpSession session,
                                       @PathVariable Integer idIssue, @PathVariable Integer idReporte) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // Crear el IssueId usando los dos parámetros de la URL
        IssueId issueId = new IssueId(idIssue, idReporte);

        // Buscar el Issue utilizando el IssueId
        Issue issue = issueRepository.findById(issueId).orElse(null);

        if (issue == null) {
            model.addAttribute("error", "Issue no encontrado");
            return "error/error";  // Redirige a una página de error personalizada
        }

        // Recuperar evidencias asociadas al Reporte del Issue
        Reporte reporte = issue.getReporte();
        List<Evidencia> evidencias = evidenciaRepository.findByReporteIdReporte(reporte.getIdReporte());
        model.addAttribute("evidencias", evidencias);

        // Recuperar los comentarios relacionados con este Issue
        List<Comentario> comentarios = issue.getComentarios();
        model.addAttribute("comentarios", comentarios);  // Pasa los comentarios a la vista

        model.addAttribute("issue", issue);  // Pasa el Issue a la vista
        return "qa/issueDetalle";  // Vista para mostrar los detalles del Issue
    }



    @GetMapping("/issueRealizar")
    public String madeIssue(Model model, Authentication auth, HttpSession session,
                            @RequestParam("idReporte") Integer idReporte) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        // Obtener el reporte por id
        Reporte reporte = reporteRepository.findById(idReporte).orElse(null);
        if (reporte == null) {
            return "redirect:/qa/issues?error=Reporte no encontrado";
        }

        // Verificar que el reporte tiene estado "Fallido"
        if (!"Fallido".equals(reporte.getEstado())) {
            return "redirect:/qa/issues?error=Solo puedes crear un Issue para reportes en estado Fallido";
        }

        model.addAttribute("reporte", reporte); // Pasamos el reporte a la vista
        return "qa/issueRealizar";
    }

    //Creando un nuevo issue
    @PostMapping("/crearIssue")
    public String crearIssue(Model model, Authentication auth,
                             @RequestParam("idReporte") Integer idReporte,
                             @RequestParam("descripcion") String descripcion,
                             @RequestParam("estado") String estado,
                             RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        Reporte reporte = reporteRepository.findById(idReporte).orElse(null);
        if (reporte == null) {
            redirectAttributes.addFlashAttribute("error", "Reporte no encontrado.");
            return "redirect:/qa/reportes";
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorDescripcion", "La descripción no puede estar vacía.");
            return "redirect:/qa/reporteDetalle?idReporte=" + idReporte;
        }

        if (descripcion.length() > 200) {
            redirectAttributes.addFlashAttribute("errorDescripcion", "La descripción no puede superar los 200 caracteres.");
            return "redirect:/qa/reporteDetalle?idReporte=" + idReporte;
        }

        if (!"Fallido".equals(reporte.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes crear un Issue para reportes en estado 'Fallido'.");
            return "redirect:/qa/reportes";
        }

        IssueId issueId = new IssueId();
        issueId.setIdReporte(idReporte);

        Issue newIssue = new Issue();
        newIssue.setId(issueId);
        newIssue.setDescripcion(descripcion);
        newIssue.setEstado("Reportado");
        newIssue.setReporte(reporte);
        newIssue.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        newIssue.setCreador(usuario);
        issueRepository.save(newIssue);

        // NOTIFICAR SOLO A DESARROLLADORES Y QAS DEL EQUIPO
        Equipo equipoApi = newIssue.getReporte().getApi().getEquipo();
        if (equipoApi != null && equipoApi.getUsuarios() != null) {
            for (Usuario miembro : equipoApi.getUsuarios()) {
                // No notificar al creador del issue
                if (!miembro.getDni().equals(usuario.getDni())) {
                    // Solo notificar a DEV y QA
                    String rolMiembro = miembro.getRol().getNombreRol();
                    if ("DEV".equals(rolMiembro) || "QA".equals(rolMiembro)) {
                        Notificacion notif = new Notificacion();
                        notif.setMensaje("Se ha creado un nuevo issue para la API: " + 
                            newIssue.getReporte().getApi().getNombre());
                        notif.setLeido(false);
                        notif.setFecha(new Timestamp(System.currentTimeMillis()));
                        notif.setUsuario(miembro);
                        notificacionRepository.save(notif);
                    }
                }
            }
        }

        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Issue");
        actividad.setDescripcion("Has creado un issue para la api " + newIssue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        return "redirect:/qa/issues";
    }

    @PostMapping("/crearComentario")
    @ResponseBody
    public ResponseEntity<?> guardarComentario(@RequestParam("comentario") String comentario,
                                    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                                    @RequestParam("idIssue") Integer idIssue,
                                    @RequestParam("idReporte") Integer idReporte,
                                    Authentication auth) {

        Map<String, String> response = new HashMap<>();

        if (comentario == null || comentario.trim().isEmpty()) {
            response.put("error", "El comentario no puede estar vacío.");
            return ResponseEntity.badRequest().body(response);
        }

        if (comentario.length() > 400) {
            response.put("error", "El comentario no puede superar los 400 caracteres.");
            return ResponseEntity.badRequest().body(response);
        }

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            response.put("error", "Issue no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Validar que el issue no esté cerrado
        if ("Corregido".equals(issue.getEstado())) {
            response.put("error", "No se pueden agregar comentarios. El QA ya ha cerrado este issue.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Comentario newComentario = new Comentario();
        newComentario.setComentario(comentario);
        newComentario.setFecha(new Timestamp(System.currentTimeMillis()));
        newComentario.setIssue(issue);
        newComentario.setUsuario(usuario);

        if (archivos != null && archivos.length > 0) {
            archivos = Arrays.stream(archivos)
                    .filter(file -> !file.isEmpty())
                    .toArray(MultipartFile[]::new);
        }

        if (archivos != null && archivos.length > 5) {
            response.put("error", "Máximo 5 archivos permitidos");
            return ResponseEntity.badRequest().body(response);
        }

        if (archivos != null && archivos.length > 0) {
            for (MultipartFile archivo : archivos) {
                if (archivo.getSize() > MAX_FILE_SIZE) {
                    response.put("error", "El archivo es demasiado grande. Máximo 5MB");
                    return ResponseEntity.badRequest().body(response);
                }

                String contentType = archivo.getContentType();
                if (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("text/plain")) {
                    response.put("error", "Solo se permiten archivos .png, .jpg, .jpeg, .log o .txt");
                    return ResponseEntity.badRequest().body(response);
                }

                try {
                    Adjunto adjunto = new Adjunto();
                    adjunto.setNombre(archivo.getOriginalFilename());
                    adjunto.setComentario(newComentario);
                    adjunto.setArchivo(archivo.getBytes());
                    adjuntoRepository.save(adjunto);
                } catch (IOException e) {
                    e.printStackTrace();
                    response.put("error", "Error al guardar el archivo");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
        }

        comentarioRepository.save(newComentario);

        // NOTIFICAR SOLO A DESARROLLADORES Y QAS DEL EQUIPO
        Equipo equipoApi = issue.getReporte().getApi().getEquipo();
        if (equipoApi != null && equipoApi.getUsuarios() != null) {
            for (Usuario miembro : equipoApi.getUsuarios()) {
                // No notificar al que comentó
                if (!miembro.getDni().equals(usuario.getDni())) {
                    // Solo notificar a DEV y QA
                    String rolMiembro = miembro.getRol().getNombreRol();
                    if ("DEV".equals(rolMiembro) || "QA".equals(rolMiembro)) {
                        Notificacion notif = new Notificacion();
                        notif.setMensaje("El usuario " + usuario.getNombre() +
                            " comentó en el issue de la API: " + issue.getReporte().getApi().getNombre());
                        notif.setLeido(false);
                        notif.setFecha(new Timestamp(System.currentTimeMillis()));
                        notif.setUsuario(miembro);
                        notificacionRepository.save(notif);
                    }
                }
            }
        }

        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Comentario");
        actividad.setDescripcion("Has dejado un comentario en el issue " + issue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        response.put("success", "Comentario agregado exitosamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/issueCerrar/{idIssue}/{idReporte}")
    public String cerrarIssue(@PathVariable Integer idIssue, @PathVariable Integer idReporte,
                          RedirectAttributes redirectAttributes, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);

        if (issue == null) {
            redirectAttributes.addFlashAttribute("error", "Issue no encontrado");
            return "redirect:/qa/issues";
        }

        // Cambiar estado del issue a Corregido
        issue.setEstado("Corregido");
        issueRepository.save(issue);

        // CAMBIAR EL ESTADO DEL REPORTE A "Aprobado"
        Reporte reporte = issue.getReporte();
        if (reporte != null) {
            reporte.setEstado("Aprobado");
            reporteRepository.save(reporte);
        }

        // NOTIFICACIONES DIFERENCIADAS POR ROL
        Equipo equipoApi = issue.getReporte().getApi().getEquipo();
        if (equipoApi != null && equipoApi.getUsuarios() != null) {
            for (Usuario miembro : equipoApi.getUsuarios()) {
                // No notificar al que cerró el issue
                if (!miembro.getDni().equals(usuario.getDni())) {
                    String rolMiembro = miembro.getRol().getNombreRol();
                    
                    // Notificación para QAs y DEVs
                    if ("DEV".equals(rolMiembro) || "QA".equals(rolMiembro)) {
                        Notificacion notif = new Notificacion();
                        notif.setMensaje("El issue de la API '" + 
                            issue.getReporte().getApi().getNombre() + "' ha sido corregido");
                        notif.setLeido(false);
                        notif.setFecha(new Timestamp(System.currentTimeMillis()));
                        notif.setUsuario(miembro);
                        notificacionRepository.save(notif);
                    }
                    
                    // Notificación especial para POs
                    if ("PO".equals(rolMiembro)) {
                        Notificacion notifPO = new Notificacion();
                        notifPO.setMensaje("Su API '" + issue.getReporte().getApi().getNombre() + 
                            "' ya ha sido arreglada y puede ser usada para su proyecto.");
                        notifPO.setLeido(false);
                        notifPO.setFecha(new Timestamp(System.currentTimeMillis()));
                        notifPO.setUsuario(miembro);
                        notificacionRepository.save(notifPO);
                    }
                }
            }
        }

        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Issue corregido");
        actividad.setDescripcion("Has cerrado el issue de " + issue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        redirectAttributes.addFlashAttribute("success", "Issue cerrado correctamente. El reporte ha sido aprobado.");
        return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
    }
}