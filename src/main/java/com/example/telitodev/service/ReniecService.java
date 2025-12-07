package com.example.telitodev.service;

import com.example.telitodev.dto.ReniecResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para consultar datos de DNI peruano mediante la API de RENIEC (apiperu.dev)
 * Permite validar y obtener nombres/apellidos automáticamente
 */
@Service
public class ReniecService {

    private static final Logger logger = LoggerFactory.getLogger(ReniecService.class);

    @Value("${reniec.api.url:https://apiperu.dev/api/dni}")
    private String reniecApiUrl;

    @Value("${reniec.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ReniecService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Consulta los datos de una persona por su DNI en RENIEC
     *
     * @param dni Documento Nacional de Identidad (8 dígitos)
     * @return ReniecResponseDto con los datos de la persona
     */
    public ReniecResponseDto consultarDNI(String dni) {
        logger.info("🔍 Consultando DNI en RENIEC: {}", dni);
        logger.info("🔑 API Key configurada: {}", (apiKey != null && !apiKey.isEmpty()) ? "SÍ (***)" : "NO");
        logger.info("🌐 URL Base: {}", reniecApiUrl);

        // Validar formato de DNI
        if (!esValidoDNI(dni)) {
            logger.warn("❌ DNI inválido: {}", dni);
            return crearRespuestaError("DNI inválido. Debe tener 8 dígitos numéricos");
        }

        try {
            // Construir URL completa
            String url = reniecApiUrl + "/" + dni;
            logger.info("📡 Llamando a API: {}", url);

            // Configurar headers con Bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            logger.info("📋 Headers configurados - Authorization: Bearer ***");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Hacer la llamada a la API
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            logger.info("📊 Status Code: {}", response.getStatusCode());

            // Procesar respuesta exitosa
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                logger.info("✅ Respuesta recibida: {}", body);

                return mapearRespuesta(body);
            } else {
                logger.warn("⚠️ Respuesta no exitosa: {}", response.getStatusCode());
                return crearRespuestaError("No se pudo obtener información del DNI");
            }

        } catch (HttpClientErrorException e) {
            logger.error("❌ Error del cliente (4xx): Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return crearRespuestaError("DNI no encontrado en RENIEC");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return crearRespuestaError("API Key inválida o expirada");
            } else {
                return crearRespuestaError("Error al consultar RENIEC: " + e.getMessage());
            }

        } catch (HttpServerErrorException e) {
            logger.error("❌ Error del servidor (5xx): Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return crearRespuestaError("El servicio de RENIEC no está disponible temporalmente");

        } catch (Exception e) {
            logger.error("❌ Error inesperado: {}", e.getMessage(), e);
            return crearRespuestaError("Error al consultar DNI: " + e.getMessage());
        }
    }

    /**
     * Mapea la respuesta de la API a nuestro DTO
     */
    private ReniecResponseDto mapearRespuesta(Map<String, Object> body) {
        ReniecResponseDto response = new ReniecResponseDto();
        
        // Verificar si la respuesta indica éxito
        boolean success = body.containsKey("success") 
            ? (Boolean) body.get("success") 
            : body.containsKey("data");

        response.setSuccess(success);

        if (success && body.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            
            ReniecResponseDto.ReniecData reniecData = new ReniecResponseDto.ReniecData();
            
            // La API devuelve "numero" que puede venir como String o Integer
            Object numeroObj = data.get("numero");
            String dni = numeroObj != null ? numeroObj.toString() : null;
            reniecData.setDni(dni);
            
            reniecData.setNombres((String) data.get("nombres"));
            reniecData.setApellidoPaterno((String) data.get("apellido_paterno"));
            reniecData.setApellidoMaterno((String) data.get("apellido_materno"));
            reniecData.setNombreCompleto((String) data.get("nombre_completo"));
            
            Object codigoObj = data.get("codigo_verificacion");
            String codigoVerificacion = codigoObj != null ? codigoObj.toString() : null;
            reniecData.setCodigoVerificacion(codigoVerificacion);

            response.setData(reniecData);
            response.setMessage("Consulta exitosa");

            logger.info("👤 Datos mapeados: {} {} {}", 
                reniecData.getNombres(), 
                reniecData.getApellidoPaterno(), 
                reniecData.getApellidoMaterno()
            );
        } else {
            String message = body.containsKey("message") 
                ? (String) body.get("message") 
                : "No se encontraron datos";
            response.setMessage(message);
        }

        return response;
    }

    /**
     * Crea una respuesta de error
     */
    private ReniecResponseDto crearRespuestaError(String mensaje) {
        ReniecResponseDto response = new ReniecResponseDto();
        response.setSuccess(false);
        response.setMessage(mensaje);
        return response;
    }

    /**
     * Valida que el DNI tenga formato correcto (8 dígitos)
     */
    private boolean esValidoDNI(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }

        // Limpiar espacios
        dni = dni.trim();

        // Verificar que tenga exactamente 8 caracteres numéricos
        return dni.matches("\\d{8}");
    }

    /**
     * Verifica si el servicio de RENIEC está disponible
     */
    public boolean isServiceAvailable() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Hacer una petición de prueba (puedes usar un DNI conocido)
            ResponseEntity<String> response = restTemplate.exchange(
                    reniecApiUrl + "/00000000",  // DNI de prueba
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode() != HttpStatus.INTERNAL_SERVER_ERROR;

        } catch (HttpClientErrorException e) {
            // 404 o 401 significa que la API está respondiendo
            return e.getStatusCode() == HttpStatus.NOT_FOUND || 
                   e.getStatusCode() == HttpStatus.UNAUTHORIZED;
        } catch (Exception e) {
            logger.warn("❌ Servicio de RENIEC no disponible: {}", e.getMessage());
            return false;
        }
    }
}
