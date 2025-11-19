package com.example.telitodev.service.creacionApi;

import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.VersionApi;
import com.example.telitodev.repository.VersionApiRepository;
import com.example.telitodev.service.FileSecurityService;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.example.telitodev.service.ValidateApiDocsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ContratoApiService {

    // Tamaño máximo de archivo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final ValidateApiDocsService validationService;
    private final FileSecurityService fileSecurityService;
    private final S3DocsApiService s3DocsApiService;

    private final VersionApiRepository versionApiRepository;

    public ContratoApiService(ValidateApiDocsService validationService, FileSecurityService fileSecurityService, S3DocsApiService s3DocsApiService, VersionApiRepository versionApiRepository) {
        this.validationService = validationService;
        this.fileSecurityService = fileSecurityService;
        this.s3DocsApiService = s3DocsApiService;
        this.versionApiRepository = versionApiRepository;
    }


    /**
     * Coordina la validación y procesa el contrato API desde DTO
     */
    public void validarYProcesarContrato(VersionContratoDTO versionContratoDto, Api api) throws ContratoValidationException {

//        boolean subioArchivo = versionContratoDto.getMetodoCarga()==VersionContratoDTO.MetodoCarga.archivo
//                && versionContratoDto.getContrato() != null && !versionContratoDto.getContrato().isEmpty();
        boolean subioArchivo = versionContratoDto.getContrato() != null && !versionContratoDto.getContrato().isEmpty();

        if (subioArchivo) {
            versionContratoDto.setDesdeArchivo(true);

            MultipartFile contratoFile = versionContratoDto.getContrato();
            versionContratoDto.setFormato(detectarFormato(contratoFile));

        } else if (versionContratoDto.getMetodoCarga()==VersionContratoDTO.MetodoCarga.texto && versionContratoDto.getContenido()!=null && !versionContratoDto.getContenido().isEmpty()) {
            versionContratoDto.setDesdeArchivo(false);

            String contenido = versionContratoDto.getContenido().trim();
            versionContratoDto.setFormato(detectarFormatoTexto(contenido));

        } else throw new SecurityException("Error procesando el archivo.");

        try {
            String contenidoNormalizado = fileSecurityService.validarSintaxisContrato(versionContratoDto);
            String cabeceras = validationService.validarSemanticaOpenAPI(contenidoNormalizado);

            VersionApi versionApi = new VersionApi();
            if (versionContratoDto.getIdVersion()==null) {
                versionApi.setApi(api);
                versionApi.setVersion(versionContratoDto.getVersion());
                versionApi.setFechaPublicacion(LocalDate.now());
                versionApi.setEstadoVersion(VersionApi.EstadoVersion.EN_CONSTRUCCION);
                // Persistir la versión primero para asegurarnos de tener un id válido
                versionApiRepository.save(versionApi);
            } else {
                versionApi = versionApiRepository.findById(versionContratoDto.getIdVersion())
                        .filter(v -> v.getApi().equals(api))
                        .orElseThrow(() -> new Exception("No se encontró la versión."));;
            }

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
                    break;

                case "application/x-yaml", "application/yaml", "application/x-yml", "application/yml", "application/octet-stream":
                    if (extension.equals("yaml") || extension.equals("yml")) {
                        return ContratoApi.FormatoContrato.YAML;
                    }
                    break;

                default:
                    throw new ContratoValidationException("El archivo no tiene formato válido.");
            }
            
        } else throw new ContratoValidationException("El archivo no tiene formato válido.");
        return null;
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
