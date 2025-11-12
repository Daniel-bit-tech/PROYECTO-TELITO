package com.example.telitodev.service.creacionApi;

import com.example.telitodev.dto.DocGeneralDTO;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.VersionApi;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.service.DocMDService;
import com.example.telitodev.service.FileSecurityService;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.example.telitodev.service.ValidateApiDocsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class DocApiService {

    private final ValidateApiDocsService validationService;
    private final FileSecurityService fileSecurityService;
    private final S3DocsApiService s3DocsApiService;
    private final DocMDService docMDService;
    private final DocumentacionRepository documentacionRepository;

    public DocApiService(ValidateApiDocsService validationService, FileSecurityService fileSecurityService, S3DocsApiService s3DocsApiService, DocMDService docMDService, DocumentacionRepository documentacionRepository) {
        this.validationService = validationService;
        this.fileSecurityService = fileSecurityService;
        this.s3DocsApiService = s3DocsApiService;
        this.docMDService = docMDService;
        this.documentacionRepository = documentacionRepository;
    }




    public void validaryProcesarDocs(DocGeneralDTO docGeneralDto, VersionApi versionApi, List<String> errores) throws DocValidationException {
        MultipartFile[] archivos = docGeneralDto.getArchivosTecnicos();
        String[] descArchivos = docGeneralDto.getDescripcionesTecnicas();
        String[] formatoArchivos = docGeneralDto.getFormatosTecnicos();

        boolean archivoMD = false;
        for (int idx=0; idx<archivos.length; idx++) {
            MultipartFile archivo = archivos[idx];
            String desc = descArchivos[idx];
            String formato = formatoArchivos[idx];
            if (desc==null || desc.trim().length()<5 || formato == null) {
                errores.add("El archivo "+archivo.getOriginalFilename()+" no tiene formato o descripción válida.");
                continue;
            }
            Documentacion doc = new Documentacion(versionApi.getApi(),versionApi,desc,"Doc Adicional");
            doc.setFormato(detectarFormatoDoc(archivo));
            if (doc.getFormato().equals(Documentacion.FormatoDoc.MARKDOWN)) {
                if (archivoMD) throw new DocValidationException("Se detectó más de 1 archivo Markdown.");
                archivoMD = true;
            }

            try {
                String contenidoNormalizado = fileSecurityService.validarSintaxisDoc(archivo,doc.getFormato());
                if (doc.getFormato().equals(Documentacion.FormatoDoc.MARKDOWN)) {
                    doc.setVersionApi(null);
                    doc.setContenido(docMDService.extraerCabecerasJsonFlexmark(contenidoNormalizado));
                }
                s3DocsApiService.subirDocAS3(doc, archivo);
                documentacionRepository.save(doc);

            } catch (SecurityException e) {
                System.err.println(e.getMessage());
                throw new DocValidationException("Error procesando archivos de documentación: " +e.getMessage(), e);
            } catch (Exception e) {
                System.err.println("Error validando contrato: " +e.getMessage());
                throw new DocValidationException("Error procesando los archivos de documentación", e);
            }
        }
    }

    public Documentacion.FormatoDoc detectarFormatoDoc(MultipartFile archivoDoc) throws DocValidationException {
        if (archivoDoc == null || archivoDoc.isEmpty()) {
            throw new DocValidationException("No se adjuntó ningún archivo.");
        }

        String nombre = Objects.toString(archivoDoc.getOriginalFilename(), "");
        String contentType = Objects.toString(archivoDoc.getContentType(), "").toLowerCase();
        System.out.println("Validando archivo: " + nombre+" de tipo "+contentType);

        switch (contentType) {
            case "application/json":
                if (nombre.endsWith("json")) {
                    return Documentacion.FormatoDoc.JSON;
                }
                break;

            case "application/x-yaml", "application/yaml", "application/x-yml", "application/yml":
                if (nombre.endsWith("yaml") || nombre.endsWith("yml")) {
                    return Documentacion.FormatoDoc.YAML;
                }
                break;

            case "application/pdf":
                if (nombre.endsWith("pdf")) {
                    return Documentacion.FormatoDoc.PDF;
                }
                break;

            case "text/markdown", "text/x-markdown":
                if (nombre.endsWith("md")) {
                    return Documentacion.FormatoDoc.MARKDOWN;
                }
                break;
            case "application/octet-stream":
                if (nombre.endsWith(".md")) {
                    return Documentacion.FormatoDoc.MARKDOWN;
                } else if (nombre.endsWith(".pdf")) {
                    return Documentacion.FormatoDoc.PDF;
                }
                break;

            default:
                throw new DocValidationException("El archivo no tiene formato válido.");
        }

        return null;
    }





    public static class DocValidationException extends Exception {

        public DocValidationException(String message) {
            super(message);
        }

        public DocValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
