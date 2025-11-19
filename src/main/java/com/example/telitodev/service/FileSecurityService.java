package com.example.telitodev.service;

import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.Documentacion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FileSecurityService {

    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5MB
    private static final Pattern NULL_BYTES = Pattern.compile("\u0000");
    private static final Pattern HTML_DANGEROUS = Pattern.compile("(?i)<\\s*script|on\\w+\\s*=");

    // Patrones típicos de secretos
    private static final List<Pattern> SECRET_PATTERNS = List.of(
            Pattern.compile("(?i)BEGIN\\s+PRIVATE\\s+KEY"),
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("(?i)aws_secret_access_key\\s*[:=]\\s*['\\\"]?[A-Za-z0-9/+=]{40}"),
            Pattern.compile("(?i)(x-api-key|api[_-]?key|token|secret|password)\\s*[:=]\\s*['\\\"][^'\\\"]+['\\\"]"),
            Pattern.compile("(?i)jdbc:[a-z]+://[^\\s]+"),
            Pattern.compile("eyJ[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}") // JWT
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String validarSintaxisContrato(VersionContratoDTO versionContratoDto) throws SecurityException {

        String contenidoContrato;
        switch (versionContratoDto.getFormato()) {
            case JSON -> {
                contenidoContrato = versionContratoDto.isDesdeArchivo()
                        ? validateAndNormalizeJson(versionContratoDto.getContrato())
                        : validateAndNormalizeJsonString(versionContratoDto.getContenido());
            }
            case YAML -> {
                contenidoContrato = versionContratoDto.isDesdeArchivo()
                        ? validateAndNormalizeYaml(versionContratoDto.getContrato())
                        : validateAndNormalizeYamlString(versionContratoDto.getContenido());
            }
            default -> throw new SecurityException("Formato de contrato no soportado.");
        }
        return contenidoContrato;
    }

    public String validarSintaxisDoc(MultipartFile archivoDoc, Documentacion.FormatoDoc formato) throws SecurityException {
        String contenidoDoc = null;
        switch (formato) {
            case JSON -> {
                contenidoDoc = validateAndNormalizeJson(archivoDoc);
            }
            case YAML -> {
                contenidoDoc = validateAndNormalizeYaml(archivoDoc);
            }
            case PDF -> {
                validatePdf(archivoDoc);
            }
            case MARKDOWN -> {
                contenidoDoc = validateAndSanitizeMarkdown(archivoDoc);
            }
            default -> throw new SecurityException("Formato de documento no soportado.");
        }
        return contenidoDoc;
    }


    /* ========== Validación común ========== */
    public void validateCommon(MultipartFile file, String logicalName) throws SecurityException, IOException {
        if (file == null || file.isEmpty())
            throw new SecurityException("Debe adjuntar un archivo " + logicalName + " con contenido.");
        if (file.getSize() > MAX_BYTES)
            throw new SecurityException("El archivo "+logicalName + " excede el tamaño permitido.");
        String contentType = Objects.toString(file.getContentType(), "").toLowerCase();
        String nombre = Objects.toString(file.getOriginalFilename(),"");

        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            byte[] head = is.readNBytes(8);
            if (!isLikelyText(head) && !"application/pdf".equalsIgnoreCase(Objects.toString(file.getContentType(), ""))) {
                throw new SecurityException("Formato binario inesperado en el archivo" + logicalName + ".");
            }
        }
    }
    private boolean isLikelyText(byte[] head) {
        for (byte b : head) {
            int v = b & 0xFF;
            if (v < 0x09 && v != 0x00) return false;
        }
        return true;
    }

    /* ========== JSON / YAML desde archivo ========== */
    public String validateAndNormalizeJson(MultipartFile json) throws SecurityException {
        try {
            validateCommon(json, "JSON");
            String content = new String(json.getBytes(), StandardCharsets.UTF_8);
            return validateAndNormalizeJsonString(content);
        } catch (IOException e) {
            throw new SecurityException("Error leyendo archivo JSON: " + e.getMessage(), e);
        }
    }

    public String validateAndNormalizeYaml(MultipartFile yamlFile) throws SecurityException {
        try {
            validateCommon(yamlFile, "YAML");
            String content = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
            return validateAndNormalizeYamlString(content);
        } catch (IOException e) {
            throw new SecurityException("Error leyendo archivo YAML: " + e.getMessage(), e);
        }
    }

    /* ========== JSON / YAML desde String (para “pegar contenido”) ========== */
    public String validateAndNormalizeJsonString(String raw) {
        if (raw == null || raw.isBlank()) throw new SecurityException("JSON vacío.");
        basicStringChecks(raw, "JSON");
        try {
            JsonNode node = objectMapper.readTree(raw);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new SecurityException("JSON inválido: " + e.getMessage());
        }
    }

    public String validateAndNormalizeYamlString(String raw) {
        if (raw == null || raw.isBlank()) throw new SecurityException("YAML vacío.");
        basicStringChecks(raw, "YAML");
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setAllowDuplicateKeys(false);
            opts.setMaxAliasesForCollections(50);
            opts.setCodePointLimit(5_000_000);
            Yaml yaml = new Yaml(new SafeConstructor(opts));
            Object data = yaml.load(raw);
            return new Yaml().dump(data);
        } catch (Exception e) {
            throw new SecurityException("YAML inválido o peligroso: " + e.getMessage());
        }
    }

    /* ========== Markdown ========== */
    public String validateAndSanitizeMarkdown(MultipartFile md) throws SecurityException {
        try {
            validateCommon(md, "Markdown");
            String content = new String(md.getBytes(), StandardCharsets.UTF_8);
            basicStringChecks(content, "Markdown");
            if (HTML_DANGEROUS.matcher(content).find()) {
                throw new SecurityException("Markdown contiene HTML/atributos potencialmente peligrosos.");
            }
            return content;
        } catch (Exception e) {
            throw new SecurityException("No se pudo procesar el archivo Markdown: "+md.getOriginalFilename(), e);
        }
    }

    /* ========== PDF (PDFBox 3 compatible) ========== */
    public void validatePdf(MultipartFile pdf) throws SecurityException {

        try (PDDocument doc = PDDocument.load(pdf.getInputStream())) {
            validateCommon(pdf, "PDF");
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            // getOpenAction() devuelve PDestinationOrAction en PDFBox 3.x -> usamos var
            var openActionAny = catalog.getOpenAction();
            if (openActionAny instanceof PDActionJavaScript) {
                throw new SecurityException("El PDF contiene JavaScript en OpenAction.");
            }

            // Adjuntos embebidos
            if (catalog.getNames() != null &&
                    catalog.getNames().getEmbeddedFiles() != null &&
                    catalog.getNames().getEmbeddedFiles().getNames() != null &&
                    !catalog.getNames().getEmbeddedFiles().getNames().isEmpty()) {
                throw new SecurityException("El PDF contiene archivos embebidos (no permitido).");
            }

            // Escaneo de secretos en texto
            String text = new PDFTextStripper().getText(doc);
            scanForSecrets(text, "PDF");
        } catch (Exception e) {
            throw new SecurityException("Archivo PDF "+pdf.getOriginalFilename()+" inválido o no legible",e);
        }
    }

    /* ========== Utilidades internas ========== */
    private void basicStringChecks(String content, String logicalName) {
        if (NULL_BYTES.matcher(content).find())
            throw new SecurityException(logicalName + " contiene null bytes.");
        scanForSecrets(content, logicalName);
    }

    public void scanForSecrets(String text, String logicalName) throws SecurityException {
        for (Pattern p : SECRET_PATTERNS) {
            if (p.matcher(text).find()) {
                throw new SecurityException("Posible fuga de secretos en " + logicalName + ". Retira credenciales/tokens.");
            }
        }
    }
}
