package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.dto.SandboxRequestDto;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.SandboxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class ProxyController {

    @Value("${mock.api.dev.url}")
    private String mockDevUrl;

    @Value("${mock.api.prod.url}")
    private String mockProdUrl;

    @Autowired
    private SandboxService sandboxService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ApiHasEntornoRepository apiHasEntornoRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private CredencialApiRepository credencialApiRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogapiRepository logapiRepository;
    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private DocumentacionRepository documentacionRepository;

    // dto aqui xd
    public static class ApiHasEntornoUrlDto {
        private String urlBase;
        private String apiNombre;

        public ApiHasEntornoUrlDto(String urlBase, String apiNombre) {
            this.urlBase = urlBase;
            this.apiNombre = apiNombre;
        }

        public String getUrlBase() { return urlBase; }
        public String getApiNombre() { return apiNombre; }
    }


    private ResponseEntity<List<Map<String, Object>>> buildApiResponse(List<Api> apis) {
        List<Map<String, Object>> response = apis.stream().map(api -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idApi", api.getIdApi());
            map.put("nombre", api.getNombre());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/sandbox/execute")
    public ResponseEntity<Map<String, Object>> executeSandboxTest(@RequestBody SandboxRequestDto request,
            Authentication authentication) {
        long startTime = System.currentTimeMillis();
        Api apiTarget = null;
        Usuario usuario = null;

        try {
            String userEmail = authentication.getName();
            usuario = usuarioRepository.findByCorreo(userEmail);
            if (usuario == null)
                throw new RuntimeException("Usuario no autenticado.");
            String userDni = usuario.getDni();
            String apiKey = request.getApiKey();
            if (apiKey == null && request.getHeaders() != null) {
                // Buscar en headers comunes
                apiKey = request.getHeaders().get("apiKey");
                if (apiKey == null)
                    apiKey = request.getHeaders().get("Authorization");
            }

            String pathTemplate;
            String urlBase;

            if (request.getTargetUrl().startsWith("http")) {
                System.out.println("DEBUG SANDBOX: Ejecutando URL ABSOLUTA. Se ignora la configuración de API/Entorno.");

                urlBase = "";
                pathTemplate = request.getTargetUrl();

            } else {
                System.out.println("DEBUG SANDBOX: Ejecutando con RUTA RELATIVA. API ID: " + request.getApiId() + ", Entorno ID: " + request.getEnvironmentId());


                if (request.getApiId() == null || request.getApiId() == 0) {
                    throw new RuntimeException("Debe seleccionar una API para rutas relativas.");
                }

                apiTarget = apiRepository.findById(request.getApiId())
                        .orElseThrow(() -> new RuntimeException("API no encontrada"));

                boolean esApiPublica = (apiTarget.getDominio().getIdDominio() == 11);
                boolean esKeyMaestra = "DEV_TEST_12345".equals(apiKey);

                if (!esApiPublica && !esKeyMaestra) {
                    Optional<CredencialApi> credencialOpt = credencialApiRepository
                            .findByApiKeyAndUsuario_DniAndEstado(apiKey, userDni, true);

                    if (credencialOpt.isEmpty()) {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("error", "Acceso Prohibido");
                        errorMap.put("detalle", "Esta API requiere una credencial válida que te pertenezca.");
                        errorMap.put("statusCode", 403);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
                    }
                }

                Map<String, List<String>> pathParamsMap = getPathParamsMap(request.getApiId());
                pathTemplate = matchEndpoint(request.getTargetUrl(), pathParamsMap);

                String mockEntorno = getMockEnvironment(request.getEnvironmentId());
                String apiName = apiTarget.getNombre().replaceAll("\\s+","").toLowerCase();

                if (mockEntorno != null) {
                    final String MOCK_SERVER_URL = "http://localhost:8083/mock/";

                    urlBase = MOCK_SERVER_URL + mockEntorno + "/" + apiName;
                    System.out.println("DEBUG SANDBOX: SELECCIÓN MOCK. Entorno: " + mockEntorno + ". URL BASE MOCK: " + MOCK_SERVER_URL);


                    pathTemplate = (request.getTargetUrl().startsWith("/")) ? request.getTargetUrl() : "/" + request.getTargetUrl();

                } else {
                    System.out.println("DEBUG SANDBOX: SELECCIÓN REAL. ID Entorno: " + request.getEnvironmentId() + " (no mock)");


                    urlBase = getUrlBaseFromDb(request.getApiId());
                }
            }


            HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
            String urlToCall = urlBase.replaceAll("/+$", "") + (pathTemplate.startsWith("http") ? "" : pathTemplate);
            System.out.println("DEBUG SANDBOX: URL FINAL PARA LLAMADA: " + urlToCall);

            String bodyAsString = null;
            if (request.getBody() != null) {
                bodyAsString = objectMapper.writeValueAsString(request.getBody());
            }

            ResponseEntity<String> response = callApi(urlBase, pathTemplate, request.getTargetUrl(), method, bodyAsString, apiKey);

            Map<String, Object> bodyMap = new HashMap<>();

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                try {
                    Object jsonBody = objectMapper.readValue(response.getBody(), Object.class);
                    bodyMap.put("data", jsonBody);
                } catch (Exception e) {
                    bodyMap.put("data", response.getBody());
                }
            } else {
                bodyMap.put("data", null);
            }

            bodyMap.put("statusCode", response.getStatusCodeValue());

            // Registrar métricas solo para entorno producción (ID 1)
            if (request.getEnvironmentId() != null && request.getEnvironmentId() == 1 && apiTarget != null) {
                logMetrics(apiTarget, usuario, request.getEndpoint(), request.getTargetUrl(), request.getMethod(), response.getStatusCode().value(), startTime);
            }

            return ResponseEntity.status(response.getStatusCode()).body(bodyMap);

        } catch (Exception e) {
            System.err.println("DEBUG SANDBOX ERROR: Capturado el error: " + e.getMessage());

            e.printStackTrace();

            // Registrar métrica de error si es producción
            if (request.getEnvironmentId() != null && request.getEnvironmentId() == 1 && apiTarget != null
                    && usuario != null) {
                logMetrics(apiTarget, usuario, request.getEndpoint(), request.getTargetUrl(), request.getMethod(), 500, startTime);
            }

            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Error de ejecución");
            errorMap.put("detalle", e.getMessage());
            errorMap.put("statusCode", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }



    private String getMockEnvironment(Integer environmentId) {
        if (environmentId == null)
            return null;

        return switch (environmentId) {
            case 2 -> "dev"; // Desarrollo
            case 3 -> "qa";
            case 1 -> "prod";
            default -> null;
        };
    }


    private ResponseEntity<String> callApi(String urlBase, String pathTemplate, String urlIngresada, HttpMethod method, String body, String apiKey) {
        Map<String, String> uriVariables = extractPathVariables(urlIngresada, pathTemplate);

        String fullUrl;
        if (urlBase == null || urlBase.isEmpty()) {
            fullUrl = urlIngresada;
        } else {
            fullUrl = urlBase.replaceAll("/+$", "") + pathTemplate;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.add("X-API-Key", apiKey);
            }
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            if (uriVariables.isEmpty()) {
                return restTemplate.exchange(fullUrl, method, entity, String.class);
            } else {
                return restTemplate.exchange(fullUrl, method, entity, String.class, uriVariables);
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error de conexión\", \"detalle\": \"" + e.getMessage() + "\"}");
        }
    }

    private String getUrlBaseFromDb(Integer apiId) throws Exception {
        ApiHasEntorno entorno = apiHasEntornoRepository
                .findFirstByApi_IdApiAndEstado(apiId, ApiHasEntorno.EstadoApiEntorno.Activo)
                .orElseThrow(() -> new RuntimeException("No hay entorno activo para la API ID: " + apiId));
        if (entorno.getUrlBase() == null || entorno.getUrlBase().isEmpty()) {
            throw new RuntimeException("La URL Base para este entorno está vacía en la base de datos.");
        }
        return entorno.getUrlBase();
    }

    private Map<String, List<String>> getPathParamsMap(Integer apiId) throws Exception {
        Optional<ContratoApi> contrato = contratoRepository.findLatestByApiId(apiId);
        if (contrato.isEmpty()) throw new RuntimeException("Documentación no encontrada");

        JsonNode root = objectMapper.readTree((String) contrato.get().getContenido());
        JsonNode paths = root.path("paths");

        Map<String, List<String>> pathParamsMap = new HashMap<>();
        paths.fieldNames().forEachRemaining(path -> {
            List<String> params = new ArrayList<>();
            Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(path);
            while (matcher.find()) {
                params.add(matcher.group(1));
            }
            pathParamsMap.put(path, params);
        });
        return pathParamsMap;
    }

    private String matchEndpoint(String urlIngresada, Map<String, List<String>> pathParamsMap) {
        for (String pathTemplate : pathParamsMap.keySet()) {
            String regex = pathTemplate.replaceAll("\\{[^/]+\\}", "([^/]+)");
            if (urlIngresada.matches(regex)) {
                return pathTemplate;
            }
        }
        throw new RuntimeException("Endpoint no encontrado en documentación.");
    }

    private Map<String, String> extractPathVariables(String urlIngresada, String pathTemplate) {
        if (pathTemplate.startsWith("http")) return Collections.emptyMap();

        List<String> paramNames = Arrays.stream(pathTemplate.split("/"))
                .filter(s -> s.startsWith("{") && s.endsWith("}"))
                .map(s -> s.substring(1, s.length() - 1))
                .collect(Collectors.toList());

        String regex = pathTemplate.replaceAll("\\{[^/]+\\}", "([^/]+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(urlIngresada);

        if (!matcher.matches()) return Collections.emptyMap();

        Map<String, String> uriVariables = new HashMap<>();
        for (int i = 0; i < paramNames.size(); i++) {
            uriVariables.put(paramNames.get(i), matcher.group(i + 1));
        }
        return uriVariables;
    }


    @GetMapping("/api/sandbox/{apiId}/entorno/{entornoId}/url-base")
    public ResponseEntity<ApiHasEntornoUrlDto> getApiBaseUrlForEntorno(@PathVariable Integer apiId, @PathVariable Integer entornoId) {
        Optional<Api> optApi = apiRepository.findById(apiId);
        String apiNombre = optApi.map(Api::getNombre).orElse("API Desconocida");


        Optional<ApiHasEntorno> optConfig = apiHasEntornoRepository.findByApi_IdApiAndEntorno_IdEntorno(apiId, entornoId);

        if (optConfig.isEmpty()) {

            return ResponseEntity.status(404).body(new ApiHasEntornoUrlDto(null, apiNombre));
        }
        return ResponseEntity.ok(new ApiHasEntornoUrlDto(optConfig.get().getUrlBase(), optConfig.get().getApi().getNombre()));
    }

    /**
     * Registra métricas de una llamada al sandbox en la tabla LogApi
     */
    private void logMetrics(Api api, Usuario usuario, String endpoint, String urlTarget, String metodoHttp, Integer statusCode, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;

            LogApi log = new LogApi();
            log.setApi(api);
            log.setUsuario(usuario);
            log.setEndpoint(endpoint);
            //            log.setUrl(urlTarget);
            log.setMetodoHttp(metodoHttp.toUpperCase());
            log.setEstadoHttp(statusCode);
            log.setTiempoRespuestaMs((int) duration);
            log.setFecha(java.time.LocalDateTime.now());

            logapiRepository.save(log);

        } catch (Exception e) {
            System.err.println("Error al registrar métrica: " + e.getMessage());
        }
    }
//
//    @GetMapping("/api/sandbox/{apiId}/endpoints")
//    public ResponseEntity<List<String>> getApiEndpoints(@PathVariable Integer apiId) {
//        Optional<Documentacion> docOpt = documentacionRepository.findFirstByApi_IdApi(apiId);
//        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();
//
//        try {
//            JsonNode root = objectMapper.readTree((String) docOpt.get().getContenido());
//            JsonNode pathsNode = root.path("paths");
//
//            List<String> endpoints = new ArrayList<>();
//            if (!pathsNode.isMissingNode()) {
//                pathsNode.fieldNames().forEachRemaining(endpoints::add);
//            }
//            return ResponseEntity.ok(endpoints);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).build();
//        }
//    }
}