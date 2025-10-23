package com.example.telitodev.controller.qualityassurance;


import com.example.telitodev.entity.Evidencia;
import com.example.telitodev.entity.Reporte;
import com.example.telitodev.repository.EvidenciaRepository;
import com.example.telitodev.repository.ReporteRepository;
import com.example.telitodev.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/qa/reportes")
@PreAuthorize("hasAnyRole('QA', 'SUPERADMIN')")
public class ReporteQAPDFController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/pdf/{idReporte}")
    public ResponseEntity<byte[]> descargarReporteQAPdf(@PathVariable("idReporte") Integer idReporte) {


        Optional<Reporte> reporteOpt = reporteRepository.findById(idReporte);
        if (reporteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Reporte reporte = reporteOpt.get();


        List<Evidencia> evidencias = evidenciaRepository.findByReporteIdReporte(idReporte);


        Map<String, Object> variables = new HashMap<>();
        variables.put("reporte", reporte);
        variables.put("evidencias", evidencias);

        byte[] pdfBytes = pdfService.generarPdfDeHtml("reportes/reporte_qa", variables);

        if (pdfBytes == null) {
            return ResponseEntity.internalServerError().body("Error generando el PDF".getBytes());
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("reporte-qa-%d.pdf", idReporte);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}