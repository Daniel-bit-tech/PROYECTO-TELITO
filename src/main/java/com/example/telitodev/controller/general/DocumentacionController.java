package com.example.telitodev.controller.general;


import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequestMapping("/documentacion")
public class DocumentacionController {

    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;

    public DocumentacionController(UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
    }

    @GetMapping("/{idDoc}")
    public String mostarVistaDocDetalle(@PathVariable Integer idDoc, Model model, Authentication auth, HttpSession session) {

        Optional<Documentacion> doc = documentacionRepository.findById(idDoc);
        if (doc.isPresent()) {
            model.addAttribute("doc", doc.get());
        } else throw new IllegalArgumentException("Documentación no encontrada");

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        return "general/docs/docDetalle";
    }

    // Vista de playground (interactivo)
    @GetMapping("/{idDoc}/playground")
    public String viewPlayground(@PathVariable Integer idDoc, Model model, Authentication auth, HttpSession session) {
        Optional<Documentacion> doc = documentacionRepository.findById(idDoc);
        if (doc.isPresent()) {
            model.addAttribute("doc", doc.get());
        } else throw new IllegalArgumentException("Documentación no encontrada");

        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        return "general/docs/playground";
    }

    /**
     * Endpoint que devuelve el JSON OpenAPI asociado
     * Consumido por Scalar (url)
     */
    @ResponseBody
    @GetMapping("/{idDoc}/openapi.json")
    public ResponseEntity<String> obtenerOpenApiSpec(@PathVariable Integer idDoc) {
        Documentacion doc = documentacionRepository.findById(idDoc)
                .orElseThrow(() -> new IllegalArgumentException("Documentación no encontrada"));

        // Si el contenido es un JSON válido guardado en BD
        if (doc.getContenido() != null) {
            System.out.println(doc.getContenido());
            String json = doc.getContenido();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(json.getBytes(StandardCharsets.UTF_8).length)
                    .body(json);
        }

        return ResponseEntity.notFound().build();
    }


    /**
     * Método helper para obtener el usuario correcto durante impersonación
     */
    private Usuario obtenerUsuarioActual(Authentication auth, HttpSession session) {
        // Verificar si hay impersonación activa
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        
        if (isImpersonating != null && isImpersonating) {
            // Durante impersonación, obtener usuario por DNI del usuario impersonado
            String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
            if (impersonatedUserDni != null) {
                Usuario impersonatedUser = usuarioRepository.findByDni(impersonatedUserDni);
                if (impersonatedUser != null) {
                    System.out.println("🎭 Documentacion DEV - Usando datos del usuario impersonado: " + impersonatedUser.getNombre());
                    return impersonatedUser;
                }
            }
        }
        
        // Sin impersonación, usar el usuario autenticado normal
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        System.out.println("👤 Documentacion DEV - Usando datos del usuario autenticado: " + usuario.getNombre());
        return usuario;
    }

}
