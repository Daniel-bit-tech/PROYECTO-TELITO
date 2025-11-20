package com.example.telitodev.controller.general;


import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.ContratoRepository;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.DocMDService;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.example.telitodev.service.creacionApi.DocApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/documentacion")
@PreAuthorize("isAuthenticated()")
public class DocumentacionController {

    private final DocMDService docMDService;

    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;
    final ContratoRepository contratoRepository;
    final ApiRepository apiRepository;
    final S3DocsApiService s3DocsApiService;

    public DocumentacionController(DocMDService docMDService, UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository, ContratoRepository contratoRepository, ApiRepository apiRepository, S3DocsApiService s3DocsApiService) {
        this.docMDService = docMDService;
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
        this.contratoRepository = contratoRepository;
        this.apiRepository = apiRepository;
        this.s3DocsApiService = s3DocsApiService;
    }

    // Vista de contrato
    @GetMapping({"/{idApi}/contrato", "/{idApi}/contrato/{idContrato}"})
    public String viewContratoApi(@PathVariable Integer idApi, @PathVariable(required = false) Integer idContrato, Model model, Authentication auth, HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = obtenerUsuarioActual(auth, session);
        model.addAttribute("usuario", usuario);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent()) {
            Api api = apiX.get();
            model.addAttribute("api", api);
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la api seleccionada");


        List<String> nombresSecs = docMDService.nombresSecsReadme(idApi);
        model.addAttribute("nombresSecs", nombresSecs);

        model.addAttribute("currentView", "contrato");

        List<ContratoApi> contratosApi = contratoRepository.findByVersionApi_Api_IdApi(idApi);
        if (contratosApi==null || contratosApi.isEmpty()) {
            model.addAttribute("contratoValido", false);
            return "general/docs/contrato";
        } else model.addAttribute("contratoValido", true);

        model.addAttribute("listaContratosApi", contratosApi);

        ContratoApi contratoApi;
        Optional<ContratoApi> contratoX = contratosApi.stream().filter(c->c.getIdContratoApi().equals(idContrato)).findFirst();
        contratoApi = contratoX.orElseGet(() -> contratosApi.get(0));
        model.addAttribute("contratoApi", contratoApi);
        try {
            model.addAttribute("scalarUrl", s3DocsApiService.generarUrlDescarga(contratoApi.getUrlContrato(), Duration.ofMinutes(5)));
        } catch (DocApiService.DocValidationException e) {
            model.addAttribute("scalarUrl", "/documentacion/"+contratoApi.getIdContratoApi()+"/openapi.json");
        }

        return "general/docs/contrato";
    }


    /** Endpoint para obtener una sección específica de doc técnica
     * @param idApi id de Api para buscar documentacion
     * @param section seccion del archivo .md a enviar
     * @return parseo de .md a html
     */
    @GetMapping("/{idApi}/{section}/.html")
    public String getSection(@PathVariable Integer idApi, @PathVariable String section, Model model) throws IOException {

        System.out.println("SECTION: " + section);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent()) {
            Api api = apiX.get();
            model.addAttribute("api", api);

            switch (section) {
                case "info":
                    model.addAttribute("api", api);
                    return "general/docs/docSecciones :: info";
                default:
                    model.addAttribute("secName" , section);

                    Map<String, String> sections = docMDService.mapeoSecsDoc(idApi);

                    model.addAttribute("secHTML" , sections.getOrDefault(section, "<p>Sección no encontrada</p>"));
                    return "general/docs/docSecciones :: sec";
            }
        }

        Map<String, String> sections = docMDService.mapeoSecsDoc(idApi);
        return sections.getOrDefault(section, "<p>Sección no encontrada</p>");
    }

    /**
     * Endpoint que devuelve el JSON OpenAPI asociado
     * Consumido por Scalar (url)
     */
    @ResponseBody
    @GetMapping("/{idContrato}/openapi.json")
    public ResponseEntity<String> obtenerOpenApiSpec(@PathVariable Integer idContrato) {
        ContratoApi contratoApi = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new IllegalArgumentException("Documentación no encontrada"));

        // Si el contenido es un JSON válido guardado en BD
        if (contratoApi.getContenido() != null) {
            String json = contratoApi.getContenido();
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
