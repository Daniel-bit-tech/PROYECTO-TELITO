package com.example.telitodev.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class DocApiService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "md", "json", "yaml", "yml");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_FILE_COUNT = 5;

    public void validateSingleFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("El archivo '%s' excede el tamaño máximo de %dMB",
                            file.getOriginalFilename(), MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Validar extensión
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("Formato no permitido: '%s'. Formatos aceptados: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        // Validar tipo MIME (opcional pero recomendado)
        validateMimeType(file);
    }

    public void validateFileList(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un archivo");
        }

        // Validar cantidad de archivos
        if (files.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Máximo %d archivos permitidos", MAX_FILE_COUNT)
            );
        }

        // Validar tamaño total
        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException(
                    String.format("El tamaño total de los archivos (%dMB) excede el límite de %dMB",
                            totalSize / (1024 * 1024), MAX_TOTAL_SIZE / (1024 * 1024))
            );
        }

        // Validar cada archivo individualmente
        for (MultipartFile file : files) {
            validateSingleFile(file);
        }
    }

    public void validateDescriptions(List<MultipartFile> files, List<String> descriptions) {
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

    public String getFileFormat(String filename) {
        String extension = getFileExtension(filename);
        return switch (extension.toLowerCase()) {
            case "pdf" -> "PDF";
            case "md" -> "MARKDOWN";
            case "json" -> "JSON";
            case "yaml", "yml" -> "YAML";
            default -> extension.toUpperCase();
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        // Validaciones básicas de tipo MIME
        if (contentType != null) {
            boolean isValidMime = contentType.startsWith("application/pdf") ||
                    contentType.startsWith("text/") ||
                    contentType.equals("application/json") ||
                    contentType.equals("application/x-yaml");

            if (!isValidMime) {
                throw new IllegalArgumentException(
                        String.format("Tipo de archivo no permitido: %s", contentType)
                );
            }
        }
    }
}