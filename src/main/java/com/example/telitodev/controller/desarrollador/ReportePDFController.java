package com.example.telitodev.controller.desarrollador;
import com.example.telitodev.entity.*;
import com.example.telitodev.entity.Evidencia;
import com.example.telitodev.entity.Issue;
import com.example.telitodev.entity.IssueId;
import com.example.telitodev.repository.EvidenciaRepository;
import com.example.telitodev.repository.IssueRepository;
import com.example.telitodev.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller("desarrolladorReportePDFController")
public class ReportePDFController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/issue/reporte-pdf/{idIssue}/{idReporte}")
    public ResponseEntity<byte[]> descargarReporteIssuePdf(@PathVariable("idIssue") Integer idIssue,
                                                           @PathVariable("idReporte") Integer idReporte) {

        IssueId issueIdObject = new IssueId(idIssue, idReporte);
        Optional<Issue> issueOpt = issueRepository.findById(issueIdObject);

        if (issueOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Issue issue = issueOpt.get();
        List<Evidencia> evidencias = evidenciaRepository.findByReporteIdReporte(idReporte);


        Map<Integer, String> imagenesBase64 = new HashMap<>();
        if (issue.getComentarios() != null) {
            for (Comentario comentario : issue.getComentarios()) {
                if (comentario.getAdjuntos() != null) {
                    for (Adjunto adjunto : comentario.getAdjuntos()) {
                        String nombre = (adjunto.getNombre() != null) ? adjunto.getNombre().toLowerCase() : "";

                        if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {


                            byte[] imagenBytes = adjunto.getArchivo();

                            if (imagenBytes != null && imagenBytes.length > 0) {
                                String base64String = Base64.getEncoder().encodeToString(imagenBytes);


                                imagenesBase64.put(adjunto.getIdAdjunto(), base64String);
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("issue", issue);
        variables.put("evidencias", evidencias);
        variables.put("imagenesBase64", imagenesBase64);


        byte[] pdfBytes = pdfService.generarPdfDeHtml("reportes/issue_reporte", variables);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("reporte-issue-%d.pdf", idIssue);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}