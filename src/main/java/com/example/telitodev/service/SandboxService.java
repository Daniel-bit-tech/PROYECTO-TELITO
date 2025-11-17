package com.example.telitodev.service;

import java.net.URI;
import com.example.telitodev.dto.SandboxApiDetailsDto;
import com.example.telitodev.dto.SandboxEnvironmentDto;
import com.example.telitodev.dto.SandboxRequestDto;
import com.example.telitodev.dto.SandboxResponseDto;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.ApiHasEntorno;
import com.example.telitodev.entity.CredencialApi;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.repository.ApiHasEntornoRepository;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.CredencialApiRepository;
import com.example.telitodev.repository.DocumentacionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SandboxService {

    @Autowired
    private ApiRepository apiRepository;
    @Autowired private DocumentacionRepository documentacionRepository;
    @Autowired private ApiHasEntornoRepository apiHasEntornoRepository;
    @Autowired private CredencialApiRepository credencialApiRepository;

    @Autowired private ObjectMapper objectMapper;



    @Value("${mock.api.dev.url}")
    private String mockDevBaseUrl;

    @Value("${mock.api.prod.url}")
    private String mockProdBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final int ID_ENTORNO_DESARROLLO = 2;


    public SandboxApiDetailsDto getApiDetails(Integer apiId, String userDni) {

        credencialApiRepository
                .findFirstByUsuario_DniAndApi_IdApiAndEstado(userDni, apiId, true)
                .orElseThrow(() -> new RuntimeException("Acceso denegado: No tienes una credencial activa para esta API."));

        Api api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API no encontrada"));

        Documentacion doc = documentacionRepository.findFirstByApi_IdApi(apiId)
                .orElseThrow(() -> new RuntimeException("Documentación no encontrada para la API con ID: " + apiId));


        List<ApiHasEntorno> entornosRel = apiHasEntornoRepository.findByApi_IdApi(apiId);
        List<SandboxEnvironmentDto> entornosDto = entornosRel.stream()
                .filter(rel -> !rel.getEntorno().getNombre().equalsIgnoreCase("Producción"))
                .filter(rel -> !rel.getEntorno().getNombre().equalsIgnoreCase("QA"))
                .map(rel -> new SandboxEnvironmentDto(rel.getEntorno().getNombre(), rel.getUrlBase()))
                .collect(Collectors.toList());

        SandboxApiDetailsDto detailsDto = new SandboxApiDetailsDto();
        detailsDto.setNombre(api.getNombre());
        detailsDto.setEntornos(entornosDto);
        try {
            Map<String, Object> spec = objectMapper.readValue(doc.getContenido(), Map.class);
            detailsDto.setSpec(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la especificación OpenAPI");
        }

        return detailsDto;
    }


    public SandboxResponseDto executeRequest(SandboxRequestDto requestDto, String userDni) {
        String fullUrl = "";
        String apiKey = requestDto.getApiKey() != null ? requestDto.getApiKey().trim() : "";
        Integer apiId = requestDto.getApiId();


        String apiBaseUrl = "";
        if (apiId != null) {
            Integer entornoBusquedaId = ID_ENTORNO_DESARROLLO;

            List<ApiHasEntorno> entornos = apiHasEntornoRepository.findByApi_IdApi(apiId);
            if (entornos.size() == 1) {
                entornoBusquedaId = entornos.get(0).getEntorno().getIdEntorno();
            } else {
                if (apiKey.toUpperCase().startsWith("PROD_")) entornoBusquedaId = 1;
                else if (apiKey.toUpperCase().startsWith("DEV_")) entornoBusquedaId = 2;
                else entornoBusquedaId = 1;
            }

            Optional<ApiHasEntorno> optConfig = apiHasEntornoRepository
                    .findByApi_IdApiAndEntorno_IdEntorno(apiId, entornoBusquedaId);

            if (optConfig.isPresent()) {
                apiBaseUrl = optConfig.get().getUrlBase().trim().replaceAll("/+$", "");
            } else {
                return new SandboxResponseDto(404, 0,
                        "{\"error\": \"No se encontró la configuración de entorno para esta API.\"}",
                        Collections.emptyMap());
            }
        }

        String targetPath = requestDto.getTargetUrl().trim();
        if (!targetPath.startsWith("/")) targetPath = "/" + targetPath;

        Documentacion doc = documentacionRepository.findFirstByApi_IdApi(apiId)
                .orElseThrow(() -> new RuntimeException("Documentación no encontrada"));

        try {
            Map<String, Object> spec = objectMapper.readValue(doc.getContenido(), Map.class);
            Map<String, Object> paths = (Map<String, Object>) spec.get("paths");

            if (!paths.containsKey(targetPath)) {
                return new SandboxResponseDto(400, 0,
                        "{\"error\": \"El endpoint solicitado no pertenece a la API seleccionada.\"}",
                        Collections.emptyMap());
            }
        } catch (Exception e) {
            return new SandboxResponseDto(500, 0,
                    "{\"error\": \"Error al validar la documentación de la API.\", \"detalle\": \"" + e.getMessage() + "\"}",
                    Collections.emptyMap());
        }

        fullUrl = apiBaseUrl + targetPath;


        String bodyString = null;
        if (requestDto.getBody() != null) {
            if (requestDto.getBody() instanceof String) {
                bodyString = ((String) requestDto.getBody()).trim();
            } else {
                try {
                    bodyString = objectMapper.writeValueAsString(requestDto.getBody());
                } catch (Exception e) {
                    return new SandboxResponseDto(500, 0,
                            "{\"error\": \"No se pudo convertir el body a JSON.\", \"detalle\": \"" + e.getMessage() + "\"}",
                            Collections.emptyMap());
                }
            }
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!apiKey.isEmpty()) headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(bodyString, headers);
        long startTime = System.currentTimeMillis();


        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.valueOf(requestDto.getMethod().toUpperCase()),
                    entity,
                    String.class
            );
            long endTime = System.currentTimeMillis();

            return new SandboxResponseDto(
                    response.getStatusCodeValue(),
                    endTime - startTime,
                    response.getBody(),
                    response.getHeaders().toSingleValueMap()
            );

        } catch (HttpClientErrorException e) {
            long endTime = System.currentTimeMillis();
            if (e.getRawStatusCode() == 429) {
                return new SandboxResponseDto(429, endTime - startTime,
                        "{\"code\":429,\"error_type\":\"RateLimitExceeded\",\"detail\":\"Límite de Rate-Limit excedido.\"}",
                        Collections.emptyMap());
            }
            return new SandboxResponseDto(
                    e.getRawStatusCode(),
                    endTime - startTime,
                    e.getResponseBodyAsString(),
                    e.getResponseHeaders() != null ? e.getResponseHeaders().toSingleValueMap() : Collections.emptyMap()
            );
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            e.printStackTrace();
            return new SandboxResponseDto(500, endTime - startTime,
                    "{\"error\":\"Error de conexión/servidor.\",\"detalle\":\"" + e.getMessage() + "\",\"url_intento\":\"" + fullUrl + "\"}",
                    Collections.emptyMap());
        }
    }




}