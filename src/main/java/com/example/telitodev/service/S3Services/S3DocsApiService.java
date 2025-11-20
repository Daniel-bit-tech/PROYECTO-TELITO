package com.example.telitodev.service.S3Services;

import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.VersionApi;
import com.example.telitodev.repository.ContratoRepository;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.repository.VersionApiRepository;
import com.example.telitodev.service.creacionApi.ContratoApiService;
import com.example.telitodev.service.creacionApi.DocApiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class S3DocsApiService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final DocumentacionRepository documentacionRepository;
    private final ContratoRepository contratoRepository;
    private final String bucketName = "ipt-symphony-bucket-2";
    private final VersionApiRepository versionApiRepository;

    public S3DocsApiService(S3Client s3Client, S3Presigner s3Presigner,
                            DocumentacionRepository documentacionRepository,
                            ContratoRepository contratoRepository, VersionApiRepository versionApiRepository) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.documentacionRepository = documentacionRepository;
        this.contratoRepository = contratoRepository;
        this.versionApiRepository = versionApiRepository;
    }

    public void subirContratoAS3(VersionContratoDTO dto, String contenidoNormalizado, String cabecerasContrato) throws ContratoApiService.ContratoValidationException {
        // 1. Generar nombres y rutas
        String nombreArchivo = generarNombreArchivo(dto.getIdAPI(), dto.getIdVersion(), dto.getFormato().name());
//        String s3Key = generarS3Key(dto.getIdAPI(), nombreArchivo);
        String s3Key = "apis/api_"+dto.getIdAPI()+"/ver/v_"+dto.getIdVersion()+"/" + nombreArchivo;

        try {
            // 2. Subir a S3 con metadatos
            byte[] bytes = contenidoNormalizado.getBytes(StandardCharsets.UTF_8);

            // Metadata para S3
            Map<String, String> metadata = new HashMap<>();
//            metadata.put("original-filename", file.getOriginalFilename());
            metadata.put("formato", dto.getFormato().name());
            metadata.put("api-id", dto.getIdAPI().toString());
            metadata.put("ver-id", dto.getIdVersion().toString());
            metadata.put("uploaded-at", Instant.now().toString());
            metadata.put("content-type", obtenerContentType(dto.getFormato()));

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(obtenerContentType(dto.getFormato()))
                    .metadata(metadata)
                    .build();
            RequestBody requestBody = RequestBody.fromBytes(bytes);

            s3Client.putObject(request,requestBody);

            // 4. Guardar metadata en BD
            guardarMetadataContrato(dto, s3Key, cabecerasContrato);


        } catch (Exception e) {
            throw new ContratoApiService.ContratoValidationException("Error guardando contrato "+ e.getMessage(), e);
        }
    }

    public void subirDocAS3(Documentacion doc, MultipartFile archivoDoc) throws DocApiService.DocValidationException {
        // 1. Generar nombres y rutas
        Integer idVersion = (doc.getVersionApi() != null) ? doc.getVersionApi().getIdVersion() : null;
        String nombreArchivo = generarNombreArchivo(doc.getApi().getIdApi(), idVersion, doc.getFormato().name());
        String s3Key = "apis/api_"+doc.getApi().getIdApi();
        if (idVersion != null) {
            s3Key += "/ver/v_"+idVersion+"/" + nombreArchivo;
        } else {
            s3Key += "/"+nombreArchivo;
        }

        try (InputStream is = archivoDoc.getInputStream()) {
            // 2. Subir a S3 con metadatos

            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", archivoDoc.getOriginalFilename());
            metadata.put("formato", doc.getFormato().name());
            metadata.put("api-id", doc.getApi().getIdApi().toString());
            metadata.put("uploaded-at", Instant.now().toString());
            metadata.put("content-type", obtenerDocContentType(doc.getFormato()));

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(obtenerDocContentType(doc.getFormato()))
                    .metadata(metadata)
                    .build();
            RequestBody requestBody = RequestBody.fromInputStream(is,archivoDoc.getSize());

            s3Client.putObject(request,requestBody);
            doc.setUrlDocumento(s3Key);

        } catch (Exception e) {
            throw new DocApiService.DocValidationException("Error guardando documento "+ e.getMessage(), e);
        }
    }

    /* ========== GENERACIÓN DE NOMBRES Y RUTAS ========== */
    private String generarNombreArchivo(Integer idApi, Integer idVersion, String formato) {
        String timestamp = Instant.now().toString().replace(":", "-");
        String extension = switch (formato) {
            case "JSON" -> ".json";
            case "YAML" -> ".yaml";
            case "PDF" -> ".pdf";
            case "MARKDOWN" -> ".md";
            default -> ".txt";
        };
        String base = "doc-api" + idApi;

        if (idVersion != null) {
            base += "-v" + idVersion;
        }
        return base + "-" + UUID.randomUUID() + extension;
    }

    /* ========== GENERACIÓN DE S3KEY ========== */
    private String generateS3Key(Integer idApi, Integer idVersion, String nombreArchivo) {
        String uuid = UUID.randomUUID().toString();
        return String.format("apis/api_%s/ver/v_%s", idApi, idVersion);
    }


    /* ========== SUBIDA DE DOC ADICIONAL A S3 CON METADATOS ========== */
    private void subirDocAddAS3(Map<String, String> metadata, String s3Key, RequestBody requestBody, Documentacion.FormatoDoc formato) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(obtenerDocContentType(formato))
                    .metadata(metadata)
                    .build();

            s3Client.putObject(request,requestBody);

        } catch (Exception e) {
            throw new RuntimeException("Error subiendo archivo a S3: " + e.getMessage(), e);
        }
    }

    private String obtenerContentType(ContratoApi.FormatoContrato formato) {
        return formato == ContratoApi.FormatoContrato.JSON ? "application/json" : "application/x-yaml";
    }

    private String obtenerDocContentType(Documentacion.FormatoDoc formato) {
        return switch (formato) {
            case JSON -> "application/json";
            case YAML -> "application/x-yaml";
            case MARKDOWN -> "text/markdown";
            case PDF -> "application/pdf";
        };
    }


    private URL generarPresignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignedRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner.presignGetObject(presignedRequest).url();
    }

    /* ========== GUARDADO EN BASE DE DATOS ========== */
    private void guardarMetadataContrato(VersionContratoDTO dto, String s3Key, String cabecerasContrato) {
            ContratoApi contrato = new ContratoApi();

            // Información básica
            VersionApi version = versionApiRepository.findById(dto.getIdVersion())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la versión solicitada"));

            contrato.setVersionApi(version);
            contrato.setFormato(dto.getFormato());

            contrato.setUrlContrato(s3Key);

            // Información de contenido (opcional - solo cabeceras)
            contrato.setContenido(cabecerasContrato);
            // Hashes para verificación
//            contrato.setSha256Hash(calcularHashSHA256(contenido));

            // Timestamps
            contrato.setFechaModificacion(Timestamp.from(Instant.now()));

            contratoRepository.save(contrato);
    }

    private String calcularHashSHA256(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "error-calculo-hash";
        }
    }


    /* ========== MÉTODOS ADICIONALES ÚTILES ========== */

    // Eliminar archivo de contrato de S3 y base de datos
    public void eliminarContratoS3(Integer idContratoApi) {
        try {
            ContratoApi contrato = contratoRepository.findById(idContratoApi)
                    .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

            // Eliminar de S3
            DeleteObjectRequest delObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(contrato.getUrlContrato()).build();

            s3Client.deleteObject(delObjectRequest);
            contratoRepository.delete(contrato);

        } catch (Exception e) {
            throw new RuntimeException("Error eliminando contrato: " + e.getMessage(), e);
        }
    }

    // Eliminar archivo de doc adicional de S3 y base de datos
    public void eliminarDocFileS3(Integer idDocumentacion) {
        try {
            Documentacion doc = documentacionRepository.findById(idDocumentacion)
                    .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(doc.getUrlDocumento())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            documentacionRepository.delete(doc);
        } catch (Exception e) {
            throw new RuntimeException("Error eliminando documentación: " + e.getMessage(), e);
        }
    }



    public String obtenerContenidoS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("error papu", e);
        }
    }
}