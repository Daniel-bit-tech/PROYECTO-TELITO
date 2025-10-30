package com.example.telitodev.service.S3Services;

import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.service.DocApiService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

@Service
public class S3DocsApiService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final DocumentacionRepository documentacionRepository;
    private final DocApiService validationService;
    private final String bucketName = "my-bucket";

    public S3DocsApiService(S3Client s3Client,
                                    S3Presigner s3Presigner,
                                    DocumentacionRepository documentacionRepository,
                                    DocApiService validationService) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.documentacionRepository = documentacionRepository;
        this.validationService = validationService;
    }

    // Subir un archivo con validación
    public Documentacion uploadFile(MultipartFile file, String tipo, String descripcion,
                                    Integer idAPI, Integer idVersion) throws IOException {

        validationService.validateSingleFile(file);

        String folder = "documentaciones/"+idAPI+"/"+idVersion+"/";

        String key = generateS3Key(file.getOriginalFilename(), folder);

        // Metadata para S3
        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", file.getOriginalFilename());
        metadata.put("uploaded-at", new Date().toString());
        metadata.put("content-type", file.getContentType());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .metadata(metadata)
                .build();

        // Subir archivo
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return saveDocumentacionMetadata(file, tipo, descripcion, idAPI, idVersion, key);
    }

    // Recuperar URL firmada para descarga
    public URL getFileUrl(Integer idDocumentacion) {
        Documentacion doc = documentacionRepository.findById(idDocumentacion)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(doc.getUrlDocumento())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    // Eliminar archivo de S3 y base de datos
    public void deleteFile(Integer idDocumentacion) {
        Documentacion doc = documentacionRepository.findById(idDocumentacion)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(doc.getUrlDocumento())
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        documentacionRepository.delete(doc);
    }

    // Subir múltiples archivos con validación
    public List<Documentacion> uploadMultipleFiles(List<MultipartFile> files, String tipo,
                                                   List<String> descripciones, Integer idAPI,
                                                   Integer idVersion) throws IOException {

        // Validar lista de archivos
        validationService.validateFileList(files);
        validationService.validateDescriptions(files, descripciones);

        List<Documentacion> savedDocs = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String descripcion = descripciones.get(i);

            Documentacion doc = uploadFile(file, tipo, descripcion, idAPI, idVersion);
            savedDocs.add(doc);
        }

        return savedDocs;
    }


    // Verificar si un archivo existe
    public boolean fileExists(Integer idDocumentacion) {
        return documentacionRepository.existsById(idDocumentacion);
    }

    // Obtener información del archivo sin URL
    public Documentacion getFileInfo(Integer idDocumentacion) {
        return documentacionRepository.findById(idDocumentacion)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    // Métodos privados auxiliares
    private String generateS3Key(String originalFilename, String folder) {
        String uuid = UUID.randomUUID().toString();
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("%s/%s_%s", folder, uuid, safeFilename);
    }

    private Documentacion saveDocumentacionMetadata(MultipartFile file, String tipo,
                                                    String descripcion, Integer idAPI,
                                                    Integer idVersion, String s3Key) {
        Documentacion doc = new Documentacion();
        doc.setTipo(tipo);
        doc.setDescripcion(descripcion);
//        doc.setApi(idAPI);
//        doc.setVersionApi(idVersion);
//        doc.setFormato(validationService.getFileFormat(file.getOriginalFilename()));
        doc.setUrlDocumento(s3Key);
        doc.setFechaCreacion(new Timestamp(System.currentTimeMillis()));

        return documentacionRepository.save(doc);
    }

}