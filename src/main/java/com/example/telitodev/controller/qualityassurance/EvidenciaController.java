package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.EvidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/evidencias")
public class EvidenciaController extends BaseController {

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @GetMapping("/descargar/{id}")
    public ResponseEntity<ByteArrayResource> descargarEvidencia(@PathVariable Integer id) {
        Evidencia evidencia = evidenciaRepository.findById(id).orElse(null);

        if (evidencia == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(evidencia.getEvidencia());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + evidencia.getNombre() + "\"")
                .contentLength(evidencia.getEvidencia().length)
                .body(resource);
    }
}
