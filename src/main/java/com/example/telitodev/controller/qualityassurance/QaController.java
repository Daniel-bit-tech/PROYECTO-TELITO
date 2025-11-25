package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.telitodev.dto.TestCase;


@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'DEV', 'SUPERADMIN')")
public class QaController extends BaseController {


    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final NotificacionRepository notificacionRepository;
    final TicketRepository ticketRepository;
    final IssueRepository issueRepository;
    final FeedbackRepository feedbackRepository;
    private final ApiRepository apiRepository;
    private final ActividadRecienteRepository actividadRecienteRepository;



    public QaController(UsuarioRepository usuarioRepository, CredencialApiRepository credencialApiRepository, NotificacionRepository notificacionRepository, TicketRepository ticketRepository, IssueRepository issueRepository, ApiRepository apiRepository, FeedbackRepository feedbackRepository, ActividadRecienteRepository actividadRecienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.notificacionRepository = notificacionRepository;
        this.ticketRepository = ticketRepository;
        this.issueRepository = issueRepository;
        this.feedbackRepository = feedbackRepository;
        this.apiRepository = apiRepository;
        this.actividadRecienteRepository = actividadRecienteRepository;
    }

    @GetMapping("/home")
    public String showQaView(Model model, Authentication auth, HttpSession session) {
        // Validar acceso al rol QA
        if (!validateRoleAccess(auth, session, "QA")) {
            String userRole = getUserRoleFromAuthentication(auth);
            return getRedirectUrlForRole(userRole);
        }

        Usuario usuario = getCurrentUser(auth, session);
        Integer NCredenciales = credencialApiRepository.countByUsuario_DniAndEstado(usuario.getDni(),true);
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_Dni(usuario.getDni());
        List<Notificacion> notis = notificacionRepository.findByUsuario_Dni(usuario.getDni());
        Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        // Contar issues no corregidos
        Integer NissuesNoCorregidos = issueRepository.countByEstadoNot("Corregido");
        model.addAttribute("NissuesNoCorregidos", NissuesNoCorregidos);

        // Contar las notificaciones no leídas para el usuario
        Integer NnotificacionesSinLeer = notificacionRepository.countByUsuarioAndLeido(usuario, false);
        model.addAttribute("NnotificacionesSinLeer", NnotificacionesSinLeer);

        // Obtener el QA en sesión (con soporte de impersonación)
        Usuario qaSesion = getCurrentUser(auth, session);

        List<Issue> ultimos3Issues = issueRepository.findTop5ByCreadorOrderByFechaCreacionDesc(qaSesion);


// Pasar la lista al modelo
        model.addAttribute("ultimos3Issues", ultimos3Issues);

        for (Issue issue : ultimos3Issues) {
            if (issue.getFechaCreacion() != null) {
                LocalDateTime fechaCreacionLocal = issue.getFechaCreacion().toLocalDateTime();
                Duration duration = Duration.between(fechaCreacionLocal, LocalDateTime.now());

                long days = duration.toDays();
                long hours = duration.toHours();
                long minutes = duration.toMinutes();

                String tiempoTranscurrido = "";
                if (days > 365) {
                    long years = days / 365;
                    tiempoTranscurrido = years + " años";
                } else if (days > 30) {
                    long months = days / 30;
                    tiempoTranscurrido = months + " meses";
                } else if (days > 0) {
                    tiempoTranscurrido = days + " días";
                } else if (hours > 0) {
                    tiempoTranscurrido = hours + " horas";
                } else if (minutes > 0) {
                    tiempoTranscurrido = minutes + " minutos";
                } else {
                    tiempoTranscurrido = "Hace poco";
                }

                // Asignamos el tiempo transcurrido a la propiedad
                issue.setTiempoTranscurrido(tiempoTranscurrido);
            } else {
                issue.setTiempoTranscurrido("Fecha desconocida");
            }
        }

        List<ActividadReciente> actividadesRecientes = actividadRecienteRepository.findTop5ByUsuarioOrderByFechaDesc(usuario);

        // Calcular el tiempo transcurrido para cada actividad reciente
        for (ActividadReciente actividad : actividadesRecientes) {
            if (actividad.getFecha() != null) {
                LocalDateTime fechaActividadLocal = actividad.getFecha();
                Duration duration = Duration.between(fechaActividadLocal, LocalDateTime.now());

                long days = duration.toDays();
                long hours = duration.toHours();
                long minutes = duration.toMinutes();

                String tiempoTranscurrido = "";
                if (days > 365) {
                    long years = days / 365;
                    tiempoTranscurrido = years + " años";
                } else if (days > 30) {
                    long months = days / 30;
                    tiempoTranscurrido = months + " meses";
                } else if (days > 0) {
                    tiempoTranscurrido = days + " días";
                } else if (hours > 0) {
                    tiempoTranscurrido = hours + " horas";
                } else if (minutes > 0) {
                    tiempoTranscurrido = minutes + " minutos";
                } else {
                    tiempoTranscurrido = "Hace poco";
                }

                // Asignamos el tiempo transcurrido a la propiedad de la actividad
                actividad.setTiempoTranscurrido(tiempoTranscurrido);
            } else {
                actividad.setTiempoTranscurrido("Fecha desconocida");
            }
        }

        // --- INICIO: Nueva Lógica para Contar APIs ---
        // Llama al nuevo método del repositorio para obtener el conteo de APIs a validar
        Integer apisPorValidar = apiRepository.countApisForQaValidation(usuario.getDni());
        model.addAttribute("apisPorValidar", apisPorValidar);


        model.addAttribute("usuario", usuario);
        model.addAttribute("NcredActivas", NCredenciales);
        model.addAttribute("credenciales", credenciales);
        model.addAttribute("Nnotis", Nnotis);
        model.addAttribute("notificaciones", notis);
        model.addAttribute("actividadesRecientes", actividadesRecientes);
        return "qa/quality";
    }

