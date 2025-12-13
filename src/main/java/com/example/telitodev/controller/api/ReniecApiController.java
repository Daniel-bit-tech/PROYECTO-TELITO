package com.example.telitodev.controller.api;

import com.example.telitodev.dto.ReniecResponseDto;
import com.example.telitodev.service.ReniecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST para consultar datos de RENIEC
 */
@RestController
@RequestMapping("/api/reniec")
public class ReniecApiController {

    @Autowired
    private ReniecService reniecService;

    /**
     * Consulta los datos de una persona por su DNI
     * 
     * @param dni Documento Nacional de Identidad (8 dígitos)
     * @return Respuesta con datos de RENIEC
     */
    @GetMapping("/consultar/{dni}")
    public ResponseEntity<ReniecResponseDto> consultarDNI(@PathVariable String dni) {
        try {
            System.out.println("📡 API: Consultando DNI: " + dni);
            
            // Validar formato básico
            if (dni == null || !dni.matches("^[0-9]{8}$")) {
                ReniecResponseDto errorResponse = new ReniecResponseDto();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("El DNI debe tener exactamente 8 dígitos numéricos");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Consultar RENIEC
            ReniecResponseDto response = reniecService.consultarDNI(dni);
            
            if (response.isSuccess()) {
                System.out.println("✅ API: Datos obtenidos correctamente");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("⚠️ API: Error en RENIEC - " + response.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ API: Error consultando RENIEC: " + e.getMessage());
            e.printStackTrace();
            
            ReniecResponseDto errorResponse = new ReniecResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error interno al consultar RENIEC: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
