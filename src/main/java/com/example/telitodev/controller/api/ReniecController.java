package com.example.telitodev.controller.api;

import com.example.telitodev.dto.ReniecResponseDto;
import com.example.telitodev.service.ReniecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para la integración con RENIEC (API de Perú)
 * Permite consultar y validar DNI peruanos
 */
@RestController
@RequestMapping("/api/reniec")
@CrossOrigin(origins = "*") // Permitir CORS para el frontend
public class ReniecController {

    private static final Logger logger = LoggerFactory.getLogger(ReniecController.class);

    private final ReniecService reniecService;

    public ReniecController(ReniecService reniecService) {
        this.reniecService = reniecService;
    }

    /**
     * Endpoint para consultar DNI en RENIEC
     * 
     * @param dni Documento Nacional de Identidad (8 dígitos)
     * @return Datos de la persona (nombres y apellidos)
     * 
     * Ejemplo de uso:
     * GET /api/reniec/dni/12345678
     * 
     * Respuesta exitosa:
     * {
     *   "success": true,
     *   "data": {
     *     "dni": "12345678",
     *     "nombres": "JUAN CARLOS",
     *     "apellidoPaterno": "PEREZ",
     *     "apellidoMaterno": "GARCIA",
     *     "nombreCompleto": "PEREZ GARCIA JUAN CARLOS"
     *   },
     *   "message": "Consulta exitosa"
     * }
     * 
     * Respuesta de error:
     * {
     *   "success": false,
     *   "message": "DNI no encontrado en RENIEC"
     * }
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ReniecResponseDto> consultarDNI(@PathVariable String dni) {
        logger.info("📥 Solicitud de consulta DNI: {}", dni);

        try {
            // Validación básica
            if (dni == null || dni.trim().isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(crearRespuestaError("El DNI es requerido"));
            }

            // Limpiar DNI (quitar espacios)
            dni = dni.trim();

            // Validar longitud
            if (dni.length() != 8) {
                return ResponseEntity
                    .badRequest()
                    .body(crearRespuestaError("El DNI debe tener 8 dígitos"));
            }

            // Validar que solo contenga números
            if (!dni.matches("\\d+")) {
                return ResponseEntity
                    .badRequest()
                    .body(crearRespuestaError("El DNI debe contener solo números"));
            }

            // Consultar en RENIEC
            ReniecResponseDto response = reniecService.consultarDNI(dni);

            // Retornar respuesta apropiada según el resultado
            if (response.isSuccess()) {
                logger.info("✅ Consulta exitosa para DNI: {}", dni);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("⚠️ Consulta sin éxito para DNI: {} - {}", dni, response.getMessage());
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
            }

        } catch (Exception e) {
            logger.error("❌ Error en consulta de DNI {}: {}", dni, e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(crearRespuestaError("Error interno al consultar DNI: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar si el servicio de RENIEC está disponible
     * 
     * GET /api/reniec/health
     * 
     * Respuesta:
     * {
     *   "available": true,
     *   "message": "Servicio de RENIEC disponible"
     * }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        logger.info("🏥 Verificando salud del servicio RENIEC");

        Map<String, Object> response = new HashMap<>();
        
        boolean isAvailable = reniecService.isServiceAvailable();
        
        response.put("available", isAvailable);
        response.put("message", isAvailable 
            ? "Servicio de RENIEC disponible" 
            : "Servicio de RENIEC no disponible");
        
        HttpStatus status = isAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Helper para crear respuesta de error
     */
    private ReniecResponseDto crearRespuestaError(String mensaje) {
        ReniecResponseDto response = new ReniecResponseDto();
        response.setSuccess(false);
        response.setMessage(mensaje);
        return response;
    }
}
