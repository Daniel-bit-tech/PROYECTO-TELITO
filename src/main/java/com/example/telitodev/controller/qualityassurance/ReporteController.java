package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import com.example.telitodev.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class ReporteController extends BaseController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ActividadRecienteRepository actividadRecienteRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @GetMapping("/reportes")
    public String showReportesView(Model model,
                                   Authentication auth,
                                   HttpSession session,
                                   @RequestParam(value = "estados", required = false) List<String> estados,
                                   @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                   @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                   @RequestParam(value = "nombreApi", required = false) String nombreApi,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {

        // Obtener usuario autenticado y datos de impersonación
        Usuario usuario = getCurrentUser(auth, session);
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        // Convertir fechas a Timestamp
        Timestamp inicio = null;
        Timestamp fin = null;
        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = Timestamp.valueOf(fechaInicio + " 00:00:00");
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = Timestamp.valueOf(fechaFin + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar paginación (orden descendente por fecha de creación)
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Reporte> reportePage = reporteRepository.findByFiltersPaged(estados, inicio, fin, nombreApi, pageable);

        // Evitar error si la página solicitada excede el total
        if (page >= reportePage.getTotalPages() && reportePage.getTotalPages() > 0) {
            int lastPage = reportePage.getTotalPages() - 1;
            pageable = PageRequest.of(lastPage, size, Sort.by("fechaCreacion").descending());
            reportePage = reporteRepository.findByFiltersPaged(estados, inicio, fin, nombreApi, pageable);
            page = lastPage;
        }

        // Pasar el objeto Page completo a la vista
        model.addAttribute("reportePage", reportePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportePage.getTotalPages());

        // Devolver los parámetros de filtro a la vista para mantener su estado
        model.addAttribute("selectedEstados", estados);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("nombreApi", nombreApi);

        return "qa/reportes";
    }



    @GetMapping("/reporteDetalle")
    public String showReporteDetalleView(Model model, Authentication auth, HttpSession session,
                                         @RequestParam("idReporte") Integer idReporte) {
        Usuario usuario = getCurrentUser(auth, session);

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        Reporte reporte = reporteRepository.findById(idReporte).orElse(null);

        if (reporte == null) {
            return "redirect:/qa/reportes?error=Reporte no encontrado";
        }

        // Obtener las evidencias asociadas al reporte
        List<Evidencia> evidencias = evidenciaRepository.findByReporte(reporte);


        // Agregar el reporte al modelo
        model.addAttribute("reporte", reporte);
        model.addAttribute("evidencias", evidencias);

        return "qa/reporteDetalle";
    }

    @GetMapping("/crearReporte")
    public String madeReport(Model model, Authentication auth, HttpSession session){
        Usuario usuario = getCurrentUser(auth, session);

        List<ApiProyectoDTO> apisParaValidar = apiRepository.findApisToReportForQa(usuario.getDni());
        model.addAttribute("apis", apisParaValidar);

        System.out.println("Lonigut es: "+apisParaValidar.size());

        // Lista de estados para el combobox
        List<String> estados = List.of("Aprobado", "Fallido");
        model.addAttribute("estadosReporte", estados);

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        return "qa/reporteRealizar";
    }

    @PostMapping("/guardarReporte")
    public String submitReporte(@RequestParam("apiId") Integer apiId,
                                    @RequestParam("estado") String estado,
                                    @RequestParam("descripcion") String descripcion,
                                    @RequestParam("poLiderDni") String poLiderDni, // <-- ¡Aquí está!
                                    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                                    Model model,
                                    Authentication auth,
                                    HttpSession session) {

            Usuario usuario = getCurrentUser(auth, session);

        boolean hasErrors = false;

        // --- INICIO DE VALIDACIONES DEL BACKEND ---

        // 1. Validación de Descripción
        if (descripcion == null || descripcion.trim().isEmpty()) {
            model.addAttribute("errorDescripcion", "La descripción no puede estar vacía.");
            hasErrors = true;
        } else if (descripcion.length() > 200) {
            model.addAttribute("errorDescripcion", "La descripción no puede superar los 200 caracteres.");
            hasErrors = true;
        }

        // 2. Validación de Estado
        if (estado == null || (!estado.equals("Aprobado") && !estado.equals("Fallido"))) {
            model.addAttribute("errorEstado", "Por favor, seleccione un estado válido.");
            hasErrors = true;
        }

        // 3. Validaciones de Archivos
        if (archivos != null) {
            MultipartFile[] nonEmptyFiles = Arrays.stream(archivos).filter(f -> !f.isEmpty()).toArray(MultipartFile[]::new);

            if (nonEmptyFiles.length > 5) {
                model.addAttribute("errorArchivos", "No puede subir más de 5 archivos.");
                hasErrors = true;
            } else {
                for (MultipartFile archivo : nonEmptyFiles) {
                    if (archivo.getSize() > MAX_FILE_SIZE) {
                        model.addAttribute("errorArchivos", "El archivo '" + archivo.getOriginalFilename() + "' supera el tamaño máximo de 5MB.");
                        hasErrors = true;
                        break;
                    }
                    String contentType = archivo.getContentType();
                    if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("text/plain"))) {
                        model.addAttribute("errorArchivos", "Formato de archivo no permitido. Solo se aceptan: .png, .jpg, .log, .txt.");
                        hasErrors = true;
                        break;
                    }
                }
            }
        }

        // --- FIN DE VALIDACIONES ---

        // Si se encontró algún error, recargamos la vista del formulario con los mensajes
        if (hasErrors) {
            List<ApiProyectoDTO> apisParaValidar = apiRepository.findApisToReportForQa(usuario.getDni());
            model.addAttribute("apisParaValidar", apisParaValidar);
            model.addAttribute("estadosReporte", List.of("Aprobado", "Fallido"));
            addImpersonationAttributes(model, session);
            model.addAttribute("usuario", usuario);
            return "qa/reporteRealizar";
        }

        // Crear y guardar el reporte
        Reporte reporte = new Reporte();
        Api apiReportada = apiRepository.findById(apiId).orElseThrow(() -> new RuntimeException("API no encontrada"));
        reporte.setApi(apiReportada);
        reporte.setEstado(estado);
        reporte.setDescripcion(descripcion);
        reporte.setFechaCreacion(new Timestamp(System.currentTimeMillis()));

        reporteRepository.save(reporte);

        // Procesar archivos como Evidencias
        if (archivos != null && archivos.length > 0) {
            archivos = Arrays.stream(archivos)
                    .filter(file -> !file.isEmpty())
                    .toArray(MultipartFile[]::new);

            if (archivos.length > 5) {
                model.addAttribute("errorArchivos", "Máximo 5 archivos permitidos.");
                return "qa/reporteRealizar";
            }

            for (MultipartFile archivo : archivos) {
                if (archivo.getSize() > MAX_FILE_SIZE) {
                    model.addAttribute("error", "El archivo es demasiado grande");
                    return "qa/reporteRealizar";
                }

                String contentType = archivo.getContentType();
                if (!contentType.equals("image/png") &&
                        !contentType.equals("image/jpeg") &&
                        !contentType.equals("text/plain")) {
                    model.addAttribute("errorArchivos", "Solo se permiten archivos .png, .jpg, .log");
                    return "qa/reporteRealizar";
                }

                try {
                    Evidencia evidencia = new Evidencia();
                    evidencia.setNombre(archivo.getOriginalFilename());
                    evidencia.setEvidencia(archivo.getBytes());
                    evidencia.setDescripcion("Adjunto del reporte"); // Puedes hacer un input para descripción individual
                    evidencia.setReporte(reporte);

                    evidenciaRepository.save(evidencia);

                } catch (IOException e) {
                    e.printStackTrace();
                    model.addAttribute("errorArchivos", "Error al guardar el archivo: " + archivo.getOriginalFilename());
                    return "qa/reporteRealizar";
                }
            }
        }

        // Crear la notificación
        Notificacion notif = new Notificacion();
        notif.setMensaje("Se ha creado un nuevo reporte para la API: " + reporte.getApi().getNombre());
        notif.setLeido(false);
        notif.setFecha(new Timestamp(System.currentTimeMillis()));
        notif.setUsuario(usuario); // propietario de la API
        notificacionRepository.save(notif);

        // Si el reporte fue APROBADO, notificar al PO Líder.
        if ("Aprobado".equals(estado) && poLiderDni != null && !poLiderDni.isEmpty()) {

            // Buscamos al PO Líder por su DNI
            Optional<Usuario> poLiderOpt = usuarioRepository.findById(poLiderDni);

            if (poLiderOpt.isPresent()) {
                Usuario poLider = poLiderOpt.get();

                // Creamos la notificación para el PO Líder
                Notificacion notificacionParaPO = new Notificacion();
                String mensaje = "El QA " + usuario.getNombre() + " ha validado y aprobado la API: '" + apiReportada.getNombre() + "'.";
                notificacionParaPO.setMensaje(mensaje);
                notificacionParaPO.setLeido(false);
                notificacionParaPO.setFecha(new Timestamp(System.currentTimeMillis()));
                notificacionParaPO.setUsuario(poLider); // Asignamos al PO Líder como receptor

                notificacionRepository.save(notificacionParaPO);
            }
        }

        ActividadReciente actividad = new ActividadReciente();
        actividad.setTitulo("Nuevo Reporte");
        actividad.setDescripcion("Has creado un reporte para la api " + reporte.getApi().getNombre());
        actividad.setUsuario(usuario);
        actividadRecienteRepository.save(actividad);

        return "redirect:/qa/reportes";
    }

}