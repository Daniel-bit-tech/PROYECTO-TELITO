package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiCreacionDTO;
import com.example.telitodev.dto.DocGeneralDTO;
import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.ContratoApiService;
import com.example.telitodev.service.FileSecurityService;
import com.example.telitodev.service.TextSecurityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping(value = {"/dev/int", "/dev/int/misApis"})
public class CreacionApisController extends BaseController {

    private final ContratoApiService contratoApiService;
    private final DominioRepository dominioRepository;
    private final TagRepository tagRepository;
    private final ContratoRepository contratoRepository;
    private final ApiRepository apiRepository;
    private final EstadoApiRepository estadoApiRepository;
    private final VersionApiRepository versionApiRepository;

    private final FileSecurityService fileSecurityService;
    private final TextSecurityService textSecurityService;

    public CreacionApisController(ContratoApiService contratoApiService,
                                  DominioRepository dominioRepository,
                                  TagRepository tagRepository,
                                  ContratoRepository contratoRepository,
                                  ApiRepository apiRepository,
                                  EstadoApiRepository estadoApiRepository,
                                  VersionApiRepository versionApiRepository,
                                  FileSecurityService fileSecurityService,
                                  TextSecurityService textSecurityService) {
        this.contratoApiService = contratoApiService;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.contratoRepository = contratoRepository;
        this.apiRepository = apiRepository;
        this.estadoApiRepository = estadoApiRepository;
        this.versionApiRepository = versionApiRepository;
        this.fileSecurityService = fileSecurityService;
        this.textSecurityService = textSecurityService;
    }

    /* ==================== PASO 1 ==================== */

