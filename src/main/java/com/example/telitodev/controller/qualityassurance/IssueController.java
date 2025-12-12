package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
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
import java.util.List;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
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

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Issue> issuePage = issueRepository.findByFilters(estados, inicio, fin, nombre, pageable);

        // Si la página solicitada está fuera de rango, redirigir a la última página válida.
        if (page >= issuePage.getTotalPages() && issuePage.getTotalPages() > 0) {
            int lastPage = issuePage.getTotalPages() - 1;
            pageable = PageRequest.of(lastPage, size, Sort.by("fechaCreacion").descending());
            issuePage = issueRepository.findByFilters(estados, inicio, fin, nombre, pageable);
            page = lastPage;
        }

        // Pasamos el objeto Page completo a la vista para mayor consistencia
        model.addAttribute("issuePage", issuePage);
        model.addAttribute("currentPage", page);
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

        // --- INICIO DE VALIDACIÓN ---
        if (descripcion == null || descripcion.trim().isEmpty()) {
            // Si hay error, usamos RedirectAttributes para enviar el error a la página anterior
            redirectAttributes.addFlashAttribute("errorDescripcion", "La descripción no puede estar vacía.");
            // Redirigimos de vuelta a la página del detalle del reporte desde donde se crea el issue
            return "redirect:/qa/reporteDetalle?idReporte=" + idReporte;
        }

        if (descripcion.length() > 200) {
            redirectAttributes.addFlashAttribute("errorDescripcion", "La descripción no puede superar los 200 caracteres.");
            return "redirect:/qa/reporteDetalle?idReporte=" + idReporte;
        }
        // --- FIN DE VALIDACIÓN ---

        // Verificar que el reporte tiene estado "Fallido"
        if (!"Fallido".equals(reporte.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes crear un Issue para reportes en estado 'Fallido'.");
            return "redirect:/qa/reportes";
        }

        // Crear el IssueId (composite key)
        IssueId issueId = new IssueId(); // Si el idIssue es autogenerado, no es necesario pasarlo
        issueId.setIdReporte(idReporte);

        // Crear el nuevo Issue
        Issue newIssue = new Issue();
        newIssue.setId(issueId);  // Asignar el IssueId
        newIssue.setDescripcion(descripcion);
        newIssue.setEstado("Reportado");  // Establecer el estado del Issue
        newIssue.setReporte(reporte); // Asociar el Issue con el Reporte
        newIssue.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        newIssue.setCreador(usuario);
        issueRepository.save(newIssue); // Guardar el Issue

        // Crear la notificación
        Notificacion notif = new Notificacion();
        notif.setMensaje("Se ha creado un nuevo issue para tu API: " + newIssue.getReporte().getApi().getNombre());
        notif.setLeido(false);
        notif.setFecha(new Timestamp(System.currentTimeMillis()));
        notif.setUsuario(newIssue.getReporte().getApi().getUsuarioPropietario()); // propietario de la API
        notificacionRepository.save(notif);

        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Issue");
        actividad.setDescripcion("Has creado un issue para la api " + newIssue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);


        return "redirect:/qa/issues"; // Redirigir a la lista de Issues
    }

    @PostMapping("/crearComentario")
    public String guardarComentario(@RequestParam("comentario") String comentario,
                                    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                                    @RequestParam("idIssue") Integer idIssue,
                                    @RequestParam("idReporte") Integer idReporte,
                                    Authentication auth, RedirectAttributes redirectAttributes) {


        if (comentario == null || comentario.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorComentario", "El comentario no puede estar vacío.");
            // No repoblamos el comentario porque estaba vacío
            return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
        }

        // Validación 2: Comentario no puede exceder los 400 caracteres
        if (comentario.length() > 400) {
            redirectAttributes.addFlashAttribute("errorComentario", "El comentario no puede superar los 400 caracteres.");
            // Devolvemos el comentario para que el usuario pueda editarlo
            redirectAttributes.addFlashAttribute("submittedComentario", comentario);
            return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
        }
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Buscar el Issue y Reporte por sus ID
        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            redirectAttributes.addFlashAttribute("error", "Issue no encontrado");
            return "redirect:/qa/issues";
        }

        // Crear comentario
        Comentario newComentario = new Comentario();
        newComentario.setComentario(comentario);
        newComentario.setFecha(new Timestamp(System.currentTimeMillis()));
        newComentario.setIssue(issue);
        newComentario.setUsuario(usuario);

        System.out.println("------------------");
        System.out.println(archivos.length);
        System.out.println("------------------");

        if (archivos != null && archivos.length > 0) {
            archivos = Arrays.stream(archivos)
                    .filter(file -> !file.isEmpty()) // Filtramos los archivos vacíos
                    .toArray(MultipartFile[]::new);
        }

        // Validaciones de archivos
        if (archivos != null && archivos.length > 5) {
            redirectAttributes.addFlashAttribute("error", "Máximo 5 archivos permitidos");
            return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
        }

        for (MultipartFile archivo : archivos) {
            if (archivo.getSize() > MAX_FILE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "El archivo es demasiado grande");
                return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
            }

            // Validar tipo de archivo (solo imágenes y logs)
            String contentType = archivo.getContentType();
            if (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("text/plain")) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten archivos de tipo .png, .jpg o .log");
                return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
            }

            try {
                // Guardar archivo adjunto
                Adjunto adjunto = new Adjunto();
                adjunto.setNombre(archivo.getOriginalFilename());
                adjunto.setComentario(newComentario);
                adjunto.setArchivo(archivo.getBytes());
                adjuntoRepository.save(adjunto);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Error al guardar el archivo");
                return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
            }
        }

        // Guardar el comentario
        comentarioRepository.save(newComentario);

        // Después de guardar el comentario
        Usuario desarrollador = issue.getReporte().getApi().getUsuarioPropietario(); // propietario de la API

        Notificacion notif = new Notificacion();
        notif.setMensaje("El QA " + usuario.getNombre() +
                " comentó en el foro del Issue de tu API: " + issue.getReporte().getApi().getNombre());
        notif.setLeido(false);
        notif.setFecha(new Timestamp(System.currentTimeMillis()));
        notif.setUsuario(desarrollador); // receptor
        notificacionRepository.save(notif);

        // Registrar la actividad reciente
        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Comentario");
        actividad.setDescripcion("Has dejado un comentario en el issue " + issue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        // Redirigir de nuevo al detalle del Issue
        return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
    }

    @PostMapping("/issueCerrar/{idIssue}/{idReporte}")
    public String cerrarIssue(@PathVariable Integer idIssue, @PathVariable Integer idReporte,
                              RedirectAttributes redirectAttributes, Authentication auth) {


        // Obtener el usuario
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);

        if (issue == null) {
            redirectAttributes.addFlashAttribute("error", "Issue no encontrado");
            return "redirect:/issues"; // o tu listado de QA
        }

        issue.setEstado("Corregido");
        issueRepository.save(issue);

        // Opcional: enviar notificación al desarrollador
        Usuario dev = issue.getReporte().getApi().getUsuarioPropietario();
        if(dev != null){
            Notificacion notif = new Notificacion();
            notif.setMensaje("El QA cerró el Issue: " + issue.getReporte().getApi().getNombre());
            notif.setLeido(false);
            notif.setFecha(new Timestamp(System.currentTimeMillis()));
            notif.setUsuario(dev);
            notificacionRepository.save(notif);
        }

        // Registrar la actividad reciente
        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Issue corregido");
        actividad.setDescripcion("Has cerrado el issue de " + issue.getReporte().getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        redirectAttributes.addFlashAttribute("success", "Issue cerrado correctamente");
        return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
    }
}