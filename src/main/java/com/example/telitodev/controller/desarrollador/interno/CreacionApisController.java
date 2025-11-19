package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.example.telitodev.service.creacionApi.ContratoApiService;
import com.example.telitodev.service.FileSecurityService;
import com.example.telitodev.service.TextSecurityService;
import com.example.telitodev.service.creacionApi.DocApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import javax.print.Doc;

@Slf4j
@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping(value = {"/dev/int", "/dev/int/misApis"})
public class CreacionApisController extends BaseController {

    private final DominioRepository dominioRepository;
    private final TagRepository tagRepository;
    private final ContratoRepository contratoRepository;
    private final ApiRepository apiRepository;
    private final DocumentacionRepository documentacionRepository;
    private final EstadoApiRepository estadoApiRepository;
    private final VersionApiRepository versionApiRepository;

    private final FileSecurityService fileSecurityService;
    private final TextSecurityService textSecurityService;
    private final ContratoApiService contratoApiService;
    private final DocApiService docApiService;
    private final DocAltoNivelRepository docAltoNivelRepository;
    private final S3DocsApiService s3DocsApiService;

    public CreacionApisController(DominioRepository dominioRepository,
                                  TagRepository tagRepository,
                                  ContratoRepository contratoRepository,
                                  ApiRepository apiRepository, DocumentacionRepository documentacionRepository,
                                  EstadoApiRepository estadoApiRepository,
                                  VersionApiRepository versionApiRepository,
                                  FileSecurityService fileSecurityService,
                                  TextSecurityService textSecurityService, ContratoApiService contratoApiService, DocApiService docApiService, DocAltoNivelRepository docAltoNivelRepository, S3DocsApiService s3DocsApiService) {
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.contratoRepository = contratoRepository;
        this.apiRepository = apiRepository;
        this.documentacionRepository = documentacionRepository;
        this.estadoApiRepository = estadoApiRepository;
        this.versionApiRepository = versionApiRepository;
        this.fileSecurityService = fileSecurityService;
        this.textSecurityService = textSecurityService;
        this.contratoApiService = contratoApiService;
        this.docApiService = docApiService;
        this.docAltoNivelRepository = docAltoNivelRepository;
        this.s3DocsApiService = s3DocsApiService;
    }

    /* ==================== PASO 1 - Crear por primera vez ==================== */
    @PostMapping("/guardarApi")
    @ResponseBody
    public ResponseEntity<?> crearApi(Authentication auth, HttpSession session,
                                   @RequestBody @Valid ApiCreacionDTO apiDto,
                                   BindingResult bindingResult, HttpServletRequest request) {

        Usuario usuario = getCurrentUser(auth, session);

        Api apiExistente = apiRepository.findByNombreIgnoreCase(apiDto.getNombre());
        if (apiExistente != null) {
            bindingResult.rejectValue("nombre", "duplicado",
                    "Ya se tiene registrada una API con ese nombre, pruebe con otro.");
        }
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        Api api = new Api(apiDto.getNombre(), apiDto.getDescripcion(), apiDto.getEndpointURL());
        api.setDominio(new Dominio(apiDto.getIdDominio()));
        api.setTag(new Tag(apiDto.getIdTag()));
        api.setEstadoApi(estadoApiRepository.getByEstado("Inactivo"));
        api.setUsuario(usuario);
        api.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
        apiRepository.save(api);

        VersionApi versionInicial = new VersionApi("v1.0", VersionApi.EstadoVersion.EN_CONSTRUCCION, api);
        versionInicial.setFechaPublicacion(LocalDate.now());
        versionApiRepository.save(versionInicial);

        // Devolver JSON de éxito
        return ResponseEntity.ok(Map.of(
                "msg", "Api " + api.getNombre() + " creada exitosamente",
                "idApi", api.getIdApi()
        ));
    }

    @PostMapping("/editarApi")
    public String editarApi(Authentication auth, HttpSession session,
                            @ModelAttribute @Valid ApiCreacionDTO apiDto, BindingResult bindingResult,
                            Model model) {

        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Api api = apiRepository.findById(apiDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo encontrar la API solicitada."));

        apiDto.setNombre(api.getNombre());
        if (bindingResult.hasErrors()) {
            model.addAttribute("listaDominios", dominioRepository.findAll());
            model.addAttribute("listaTags", tagRepository.findAll());

            return "desarrollador/interno/gestionApiBase";
        }

        api.setDominio(new Dominio(apiDto.getIdDominio()));
        api.setTag(new Tag(apiDto.getIdTag()));
        api.setDescripcion(apiDto.getDescripcion());
        api.setEndpointUrl(apiDto.getEndpointURL());

        apiRepository.save(api);
//        redirectAttributes.addFlashAttribute("msg", "Api " + api.getNombre() + " editado exitosamente");

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());
        return "redirect:/dev/int/misApis/"+api.getIdApi();
    }