    @GetMapping("/perfilQa")
    public String showPerfil (Model model, Authentication auth, HttpSession session) {
        // Validar acceso al rol QA
        if (!validateRoleAccess(auth, session, "QA")) {
            String userRole = getUserRoleFromAuthentication(auth);
            return getRedirectUrlForRole(userRole);
        }
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        
        model.addAttribute("usuario", usuario);
        return "qa/perfilQa";
    }

    @GetMapping("/apiDetalle")
    public String showRoadmapView(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/apiDetalle";
    }

    @GetMapping("/soporte")
    public String showSoporte(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);
        
        model.addAttribute("usuario", usuario);
        return "qa/soporte";
    }

    /*
    @GetMapping("/issueRealizar")
    public String madeIssue(Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "qa/issueRealizar";
    }
    */

    @GetMapping("/sandbox")
    public String showQaSandbox(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        try {
            List<Api> todasLasApis = apiRepository.findAll();
            List<ApiQADTO> apisDisponibles = todasLasApis.stream()
                    .map(this::convertToApiQADTO)
                    .collect(Collectors.toList());

            model.addAttribute("usuario", usuario);
            model.addAttribute("apisDisponibles", apisDisponibles);
            model.addAttribute("pageTitle", "QA Sandbox - Testing Automatizado");

            return "qa/sandbox_qa";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar las APIs: " + e.getMessage());
            return "qa/sandbox_qa";
        }
    }

    /**
     * Convertir entidad Api a ApiQADTO
     */
    private ApiQADTO convertToApiQADTO(Api api) {
        ApiQADTO dto = new ApiQADTO(api.getIdApi(), api.getNombre(), api.getDescripcion());

        dto.setSpec(generateBasicOpenAPISpec(api));
        dto.setEntornos(generateQAEnvironments());

        return dto;
    }

    /**
     * Endpoint API para obtener detalles de API para QA
     */
    @GetMapping("/api/sandbox/api-details/{apiId}")
    @ResponseBody
    public ResponseEntity<?> getApiDetailsForQA(@PathVariable Integer apiId, Authentication auth) {
        try {
            Optional<Api> apiOpt = apiRepository.findById(apiId);
            if (apiOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "API no encontrada"));
            }

            Api api = apiOpt.get();
            ApiDetailsQADTO apiDetails = convertToApiDetailsQADTO(api);

            return ResponseEntity.ok(apiDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar detalles de la API: " + e.getMessage()));
        }
    }

    /**
     * Convertir entidad Api a ApiDetailsQADTO
     */
    private ApiDetailsQADTO convertToApiDetailsQADTO(Api api) {
        ApiDetailsQADTO dto = new ApiDetailsQADTO();
        dto.setId(api.getIdApi());
        dto.setNombre(api.getNombre());
        dto.setDescripcion(api.getDescripcion());
        dto.setVersion("1.0.0");


        dto.setSpec(generateBasicOpenAPISpec(api));

        dto.setEntornos(generateQAEnvironments());

        return dto;
    }

    /**
     * Generar spec OpenAPI básico basado en la información de la API
     */
    private Map<String, Object> generateBasicOpenAPISpec(Api api) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("openapi", "3.0.0");


        Map<String, Object> info = new HashMap<>();
        info.put("title", api.getNombre());
        info.put("description", api.getDescripcion() != null ? api.getDescripcion() : "");
        info.put("version", "1.0.0");
        spec.put("info", info);


        List<Map<String, String>> servers = new ArrayList<>();
        servers.add(createServer("QA Environment", "https://qa-api.telito.com/v1"));
        servers.add(createServer("Staging Environment", "https://staging-api.telito.com/v1"));
        spec.put("servers", servers);


        Map<String, Object> paths = new HashMap<>();


        paths.put("/health", createHealthCheckEndpoint());

        String basePath = "/" + api.getNombre().toLowerCase().replace(" ", "-");
        paths.put(basePath, createExampleEndpoint(api.getNombre()));

        spec.put("paths", paths);

        Map<String, Object> components = new HashMap<>();
        components.put("schemas", createExampleSchemas());
        spec.put("components", components);

        return spec;
    }

    private Map<String, String> createServer(String description, String url) {
        Map<String, String> server = new HashMap<>();
        server.put("url", url);
        server.put("description", description);
        return server;
    }

    private Map<String, Object> createHealthCheckEndpoint() {
        Map<String, Object> endpoint = new HashMap<>();

        Map<String, Object> get = new HashMap<>();
        get.put("summary", "Health Check");
        get.put("description", "Verificar el estado del servicio");
        get.put("responses", createHealthCheckResponses());

        endpoint.put("get", get);
        return endpoint;
    }

    private Map<String, Object> createHealthCheckResponses() {
        Map<String, Object> responses = new HashMap<>();

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Servicio funcionando correctamente");
        responses.put("200", successResponse);

        return responses;
    }

    private Map<String, Object> createExampleEndpoint(String apiName) {
        Map<String, Object> endpoint = new HashMap<>();

        Map<String, Object> get = new HashMap<>();
        get.put("summary", "Obtener " + apiName + " data");
        get.put("description", "Endpoint de ejemplo para " + apiName);
        get.put("responses", createExampleResponses(apiName));

        endpoint.put("get", get);
        return endpoint;
    }

    private Map<String, Object> createExampleResponses(String apiName) {
        Map<String, Object> responses = new HashMap<>();

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("description", "Datos de " + apiName + " obtenidos exitosamente");

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> applicationJson = new HashMap<>();
        Map<String, Object> schema = new HashMap<>();
        schema.put("$ref", "#/components/schemas/ApiResponse");
        applicationJson.put("schema", schema);
        content.put("application/json", applicationJson);

        successResponse.put("content", content);
        responses.put("200", successResponse);

        return responses;
    }

    private Map<String, Object> createExampleSchemas() {
        Map<String, Object> schemas = new HashMap<>();

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("type", "object");
        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> status = new HashMap<>();
        status.put("type", "string");
        status.put("example", "success");
        properties.put("status", status);

        Map<String, Object> data = new HashMap<>();
        data.put("type", "object");
        properties.put("data", data);

        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("type", "string");
        timestamp.put("format", "date-time");
        properties.put("timestamp", timestamp);

        apiResponse.put("properties", properties);
        schemas.put("ApiResponse", apiResponse);

        return schemas;
    }

    /**
     * Generar entornos de QA
     */
    private List<EnvironmentQADTO> generateQAEnvironments() {
        List<EnvironmentQADTO> entornos = new ArrayList<>();
        entornos.add(new EnvironmentQADTO("QA - Testing", "https://qa-api.telito.com/v1", "calidad"));
        entornos.add(new EnvironmentQADTO("Staging - Preproducción", "https://staging-api.telito.com/v1", "preproduccion"));
        entornos.add(new EnvironmentQADTO("Preprod - Validación", "https://preprod-api.telito.com/v1", "validacion"));
        return entornos;
    }

    /**
     * Endpoint para generar tokens QA temporales
     */
    @PostMapping("/api/sandbox/generate-token")
    @ResponseBody
    public ResponseEntity<QATokenResponseDTO> generateQAToken(@RequestBody QATokenRequestDTO tokenRequest,
                                                              Authentication auth, HttpSession session) {
        try {
            Usuario usuario = getCurrentUser(auth, session);


            String token = "qa_temp_" + System.currentTimeMillis() + "_" + usuario.getDni();


            long durationHours = tokenRequest.getDuration() != null ?
                    Long.parseLong(tokenRequest.getDuration()) : 1;
            long expiresAt = System.currentTimeMillis() + (durationHours * 3600 * 1000);

            QATokenResponseDTO response = new QATokenResponseDTO();
            response.setSuccess(true);
            response.setToken(token);
            response.setExpiresAt(expiresAt);
            response.setLimit(tokenRequest.getLimit() != null ? tokenRequest.getLimit() : "1000");
            response.setEnvironment(tokenRequest.getEnvironment() != null ?
                    tokenRequest.getEnvironment() : "qa");

            session.setAttribute("qaActiveToken", token);
            session.setAttribute("qaTokenExpiresAt", expiresAt);
            session.setAttribute("qaTokenLimit", response.getLimit());
            session.setAttribute("qaTokenEnvironment", response.getEnvironment());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            QATokenResponseDTO errorResponse = new QATokenResponseDTO();
            errorResponse.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para ejecutar pruebas QA
     */
    @PostMapping("/api/sandbox/execute-test")
    @ResponseBody
    public ResponseEntity<?> executeQATest(@RequestBody Map<String, Object> testRequest,
                                           Authentication auth) {
        try {
            String testId = (String) testRequest.get("testId");
            String environment = (String) testRequest.get("environment");
            String token = (String) testRequest.get("token");


            Map<String, Object> result = new HashMap<>();
            result.put("success", Math.random() > 0.3);
            result.put("testId", testId);
            result.put("duration", (int) (Math.random() * 1000) + 100);
            result.put("timestamp", System.currentTimeMillis());

            if (!result.get("success").equals(true)) {
                result.put("error", "Timeout en la respuesta del servidor");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error ejecutando prueba: " + e.getMessage()));
        }
    }



    @GetMapping("/api/sandbox/available-apis")
    @ResponseBody
    public List<ApiQADTO> getAvailableAPIs() {
        try {
            List<Api> todasLasApis = apiRepository.findAll();
            return todasLasApis.stream()
                    .map(this::convertToApiQADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }




}