package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import com.example.telitodev.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SUPERADMIN')")
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
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        // Limpiar parámetros vacíos: convertir cadenas vacías y listas vacías en null
        if (estados != null && estados.isEmpty()) estados = null;
        if (nombreApi != null && nombreApi.trim().isEmpty()) nombreApi = null;
        if (fechaInicio != null && fechaInicio.trim().isEmpty()) fechaInicio = null;
        if (fechaFin != null && fechaFin.trim().isEmpty()) fechaFin = null;

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

        // Si la página solicitada está fuera de rango, ajustar a la última página válida
        if (page >= reportePage.getTotalPages() && reportePage.getTotalPages() > 0) {
            page = reportePage.getTotalPages() - 1;
            pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            reportePage = reporteRepository.findByFiltersPaged(estados, inicio, fin, nombreApi, pageable);
        }

        // Pasar el objeto Page completo a la vista
        model.addAttribute("reportePage", reportePage);
        model.addAttribute("currentPage", reportePage.getNumber());
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
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

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
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        List<ApiProyectoDTO> apisParaValidar = apiRepository.findApisToReportForQa(usuario.getDni());
        model.addAttribute("apisParaValidar", apisParaValidar);

        // Lista de estados para el combobox
        List<String> estados = List.of("Aprobado", "Fallido");
        model.addAttribute("estadosReporte", estados);

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        return "qa/reporteRealizar";
    }

    @PostMapping("/guardarReporte")
    @ResponseBody
    public ResponseEntity<?> submitReporte(@RequestParam("apiId") Integer apiId,
                                @RequestParam("estado") String estado,
                                @RequestParam("descripcion") String descripcion,
                                @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                                Authentication auth) {

        Map<String, String> response = new HashMap<>();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // --- VALIDACIONES DEL BACKEND ---

        // 1. Validación de Descripción
        if (descripcion == null || descripcion.trim().isEmpty()) {
            response.put("error", "La descripción no puede estar vacía.");
            return ResponseEntity.badRequest().body(response);
        }
        if (descripcion.length() > 200) {
            response.put("error", "La descripción no puede superar los 200 caracteres.");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Validación de Estado
        if (estado == null || (!estado.equals("Aprobado") && !estado.equals("Fallido"))) {
            response.put("error", "Por favor, seleccione un estado válido.");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Validar que la API existe
        Optional<Api> apiOptional = apiRepository.findById(apiId);
        if (!apiOptional.isPresent()) {
            response.put("error", "La API seleccionada no existe.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Api apiReportada = apiOptional.get();

        // 4. Filtrar archivos vacíos
        MultipartFile[] nonEmptyFiles = null;
        if (archivos != null) {
            nonEmptyFiles = Arrays.stream(archivos)
                    .filter(f -> !f.isEmpty())
                    .toArray(MultipartFile[]::new);
        }

        // 5. Validación: Si el estado es "Fallido", debe subir al menos una evidencia
        if ("Fallido".equals(estado) && (nonEmptyFiles == null || nonEmptyFiles.length == 0)) {
            response.put("error", "Cuando el estado es 'Fallido', debe adjuntar al menos una evidencia del error.");
            return ResponseEntity.badRequest().body(response);
        }

        // 6. Validación de cantidad de archivos
        if (nonEmptyFiles != null && nonEmptyFiles.length > 5) {
            response.put("error", "No puede subir más de 5 archivos.");
            return ResponseEntity.badRequest().body(response);
        }

        // 7. Validación de tamaño y tipo de archivos
        if (nonEmptyFiles != null) {
            for (MultipartFile archivo : nonEmptyFiles) {
                if (archivo.getSize() > MAX_FILE_SIZE) {
                    response.put("error", "El archivo '" + archivo.getOriginalFilename() + "' supera el tamaño máximo de 5MB.");
                    return ResponseEntity.badRequest().body(response);
                }
                String contentType = archivo.getContentType();
                if (contentType == null || (!contentType.equals("image/png") && 
                    !contentType.equals("image/jpeg") && !contentType.equals("text/plain"))) {
                    response.put("error", "Formato de archivo no permitido. Solo se aceptan: .png, .jpg, .log, .txt.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
        }

        // --- CREAR Y GUARDAR EL REPORTE ---
        try {
            Reporte reporte = new Reporte();
            reporte.setApi(apiReportada);
            reporte.setEstado(estado);
            reporte.setDescripcion(descripcion);
            reporte.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            reporteRepository.save(reporte);

            // Guardar evidencias
            if (nonEmptyFiles != null) {
                for (MultipartFile archivo : nonEmptyFiles) {
                    Evidencia evidencia = new Evidencia();
                    evidencia.setNombre(archivo.getOriginalFilename());
                    evidencia.setEvidencia(archivo.getBytes());
                    evidencia.setDescripcion("Evidencia del reporte");
                    evidencia.setReporte(reporte);
                    evidenciaRepository.save(evidencia);
                }
            }

            // NOTIFICAR A TODO EL EQUIPO DE LA API
            Equipo equipoApi = apiReportada.getEquipo();
            if (equipoApi != null && equipoApi.getUsuarios() != null) {
                for (Usuario miembro : equipoApi.getUsuarios()) {
                    if (!miembro.getDni().equals(usuario.getDni())) {
                        Notificacion notif = new Notificacion();
                        String mensaje = "El QA " + usuario.getNombre() + " ha creado un reporte " + 
                                       ("Aprobado".equals(estado) ? "de aprobación" : "de fallo") + 
                                       " para la API: " + apiReportada.getNombre();
                        notif.setMensaje(mensaje);
                        notif.setLeido(false);
                        notif.setFecha(new Timestamp(System.currentTimeMillis()));
                        notif.setUsuario(miembro);
                        notificacionRepository.save(notif);
                    }
                }
            }

            ActividadReciente actividad = new ActividadReciente();
            actividad.setTitulo("Nuevo Reporte");
            actividad.setDescripcion("Has creado un reporte para la API " + reporte.getApi().getNombre());
            actividad.setUsuario(usuario);
            actividadRecienteRepository.save(actividad);

            response.put("success", "Reporte creado exitosamente");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Error al procesar los archivos. Por favor, inténtalo nuevamente.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error al crear el reporte. Por favor, inténtalo nuevamente.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}