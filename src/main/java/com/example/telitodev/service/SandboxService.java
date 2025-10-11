package com.example.telitodev.service;


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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SandboxService {

    @Autowired
    private ApiRepository apiRepository;
    @Autowired private DocumentacionRepository documentacionRepository;
    @Autowired private ApiHasEntornoRepository apiHasEntornoRepository;
    @Autowired private CredencialApiRepository credencialApiRepository;
    @Autowired private ObjectMapper objectMapper; // Para manejar JSON

    /**
     * Lógica para el endpoint "Bibliotecario".
     */
    public SandboxApiDetailsDto getApiDetails(Integer apiId, String userDni) {
        // 1. Obtener datos de la API

        credencialApiRepository
                .findFirstByUsuario_DniAndApi_IdApiAndEstado(userDni, apiId, true)
                .orElseThrow(() -> new RuntimeException("Acceso denegado: No tienes una credencial activa para esta API."));
        // Si no se encuentra una credencial, la línea de arriba lanza un error y el método se detiene aquí.

        Api api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API no encontrada"));

        Documentacion doc = documentacionRepository.findFirstByApi_IdApi(apiId)
                .orElseThrow(() -> new RuntimeException("Documentación no encontrada para la API con ID: " + apiId));




        // 3. Obtener entornos de prueba
        List<ApiHasEntorno> entornosRel = apiHasEntornoRepository.findByApi_IdApi(apiId);
        List<SandboxEnvironmentDto> entornosDto = entornosRel.stream()
                .filter(rel -> !rel.getEntorno().getNombre().equalsIgnoreCase("Producción"))
                .filter(rel -> !rel.getEntorno().getNombre().equalsIgnoreCase("QA")) // <-- FILTRO AÑADIDO
                .map(rel -> new SandboxEnvironmentDto(rel.getEntorno().getNombre(), rel.getUrlBase()))
                .collect(Collectors.toList());

        // 4. Construir el DTO de respuesta
        SandboxApiDetailsDto detailsDto = new SandboxApiDetailsDto();
        detailsDto.setNombre(api.getNombre());
        detailsDto.setEntornos(entornosDto);
        try {
            // Convertimos el string JSON del campo 'contenido' a un Map
            Map<String, Object> spec = objectMapper.readValue(doc.getContenido(), Map.class);
            detailsDto.setSpec(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la especificación OpenAPI");
        }

        return detailsDto;
    }

    /**
     * Lógica para el endpoint "Proxy".
     */
    public SandboxResponseDto executeRequest(SandboxRequestDto requestDto, String userDni) {
        // 1. Obtener la API Key del usuario para esta API
        CredencialApi credencial = credencialApiRepository
                .findFirstByUsuario_DniAndApi_IdApiAndEstado(userDni, requestDto.getApiId(), true)
                .orElseThrow(() -> new RuntimeException("No tienes una API Key activa para esta API."));

        // 2. Preparar y ejecutar la llamada HTTP externa
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        requestDto.getHeaders().forEach(headers::add);

        // **Paso de Seguridad Clave: Añadir la API Key**
        headers.add("X-API-KEY", credencial.getApiKey()); // O el header que usen tus APIs

        HttpEntity<String> entity = new HttpEntity<>(requestDto.getBody(), headers);
        HttpMethod method = HttpMethod.valueOf(requestDto.getMethod().toUpperCase());

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(requestDto.getTargetUrl(), method, entity, String.class);
        } catch (HttpClientErrorException e) {
            // Capturar errores HTTP (4xx, 5xx) para devolverlos al usuario
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
        long endTime = System.currentTimeMillis();

        // 3. Construir la respuesta para el frontend
        Map<String, String> responseHeaders = response.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));

        return new SandboxResponseDto(
                response.getStatusCodeValue(),
                (endTime - startTime),
                response.getBody(),
                responseHeaders
        );
    }
}