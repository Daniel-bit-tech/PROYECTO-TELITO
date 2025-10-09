package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @GetMapping("/issues")
    public String showIssueView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);

        List<Issue> issues = issueRepository.findAll();
        model.addAttribute("issues", issues);
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
            return "errorPage";  // Redirige a una página de error personalizada
        }

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
                             @RequestParam("estado") String estado) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
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
        issueRepository.save(newIssue); // Guardar el Issue

        return "redirect:/qa/issues"; // Redirigir a la lista de Issues
    }

    @PostMapping("/crearComentario")
    public String guardarComentario(
            @RequestParam("comentario") String comentario,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo,
            @RequestParam("idIssue") Integer idIssue,
            @RequestParam("idReporte") Integer idReporte,
            Authentication auth) {

        // Obtener el usuario actual
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Buscar el Issue y Reporte por sus ID
        IssueId issueId = new IssueId(idIssue, idReporte);
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            return "redirect:/qa/issues?error=Issue no encontrado";
        }

        // Crear un nuevo comentario
        Comentario newComentario = new Comentario();
        newComentario.setComentario(comentario);
        newComentario.setFecha(new Timestamp(System.currentTimeMillis()));
        newComentario.setIssue(issue);
        newComentario.setUsuario(usuario);

        // Si hay archivo adjunto, guardarlo
        // Si hay archivo adjunto, guardarlo
        if (!archivo.isEmpty()) {
            // Validar el tamaño del archivo (máximo 5 MB, por ejemplo)
            if (archivo.getSize() > MAX_FILE_SIZE) {
                return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte + "?error=El archivo es demasiado grande";
            }

            try {
                // Crear un nuevo adjunto
                Adjunto adjunto = new Adjunto();
                adjunto.setNombre(archivo.getOriginalFilename());
                adjunto.setComentario(newComentario);  // Asociar el adjunto con el comentario

                // Determinar si el archivo es un log o una imagen (basado en el tipo MIME)
                if ("text/plain".equals(archivo.getContentType())) {
                    // Si es un log (archivo de texto), lo almacenamos como binario en LONGBLOB
                    adjunto.setArchivo(archivo.getBytes());  // Almacenamos el archivo binario
                } else {
                    // Si es una imagen u otro archivo binario, lo almacenamos también en LONGBLOB
                    adjunto.setArchivo(archivo.getBytes());  // Almacenamos la imagen u otro archivo binario
                }

                // Guardamos el adjunto en la base de datos
                adjuntoRepository.save(adjunto);

            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte + "?error=Error al guardar el archivo";
            }
        }

        // Guardar el comentario en la base de datos
        comentarioRepository.save(newComentario);

        // Redirigir de nuevo al detalle del Issue
        return "redirect:/qa/issueDetalle/" + idIssue + "/" + idReporte;
    }




}