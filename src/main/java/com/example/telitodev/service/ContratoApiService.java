package com.example.telitodev.service;

import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.VersionApi;
import com.example.telitodev.repository.ContratoRepository;
import com.example.telitodev.repository.VersionApiRepository;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Service
public class ContratoApiService {

    // Tamaño máximo de archivo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final DocApiService validationService;
    private final FileSecurityService fileSecurityService;
    private final S3DocsApiService s3DocsApiService;

    private final ContratoRepository contratoRepository;
    private final VersionApiRepository versionApiRepository;

    public ContratoApiService(DocApiService validationService, FileSecurityService fileSecurityService, S3DocsApiService s3DocsApiService, ContratoRepository contratoRepository, VersionApiRepository versionApiRepository) {
        this.validationService = validationService;
        this.fileSecurityService = fileSecurityService;
        this.s3DocsApiService = s3DocsApiService;
        this.contratoRepository = contratoRepository;
        this.versionApiRepository = versionApiRepository;
    }


    /**
     * Coordina la validación y procesa el contrato API desde DTO
     */
    public void validarYProcesarContrato(VersionContratoDTO versionContratoDto, Api api) throws ContratoValidationException {

        boolean subioArchivo = versionContratoDto.getMetodoCarga()==VersionContratoDTO.MetodoCarga.archivo
                && versionContratoDto.getArchivo() != null && !versionContratoDto.getArchivo().isEmpty();

        if (subioArchivo) {
            versionContratoDto.setDesdeArchivo(true);

            MultipartFile contratoFile = versionContratoDto.getArchivo();
            versionContratoDto.setFormato(detectarFormato(contratoFile));

        } else if (versionContratoDto.getMetodoCarga()==VersionContratoDTO.MetodoCarga.texto && versionContratoDto.getContenido()!=null && !versionContratoDto.getContenido().isEmpty()) {
            versionContratoDto.setDesdeArchivo(false);

            String contenido = versionContratoDto.getContenido().trim();
            versionContratoDto.setFormato(detectarFormatoTexto(contenido));

        } else throw new SecurityException("Error procesando el archivo.");

        try {
            String contenidoNormalizado = fileSecurityService.validarSintaxis(versionContratoDto);
            String cabeceras = validationService.validarSemanticaOpenAPI(contenidoNormalizado);

            VersionApi versionApi = new VersionApi();
            versionApi.setApi(api);
            versionApi.setVersion(versionContratoDto.getVersion());
            versionApi.setFechaPublicacion(versionContratoDto.getFechaPublicacion());
            versionApi.setEstadoVersion(versionContratoDto.getEstadoVersion());
            // Persistir la versión primero para asegurarnos de tener un id válido
            versionApiRepository.save(versionApi);

//            ContratoApi contratoApi = new ContratoApi();
//            contratoApi.setVersionApi(versionApi);
//            contratoApi.setFormato(versionContratoDto.getFormato());
//            contratoApi.setContenido(cabeceras);
//            contratoRepository.save(contratoApi);

            versionContratoDto.setIdVersion(versionApi.getIdVersion());
            s3DocsApiService.subirContratoAS3(versionContratoDto, contenidoNormalizado, cabeceras);

        } catch (SecurityException e) {
            throw new ContratoValidationException("Error validando archivo: " +e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error validando contrato: " +e.getMessage());
            throw new ContratoValidationException("Error procesando el contrato: ", e);
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
     * Detecta automáticamente el formato del contenido desde texto plano
     * @param contenido texto plano dej contrato
     * @return JSON o YAML
     * @throws ContratoValidationException cuando no se puede obtener un formato válido
     */
    public ContratoApi.FormatoContrato detectarFormatoTexto(String contenido) throws ContratoValidationException {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ContratoValidationException("No se puede procesar contenido vacío.");
        }

        String contenidoTrim = contenido.trim();

        if (contenidoTrim.startsWith("{") || contenidoTrim.startsWith("[")) {
            return ContratoApi.FormatoContrato.JSON;
        }

        if (contenidoTrim.contains("openapi:") || contenidoTrim.contains("swagger:")) {
            return ContratoApi.FormatoContrato.YAML;
        }

        throw new ContratoValidationException("El contenido no tiene formato válido.");
    }

    /**
     * Detecta automáticamente el formato del archivo de contrato
     * @param archivoContrato el archivo JSON o YAML
     * @return JSON o YAML
     * @throws ContratoValidationException cuando no se puede obtener un formato válido
     */
    public ContratoApi.FormatoContrato detectarFormato(MultipartFile archivoContrato) throws ContratoValidationException {
        String filename = archivoContrato.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new ContratoValidationException("El archivo no tiene extensión válida.");
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        String contentType = archivoContrato.getContentType();
        if (contentType != null) {
            switch (contentType) {
                case "application/json":
                    if (extension.equals("json")) {
                        return ContratoApi.FormatoContrato.JSON;
                    }

                case "application/x-yaml", "application/yaml", "application/x-yml", "application/yml", "application/octet-stream":
                    if (extension.equals("yaml") || extension.equals("yml")) {
                        return ContratoApi.FormatoContrato.YAML;
                    }
                default:
                    throw new ContratoValidationException("El archivo no tiene formato válido.");
            }
            
        } else throw new ContratoValidationException("El archivo no tiene formato válido.");
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
