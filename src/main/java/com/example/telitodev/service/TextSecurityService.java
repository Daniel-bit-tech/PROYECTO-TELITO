package com.example.telitodev.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class TextSecurityService {

    private final FileSecurityService fileSecurityService;

    public TextSecurityService(FileSecurityService fileSecurityService) {
        this.fileSecurityService = fileSecurityService;
    }

    /** Sanea (sin HTML), valida tamaño y secretos. */
    public String sanitizeRequired(String value, String logicalName, int min, int max) {
        if (value == null) throw new IllegalArgumentException(logicalName + " es requerido.");
        String stripped = Jsoup.clean(value, Safelist.none());
        if (stripped.length() < min || stripped.length() > max) {
            throw new IllegalArgumentException(logicalName + " debe tener entre " + min + " y " + max + " caracteres.");
        }
        fileSecurityService.scanForSecrets(stripped, logicalName);
        return stripped;
    }
}
