package com.example.telitodev.service;

import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.ContratoApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ContratoApiService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoApiService.class);

    // Tamaño máximo de archivo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Valida y procesa el contrato API desde DTO
     */
    public String validarYProcesarContrato(VersionContratoDTO versionContratoDto) throws ContratoValidationException {
        try {
            String contenido;

            if (versionContratoDto.isDesdeArchivo()) {
                contenido = procesarArchivoContrato(versionContratoDto.getArchivo());
            } else {
                contenido = procesarContenidoDirecto(versionContratoDto.getContenido());
            }

            // Validar formato específico
            validarFormatoContrato(contenido, versionContratoDto.getFormato());

            return contenido;

        } catch (Exception e) {
            System.err.println("Error validando contrato para API: " + versionContratoDto.getNombreAPI()+" con ID "+versionContratoDto.getIdAPI());
            System.err.println(e.getMessage());
            throw new ContratoValidationException("Error al procesar el contrato: " + e.getMessage());
        }
    }

    /**
     * Procesa archivo subido y extrae contenido
     */
    private String procesarArchivoContrato(MultipartFile archivo) throws ContratoValidationException {
        if (archivo == null || archivo.isEmpty()) {
            throw new ContratoValidationException("No se ha proporcionado un archivo de contrato");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new ContratoValidationException("El archivo es demasiado grande. Tamaño máximo: 10MB");
        }

        // Validar tipo de archivo
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !esExtensionValida(nombreArchivo)) {
            throw new ContratoValidationException("Formato de archivo no válido. Use .yaml, .yml o .json");
        }

        try {
            // Leer contenido del archivo
            return new String(archivo.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ContratoValidationException("Error al leer el archivo: " + e.getMessage());
        }
    }

    /**
     * Procesa contenido directo pegado
     */
    private String procesarContenidoDirecto(String contenido) throws ContratoValidationException {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ContratoValidationException("El contenido del contrato no puede estar vacío");
        }

        if (contenido.length() > MAX_FILE_SIZE) {
            throw new ContratoValidationException("El contenido es demasiado grande. Límite: 10MB");
        }

        return contenido.trim();
    }

    /**
     * Valida el formato del contrato (JSON/YAML)
     */
    private void validarFormatoContrato(String contenido, ContratoApi.FormatoContrato formato)
            throws ContratoValidationException {

        try {
            switch (formato) {
                case JSON:
                    validarJSON(contenido);
                    break;
                case YAML:
                    validarYAML(contenido);
                    break;
                default:
                    throw new ContratoValidationException("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            throw new ContratoValidationException("Error en la validación del formato " + formato + ": " + e.getMessage());
        }
    }

    /**
     * Valida formato JSON
     */
    private void validarJSON(String contenido) throws ContratoValidationException {
        try {

            System.out.println(contenido);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(contenido);

            // Validaciones básicas de OpenAPI
            if (!jsonNode.has("openapi") && !jsonNode.has("swagger")) {
                throw new ContratoValidationException("No se detectó especificación OpenAPI/Swagger válida");
            }

            if (!jsonNode.has("info")) {
                throw new ContratoValidationException("Falta sección 'info' requerida en OpenAPI");
            }

            if (!jsonNode.has("paths")) {
                throw new ContratoValidationException("Falta sección 'paths' requerida en OpenAPI");
            }

            System.out.println("Contrato JSON validado exitosamente");

        } catch (JsonProcessingException e) {
            throw new ContratoValidationException("JSON malformado: " + e.getMessage());
        }
    }

    /**
     * Valida formato YAML
     */
    private void validarYAML(String contenido) throws ContratoValidationException {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(contenido);

            if (data == null) {
                throw new ContratoValidationException("YAML vacío o malformado");
            }

            // Validaciones básicas de OpenAPI
            if (!data.containsKey("openapi") && !data.containsKey("swagger")) {
                throw new ContratoValidationException("No se detectó especificación OpenAPI/Swagger válida");
            }

            if (!data.containsKey("info")) {
                throw new ContratoValidationException("Falta sección 'info' requerida en OpenAPI");
            }

            if (!data.containsKey("paths")) {
                throw new ContratoValidationException("Falta sección 'paths' requerida en OpenAPI");
            }

            System.out.println("Contrato YAML validado exitosamente");

        } catch (Exception e) {
            throw new ContratoValidationException("YAML malformado: " + e.getMessage());
        }
    }

    /**
     * Verifica extensión de archivo válida
     */
    private boolean esExtensionValida(String nombreArchivo) {
        String extension = nombreArchivo.toLowerCase();
        return extension.endsWith(".yaml") || extension.endsWith(".yml") || extension.endsWith(".json");
    }

    /**
     * Detecta automáticamente el formato del contenido
     */
    public ContratoApi.FormatoContrato detectarFormato(String contenido) {
        if (contenido == null) {
            return ContratoApi.FormatoContrato.JSON;
        }

        String contenidoTrim = contenido.trim();

        // Si empieza con { o [, probablemente es JSON
        if (contenidoTrim.startsWith("{") || contenidoTrim.startsWith("[")) {
            return ContratoApi.FormatoContrato.JSON;
        }

        // Si contiene openapi: o swagger:, probablemente es YAML
        if (contenidoTrim.contains("openapi:") || contenidoTrim.contains("swagger:")) {
            return ContratoApi.FormatoContrato.YAML;
        }

        // Por defecto JSON
        return ContratoApi.FormatoContrato.JSON;
    }


    public static class ContratoValidationException extends Exception {

        public ContratoValidationException(String message) {
            super(message);
        }

        public ContratoValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
