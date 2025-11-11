package com.example.telitodev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocApiService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "md", "json", "yaml", "yml");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_FILE_COUNT = 5;


    public void validarDescriptions(List<MultipartFile> files, List<String> descriptions) {
        if (descriptions == null || descriptions.size() != files.size()) {
            throw new IllegalArgumentException("Cada archivo debe tener una descripción");
        }

        for (int i = 0; i < descriptions.size(); i++) {
            String desc = descriptions.get(i);
            if (desc == null || desc.trim().length() < 5) {
                String fileName = files.get(i).getOriginalFilename();
                throw new IllegalArgumentException(
                        String.format("La descripción para '%s' debe tener al menos 5 caracteres", fileName)
                );
            }
        }
    }

    public String validarSemanticaOpenAPI(String content) throws ContratoApiService.ContratoValidationException {
        SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readContents(content);
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();

        if (openAPI == null || openAPI.getComponents() == null || openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            throw new ContratoApiService.ContratoValidationException("El contrato no cumple con OpenAPI 3.x");
        }

        // Construir un mapa simple con path -> descripción
        Map<String, String> pathsMap = new LinkedHashMap<>();
        openAPI.getPaths().forEach((path, pathItem) -> {
            // Tomamos la primera operación disponible (ej. GET, POST, etc.)
            if (pathItem.getGet() != null && pathItem.getGet().getSummary() != null) {
                pathsMap.put(path, pathItem.getGet().getSummary());
            } else if (pathItem.getPost() != null && pathItem.getPost().getSummary() != null) {
                pathsMap.put(path, pathItem.getPost().getSummary());
            } else {
                pathsMap.put(path, "Sin descripción");
            }
        });

        // Serializar a JSON con formato { "paths": { ... } }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> wrapper = Map.of("paths", pathsMap);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);
        } catch (Exception e) {
            throw new ContratoApiService.ContratoValidationException("Error al convertir paths a JSON: " + e.getMessage(), e);
        }
    }

//    public void validarEstrictaSemanticaOpenAPI(String content, List<String> semanticas) {
//        OpenApi3 api = new OpenApi3Parser().parse(content, false);
//        api.validate(); // lanza excepciones si no cumple
//    }


}