    /* ==================== PASO 2 ==================== */
    @PostMapping("/guardarVersionApi")
    @ResponseBody
    public ResponseEntity<?> guardarVersionApi(Authentication auth, HttpSession session,
                                    @ModelAttribute @Valid VersionContratoDTO versionContratoDto,
                                    BindingResult bindingResult) {
        Map<String,Object> response = new HashMap<>();

        Usuario usuario = getCurrentUser(auth, session);

        Api api = apiRepository.findById(versionContratoDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la versión."));

        Boolean versionExiste = versionApiRepository.existsByVersionAndApi_IdApi(versionContratoDto.getVersion(), api.getIdApi());
        if (versionExiste) {
            bindingResult.rejectValue("version", "error.version", "Una versión con este nombre ya existe");
        }

        if (bindingResult.hasErrors()) {
            Map<String,String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            contratoApiService.validarYProcesarContrato(versionContratoDto, api);

            response.put("success", true);
            response.put("message", "Contrato actualizado correctamente");
            response.put("idApi", api.getIdApi());
            response.put("idVersion", versionContratoDto.getIdVersion());
            return ResponseEntity.ok(response);

        } catch (ContratoApiService.ContratoValidationException e) {
            response.put("success", false);
            response.put("errors", Map.of("contrato", "Error procesando el archivo: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", Map.of("contrato", "Error procesando el archivo."));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/editarContratoVersionApi")
    @ResponseBody
    public ResponseEntity<?> reemplazarContratoVersionApi(Authentication auth, HttpSession session,
                                               @ModelAttribute @Valid VersionContratoDTO versionContratoDto,
                                               BindingResult bindingResult) {
        Map<String,Object> response = new HashMap<>();

        Usuario usuario = getCurrentUser(auth, session);

        Api api = apiRepository.findById(versionContratoDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo encontrar la Api para está versión."));

        VersionApi versionApi = null;
        if (versionContratoDto.getIdVersion()!=null) {
            versionApi = versionApiRepository.findById(versionContratoDto.getIdVersion())
                    .filter(v -> v.getApi().equals(api))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo encontrar la versión."));
        } else {
            bindingResult.rejectValue("idVersion", "error.idVersion", "No se pudo encontrar la versión.");
        }

        if (bindingResult.hasErrors()) {
            Map<String,String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            contratoApiService.validarYProcesarContrato(versionContratoDto, api);

            response.put("success", true);
            response.put("message", "Versión creada correctamente");
            response.put("idApi", api.getIdApi());
            response.put("idVersion", versionContratoDto.getIdVersion());
            return ResponseEntity.ok(response);

        } catch (ContratoApiService.ContratoValidationException e) {
            response.put("success", false);
            response.put("errors", Map.of("contrato", "Error procesando el archivo: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", Map.of("contrato", "Error procesando el archivo."));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/estadoVersion")
    public String editarEstadoVersionApi(Authentication auth, HttpSession session,
                                         @RequestParam Integer idVersion, @RequestParam String estadoVersion,
                                         RedirectAttributes redirectAttributes) {

        Usuario usuario = getCurrentUser(auth, session);

        VersionApi version = versionApiRepository.findById(idVersion)
                .filter(v -> v.getApi().getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo encontrar la Version solicitada."));

        try {
            VersionApi.EstadoVersion nuevoEstado = VersionApi.EstadoVersion.valueOf(estadoVersion);
            version.setEstadoVersion(nuevoEstado);
            versionApiRepository.save(version);

            redirectAttributes.addFlashAttribute("toastMessage", "Estado actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Estado inválido: " + estadoVersion);
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/dev/int/misApis/"+version.getApi().getIdApi()+"/versiones?idVersion="+version.getIdVersion();
    }

    /* ==================== PASO 3 ==================== */
    @PostMapping("/guardarDocsApi")
    @ResponseBody
    public ResponseEntity<?> guardarDocsAdicionalesApi(Authentication auth, HttpSession session,
                                                     @ModelAttribute DocAdicionalDTO requestDto) {
        Map<String, Object> response = new HashMap<>();

        Usuario usuario = getCurrentUser(auth, session);

        Api api = apiRepository.findById(requestDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación"));
        VersionApi versionApi = versionApiRepository.findById(requestDto.getIdVersion())
                .filter(v -> v.getApi().equals(api))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación"));

        try {
            if (requestDto.getFiles() != null) {
                List<MultipartFile> archivos = requestDto.getFiles();
                List<String> descArchivos = requestDto.getDescriptions();
                List<String> formatoArchivos = requestDto.getFormatos();

                List<String> erroresDoc = new ArrayList<>();

                if (descArchivos!=null&&descArchivos.size()==archivos.size() && formatoArchivos!=null&&formatoArchivos.size()==archivos.size()) {
                    if (requestDto.getIdVersion() == 0) {
                        requestDto.setIdVersion(null);
                    }
//                    docApiService.validaryProcesarDocs(requestDto, versionApi, erroresDoc);
                } else throw new DocApiService.DocValidationException("La cantidad de archivos recibidos ("+archivos.size()+") no coincide con la cantidad de descripciones o formatos.");

                response.put("success", true);
                response.put("message", "Documentación guardada correctamente");
                return ResponseEntity.ok(response);

            } else throw new DocApiService.DocValidationException("Debe enviar al menos 1 archivo de documentación adicional");
        } catch (DocApiService.DocValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/eliminarDoc/{idDoc}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDoc(Authentication auth, HttpSession session,
                                                           @PathVariable Integer idDoc) {

        Map<String, Object> response = new HashMap<>();
        Usuario usuario = getCurrentUser(auth, session);

        Documentacion doc = documentacionRepository.findById(idDoc)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento no encontrado"));

        // Validar que el doc pertenece al usuario
        if (!doc.getApi().getUsuario().equals(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para eliminar este documento");
        }

        try {
            System.out.println("Intentando eliminar el Documento "+idDoc);
//            s3DocsApiService.eliminarDocFileS3(idDoc);
//            documentacionRepository.delete(doc);
            response.put("success", true);
            response.put("message", "Documentación eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error eliminando documentación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /* ==================== PASO 4 - Doc Necesaria de Api ==================== */
    @PostMapping("/guardarDocMD")
    @ResponseBody
    public ResponseEntity<?> guardarMarkdownTecnico(Authentication auth, HttpSession session,
                                                    @Valid @ModelAttribute DocMDDTO docMdDto, BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        Usuario usuario = getCurrentUser(auth, session);

        Api api = apiRepository.findById(docMdDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada"));

        if (bindingResult.hasErrors()) {
            Map<String,String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Documentacion docMDX = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(docMdDto.getIdApi(), Documentacion.FormatoDoc.MARKDOWN, "Documentación técnica");

            if (docMDX != null) {
                // si existe doc en db, realmente no importa el id, pero validamos
                if (!docMDX.getIdDocumentacion().equals(docMdDto.getIdDoc())) {
                    //existe doc en db y mandaron id, coinciden. Usare info de la db
                    docApiService.validarYProcesarMDTecnico(docMdDto.getMdFile(), docMDX);

                } else throw new DocApiService.DocValidationException("No se pudo encontrar documento Readme.");
            } else {
                //no existe en db, crear nuevo
                Documentacion readMe = new Documentacion(api,null,"Documentación técnica","General");
                readMe.setFormato(Documentacion.FormatoDoc.MARKDOWN);
                if (docMdDto.getIdDoc()==null || docMdDto.getIdDoc()==0) {
                    docApiService.validarYProcesarMDTecnico(docMdDto.getMdFile(),readMe);
                } else throw new DocApiService.DocValidationException("No se pudo encontrar el documento Readme.");

            }

            response.put("success", true);
            response.put("message", "Documentación guardada correctamente");
            return ResponseEntity.ok(response);

        } catch (DocApiService.DocValidationException e) {
            response.put("success", false);
            response.put("errors", Map.of("readme", "Error procesando el archivo Readme: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error guardando documento Readme: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/guardarDocAltoNivel")
    @ResponseBody
    public ResponseEntity<?> guardarDocAltoNivel(Authentication auth, HttpSession session,
              @Valid @ModelAttribute DocAltoNivelDTO docAltoNivelDto, BindingResult bindingResult) {

    Map<String, Object> response = new HashMap<>();

    Usuario usuario = getCurrentUser(auth, session);

    Api api = apiRepository.findById(docAltoNivelDto.getIdApi())
        .filter(a -> a.getUsuario().equals(usuario))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada"));

        if (bindingResult.hasErrors()) {

            return ResponseEntity.badRequest().body(response);
        }

        response.put("success", true);
        response.put("message", "Documentación guardada correctamente");
        return ResponseEntity.ok(response);

    }
}
