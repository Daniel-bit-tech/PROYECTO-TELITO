package com.example.telitodev.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class TextSecurityService {

    private final FileSecurityService fileSecurityService;

    public TextSecurityService(FileSecurityService fileSecurityService) {
        this.fileSecurityService = fileSecurityService;
    }

    /** Sanea (sin HTML), valida tamaño y secretos. */
    public String sanitizeRequired(String value, String logicalName, int min, int max) {
        if (value == null) throw new IllegalArgumentException(logicalName + ": Es un campo requerido.");
        String stripped = Jsoup.clean(value, Safelist.none());
        if (stripped.length() < min || stripped.length() > max) {
            throw new IllegalArgumentException(logicalName + ": Debe tener entre " + min + " y " + max + " caracteres.");
        }
        try {
            fileSecurityService.scanForSecrets(stripped, logicalName);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(logicalName + ": "+e.getMessage(),e);
        }
        return stripped;
    }


    public void validarCampo(Supplier<String> getter, Consumer<String> setter,
                              String logicalName, int min, int max,
                              List<String[]> errores) {
        try {
            setter.accept(sanitizeRequired(getter.get(), logicalName, min, max));
        } catch (IllegalArgumentException ex) {
            errores.add(ex.getMessage().split(": "));
        }
    }

}
