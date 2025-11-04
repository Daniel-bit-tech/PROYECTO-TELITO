package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Adjunto;
import com.example.telitodev.repository.AdjuntoRepository;
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
@RequestMapping("/archivos")
public class ArchivoController extends BaseController {

    @Autowired
    private AdjuntoRepository adjuntoRepository;

    @GetMapping("/verAdjunto/{idAdjunto}")
    public ResponseEntity<ByteArrayResource> serveFile(@PathVariable Integer idAdjunto) {
        // Buscar el archivo adjunto en la base de datos
        Adjunto adjunto = adjuntoRepository.findById(idAdjunto).orElse(null);

        if (adjunto == null) {
            // Si el archivo no existe, retornar un error
            return ResponseEntity.notFound().build();
        }

        System.out.println("TU DESCARGA ESTÁ EMPEZANDO...");

        // Obtener los bytes del archivo almacenado
        byte[] archivoBytes = adjunto.getArchivo();
        ByteArrayResource resource = new ByteArrayResource(archivoBytes);

        System.out.println("DESCARGANDO....");

        // Configurar la respuesta para la descarga
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + adjunto.getNombre() + "\"")
                .contentLength(archivoBytes.length)
                .body(resource);
    }
}