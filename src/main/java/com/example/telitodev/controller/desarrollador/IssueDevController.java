package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
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
@PreAuthorize("hasAnyRole('DEV', 'SADMIN')")
public class IssueDevController extends BaseController{

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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @GetMapping("/issuesDev")
    public String showIssueDevView(Model model,
                                Authentication auth,
                                HttpSession session,
                                @RequestParam(value = "tags", required = false) List<String> estados,
                                @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                @RequestParam(value = "nombre", required = false) String nombre,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) { // 6 issues por página

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        Timestamp inicio = null;
        Timestamp fin = null;
        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) inicio = Timestamp.valueOf(fechaInicio + " 00:00:00");
            if (fechaFin != null && !fechaFin.isEmpty()) fin = Timestamp.valueOf(fechaFin + " 23:59:59");
        } catch (Exception e) { e.printStackTrace(); }

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Issue> issuesPage = issueRepository.findByFiltersForDev(estados, inicio, fin, nombre, usuario.getDni(), pageable);

        // 🔹 Si el usuario pide una página mayor al total, regresar a la última válida
        if (page >= issuesPage.getTotalPages() && issuesPage.getTotalPages() > 0) {
            pageable = PageRequest.of(issuesPage.getTotalPages() - 1, size);
            issuesPage = issueRepository.findByFiltersForDev(estados, inicio, fin, nombre, usuario.getDni(), pageable);
            page = issuesPage.getTotalPages() - 1;
        }

        model.addAttribute("issues", issuesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuesPage.getTotalPages());
        model.addAttribute("selectedEstados", estados);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("nombre", nombre);
        model.addAttribute("pageSize", size);

        return "desarrollador/issuesDev";
    }

    @GetMapping("/issueDetalleDev/{idIssue}/{idReporte}")
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
        return "desarrollador/issueDevDetalle";  // Vista para mostrar los detalles del Issue
    }

    @PostMapping("/crearComentarioDev")
    public String guardarComentario(@RequestParam("comentario") String comentario,
                                    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                                    @RequestParam("idIssue") Integer idIssue,
                                    @RequestParam("idReporte") Integer idReporte,
                                    Authentication auth, RedirectAttributes redirectAttributes) {

        // Obtener el usuario
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Buscar el Issue y Reporte por sus ID
        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            redirectAttributes.addFlashAttribute("error", "Issue no encontrado");
            return "redirect:/issuesDev";
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
            return "redirect:/issueDetalleDev/" + idIssue + "/" + idReporte;
        }

        for (MultipartFile archivo : archivos) {
            if (archivo.getSize() > MAX_FILE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "El archivo es demasiado grande");
                return "redirect:/issueDetalleDev/" + idIssue + "/" + idReporte;
            }

            // Validar tipo de archivo (solo imágenes y logs)
            String contentType = archivo.getContentType();
            if (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("text/plain")) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten archivos de tipo .png, .jpg o .log");
                return "redirect:/issueDetalleDev/" + idIssue + "/" + idReporte;
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
                return "redirect:/issueDetalleDev/" + idIssue + "/" + idReporte;
            }
        }

        // Guardar el comentario
        comentarioRepository.save(newComentario);

        // 🔹 Cambiar estado del Issue si el usuario es desarrollador
        if (usuario.getRol().getNombreRol().equals("DEV")) {
            issue.setEstado("En progreso");
            issueRepository.save(issue);
        }

        // Redirigir de nuevo al detalle del Issue
        return "redirect:/issueDetalleDev/" + idIssue + "/" + idReporte;
    }
}