    @GetMapping("/nuevaApi")
    public String vistaCreacionApi(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        ApiCreacionDTO apiDto = new ApiCreacionDTO();
        model.addAttribute("apiDto", apiDto);
        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());
        model.addAttribute("hoy", LocalDateTime.now());
        return "desarrollador/interno/crearApi";
    }

    @PostMapping("/guardarApi")
    public String crearApi(Model model, Authentication auth, HttpSession session,
                           @ModelAttribute("apiDto") ApiCreacionDTO apiDto,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        Api apiExistente = apiRepository.findByNombreIgnoreCase(apiDto.getNombre());
        if (bindingResult.hasErrors() || apiExistente != null) {
            model.addAttribute("listaDominios", dominioRepository.findAll());
            model.addAttribute("listaTags", tagRepository.findAll());
            if (apiExistente != null) {
                model.addAttribute("duplicado", true);
                bindingResult.rejectValue("nombre", "duplicado",
                        "Ya se tiene registrada una API con ese nombre, pruebe con otro.");
            }
            return "desarrollador/interno/crearApi";
        }

        Api api = new Api(apiDto.getNombre(), apiDto.getDescripcion(),
                apiDto.getDominio(), apiDto.getTag(), apiDto.getEndpointURL());
        api.setEstadoApi(estadoApiRepository.getByEstado("Inactivo"));
        api.setUsuario(usuario);
        api.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
        apiRepository.save(api);

        redirectAttributes.addFlashAttribute("msg", "Api " + api.getNombre() + " creada exitosamente");
        return "redirect:/dev/int/" + api.getIdApi() + "/nuevaVersion";
    }

    /* ==================== PASO 2 ==================== */

    @GetMapping("/{idApi}/nuevaVersion")
    public String vistaCrearVersionApi(Model model, Authentication auth, HttpSession session,
                                       @PathVariable("idApi") Integer idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        Api api = apiRepository.findById(idApi)
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada"));

        VersionContratoDTO dto = new VersionContratoDTO();
        if (api.getVersionesApi().isEmpty()) {
            model.addAttribute("paso2", true);
            dto.setVersion("v1.0");
        } else {
            model.addAttribute("paso2", false);
        }
        dto.setIdAPI(api.getIdApi());
        dto.setNombreAPI(api.getNombre());
        dto.setEstadoVersion(VersionApi.EstadoVersion.EN_CONSTRUCCION);

        model.addAttribute("versionContratoDto", dto);
        model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
        model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
        // Indicar que por defecto NO hay errores del servidor para la validación del contrato
        model.addAttribute("hasServerErrors", false);
        return "desarrollador/interno/crearVersionApi";
    }

    @PostMapping("/guardarVersionApi")
    public String guardarVersionApi(Model model, Authentication auth, HttpSession session,
                                    @ModelAttribute VersionContratoDTO versionContratoDto,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        Api api = apiRepository.findById(versionContratoDto.getIdAPI())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la versión"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("versionContratoDto", versionContratoDto);
            model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
            model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
            // Marcar que hay errores de validación del servidor para que la plantilla los muestre
            model.addAttribute("hasServerErrors", true);
            return "desarrollador/interno/crearVersionApi";
        }

        // Validar/normalizar contrato (JSON/YAML) antes de pasarlo al servicio
        String contenidoContrato;
        log.info("[DEBUG] Entró a guardarVersionApi() para API ID={}, formato={}, tieneArchivo={}",
                versionContratoDto.getIdAPI(),
                versionContratoDto.getFormato(),
                versionContratoDto.getArchivo() != null && !versionContratoDto.getArchivo().isEmpty());
        try {
            log.info("[DEBUG] Validando contrato OpenAPI en formato {}", versionContratoDto.getFormato());
            boolean subioArchivo = versionContratoDto.getArchivo() != null
                    && !versionContratoDto.getArchivo().isEmpty();

            switch (versionContratoDto.getFormato()) {
                case JSON -> {
                    log.info("[DEBUG] -> Validando JSON: archivo={} ", subioArchivo);
                    contenidoContrato = subioArchivo
                            ? fileSecurityService.validateAndNormalizeJson(versionContratoDto.getArchivo())
                            : fileSecurityService.validateAndNormalizeJsonString(versionContratoDto.getContenido());
                }
                case YAML -> {
                    log.info("[DEBUG] -> Validando YAML: archivo={} ", subioArchivo);
                    contenidoContrato = subioArchivo
                            ? fileSecurityService.validateAndNormalizeYaml(versionContratoDto.getArchivo())
                            : fileSecurityService.validateAndNormalizeYamlString(versionContratoDto.getContenido());
                }
                default -> throw new SecurityException("Formato de contrato no soportado.");
            }

        } catch (SecurityException | IOException e) {
            log.error("[ERROR] Falló la validación de contrato: {}", e.getMessage());
            boolean subioArchivo = versionContratoDto.getArchivo() != null
                    && !versionContratoDto.getArchivo().isEmpty();

            // Marca el método activo para que el radio/visual se mantenga
            versionContratoDto.setDesdeArchivo(subioArchivo);

            // Rechaza el campo correcto para que el error aparezca al lado del input
            bindingResult.rejectValue(subioArchivo ? "archivo" : "contenido",
                    "error.contrato", e.getMessage());

            model.addAttribute("versionContratoDto", versionContratoDto);
            model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
            model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
            // Mostrar el mensaje detallado del error en el recuadro de errores del servidor
            model.addAttribute("errorBack", "Falló la validación de contrato: " + e.getMessage());
            // Indicar al template que se deben mostrar errores de servidor
            model.addAttribute("hasServerErrors", true);
            return "desarrollador/interno/crearVersionApi";
        }

        VersionApi versionApi = new VersionApi();
        versionApi.setApi(api);
        versionApi.setVersion(versionContratoDto.getVersion());
        versionApi.setFechaPublicacion(versionContratoDto.getFechaPublicacion());
        versionApi.setEstadoVersion(versionContratoDto.getEstadoVersion());
    // Persistir la versión primero para asegurarnos de tener un id válido
    versionApiRepository.save(versionApi);

    ContratoApi contratoApi = new ContratoApi();
    contratoApi.setVersionApi(versionApi);
    contratoApi.setFormato(versionContratoDto.getFormato());
    contratoApi.setContenido(contenidoContrato);
    contratoRepository.save(contratoApi);

    redirectAttributes.addFlashAttribute("msg",
        "Creaste la primera versión de tu API " + api.getNombre() + " exitosamente");
    // Redirigir directamente a Revisión Final (Paso 4)
    return "redirect:/dev/int/" + api.getIdApi() + "/versiones/" + versionApi.getIdVersion() + "/revisionFinal";


    }


    /* ==================== PASO 3 ==================== */

    @GetMapping("/{idApi}/versiones/{idVersion}/docs")
    public String vistaDocsVersionApi(Model model, Authentication auth, HttpSession session,
                                      @PathVariable("idApi") Integer idApi,
                                      @PathVariable("idVersion") Integer idVersion) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        Api api = apiRepository.findById(idApi)
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada"));
        VersionApi versionApi = versionApiRepository.findById(idVersion)
                .filter(v -> v.getApi().equals(api))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la versión solicitada"));

        model.addAttribute("paso3", versionApi.getDocumentaciones().isEmpty());
        DocGeneralDTO docGeneralDto = new DocGeneralDTO();
        docGeneralDto.setIdApi(api.getIdApi());
        docGeneralDto.setNombreApi(api.getNombre());
        docGeneralDto.setIdVersion(versionApi.getIdVersion());
        docGeneralDto.setNumeroVersion(versionApi.getVersion());
        model.addAttribute("docGeneralDto", docGeneralDto);
        return "desarrollador/interno/crearDocGeneral";
    }

    @PostMapping("/guardarDocsApi")
    public String guardarDocGeneralesApi(Model model, Authentication auth, HttpSession session,
                                         @ModelAttribute DocGeneralDTO docGeneralDto,
                                         BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        addImpersonationAttributes(model, session);

        Api api = apiRepository.findById(docGeneralDto.getIdApi())
                .filter(a -> a.getUsuario().equals(usuario))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación"));
        VersionApi versionApi = versionApiRepository.findById(docGeneralDto.getIdVersion())
                .filter(v -> v.getApi().equals(api))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("docGeneralDto", docGeneralDto);
            return "desarrollador/interno/crearDocGeneral";
        }

        // Alto nivel: sanear y validar
        try {
            docGeneralDto.setBeneficios(textSecurityService.sanitizeRequired(docGeneralDto.getBeneficios(), "Beneficios", 100, 400));
            docGeneralDto.setLimitaciones(textSecurityService.sanitizeRequired(docGeneralDto.getLimitaciones(), "Limitaciones", 100, 400));
            docGeneralDto.setFlujoFuncional(textSecurityService.sanitizeRequired(docGeneralDto.getFlujoFuncional(), "Flujo funcional", 100, 400));
            docGeneralDto.setSla(textSecurityService.sanitizeRequired(docGeneralDto.getSla(), "SLA", 100, 400));
            docGeneralDto.setCostos(textSecurityService.sanitizeRequired(docGeneralDto.getCostos(), "Costos", 100, 400));
            docGeneralDto.setEjemplosIntegracion(textSecurityService.sanitizeRequired(docGeneralDto.getEjemplosIntegracion(), "Ejemplos de integración", 100, 400));
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("beneficios", "error.alto.nivel", ex.getMessage());
            model.addAttribute("docGeneralDto", docGeneralDto);
            return "desarrollador/interno/crearDocGeneral";
        }

        Path storageRoot = Paths.get(System.getProperty("user.home"), "telitodev-storage");
        Path baseDir = storageRoot.resolve("api_" + api.getIdApi() + "_ver_" + versionApi.getIdVersion());
        try { Files.createDirectories(baseDir); } catch (Exception ignored) {}

        if (docGeneralDto.getDocumentosTecnicos() != null) {
            for (int idx = 0; idx < docGeneralDto.getDocumentosTecnicos().size(); idx++) {
                DocGeneralDTO.DocumentacionItemDTO it = docGeneralDto.getDocumentosTecnicos().get(idx);
                if (it == null || it.getFormato() == null) continue;

                try {
                    String storedName;
                    String normalizedContent = null;

                    switch (it.getFormato()) {
                        case JSON -> {
                            normalizedContent = fileSecurityService.validateAndNormalizeJson(it.getArchivo());
                            storedName = "openapi-" + UUID.randomUUID() + ".json";
                            Files.writeString(baseDir.resolve(storedName), normalizedContent, StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                        case YAML -> {
                            normalizedContent = fileSecurityService.validateAndNormalizeYaml(it.getArchivo());
                            storedName = "openapi-" + UUID.randomUUID() + ".yaml";
                            Files.writeString(baseDir.resolve(storedName), normalizedContent, StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                        case MARKDOWN -> {
                            normalizedContent = fileSecurityService.validateAndSanitizeMarkdown(it.getArchivo());
                            storedName = "doc-" + UUID.randomUUID() + ".md";
                            Files.writeString(baseDir.resolve(storedName), normalizedContent, StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                        case PDF -> {
                            fileSecurityService.validatePdf(it.getArchivo());
                            storedName = "doc-" + UUID.randomUUID() + ".pdf";
                            Files.copy(it.getArchivo().getInputStream(), baseDir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
                        }
                        default -> throw new SecurityException("Formato no soportado: " + it.getFormato());
                    }

                    Documentacion doc = new Documentacion();
                    doc.setApi(api);
                    doc.setVersionApi(versionApi);
                    doc.setFormato(it.getFormato());
                    doc.setDescripcion(it.getDescripcion());
                    doc.setTipo(it.getTipo());
                    doc.setUrlDocumento(baseDir.resolve(storedName).toString());

                    // TODO: persistir con tu repository de documentación (no incluido aquí)
                    // documentacionRepository.save(doc);

                } catch (Exception e) {
                    bindingResult.rejectValue("documentosTecnicos[" + idx + "].archivo", "error.doc", e.getMessage());
                    model.addAttribute("docGeneralDto", docGeneralDto);
                    // Mostrar detalle del error en la vista
                    model.addAttribute("errorBack", e.getMessage());
                    return "desarrollador/interno/crearDocGeneral";
                }
            }
        }

    redirectAttributes.addFlashAttribute("msg",
        "Creaste la documentación de la versión " + versionApi.getVersion() + " de tu API " + api.getNombre() + " exitosamente");
    // Después de guardar la documentación, redirigir a la vista de Revisión Final (Paso 4)
    return "redirect:/dev/int/" + api.getIdApi() + "/versiones/" + versionApi.getIdVersion() + "/revisionFinal";
    }

    /* ==================== PASO 4 - REVISIÓN FINAL ==================== */

    @GetMapping("/{idApi}/versiones/{idVersion}/revisionFinal")
    public String vistaRevisionFinal(Model model, Authentication auth, HttpSession session,
                     @PathVariable("idApi") Integer idApi,
                     @PathVariable("idVersion") Integer idVersion) {
    Usuario usuario = getCurrentUser(auth, session);
    model.addAttribute("usuario", usuario);
    addImpersonationAttributes(model, session);

    Api api = apiRepository.findById(idApi)
        .filter(a -> a.getUsuario().equals(usuario))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada"));
    VersionApi versionApi = versionApiRepository.findById(idVersion)
        .filter(v -> v.getApi().equals(api))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la versión solicitada"));

    model.addAttribute("apiObj", api);
    model.addAttribute("versionObj", versionApi);
    model.addAttribute("paso4", true);
    return "desarrollador/interno/revisionFinal";
    }
}
