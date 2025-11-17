package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiCreacionDTO;
import com.example.telitodev.dto.DocGeneralDTO;
import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.creacionApi.ContratoApiService;
import com.example.telitodev.service.FileSecurityService;
import com.example.telitodev.service.TextSecurityService;
import com.example.telitodev.service.creacionApi.DocApiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping(value = {"/dev/int", "/dev/int/misApis"})
public class CreacionApisController extends BaseController {

    private final DominioRepository dominioRepository;
    private final TagRepository tagRepository;
    private final ContratoRepository contratoRepository;
    private final ApiRepository apiRepository;
    private final EstadoApiRepository estadoApiRepository;
    private final VersionApiRepository versionApiRepository;

    private final FileSecurityService fileSecurityService;
    private final TextSecurityService textSecurityService;
    private final ContratoApiService contratoApiService;
    private final DocApiService docApiService;
    private final DocAltoNivelRepository docAltoNivelRepository;

    public CreacionApisController(DominioRepository dominioRepository,
                                  TagRepository tagRepository,
                                  ContratoRepository contratoRepository,
                                  ApiRepository apiRepository,
                                  EstadoApiRepository estadoApiRepository,
                                  VersionApiRepository versionApiRepository,
                                  FileSecurityService fileSecurityService,
                                  TextSecurityService textSecurityService, ContratoApiService contratoApiService, DocApiService docApiService, DocAltoNivelRepository docAltoNivelRepository) {
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.contratoRepository = contratoRepository;
        this.apiRepository = apiRepository;
        this.estadoApiRepository = estadoApiRepository;
        this.versionApiRepository = versionApiRepository;
        this.fileSecurityService = fileSecurityService;
        this.textSecurityService = textSecurityService;
        this.contratoApiService = contratoApiService;
        this.docApiService = docApiService;
        this.docAltoNivelRepository = docAltoNivelRepository;
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

        List<VersionApi> versionesExistentes = api.getVersionesApi();
        if (versionesExistentes.stream().anyMatch(
                ver -> ver.getVersion().equals(versionContratoDto.getVersion()))) {
            bindingResult.rejectValue("version", "error.version", " Una versión con este nombre ya existe");
            model.addAttribute("errorBack", "No se pudo crear la versión "+versionContratoDto.getVersion());
        }

        if (bindingResult.hasErrors()) {
            versionContratoDto.setIdAPI(api.getIdApi());
            versionContratoDto.setNombreAPI(api.getNombre());
            model.addAttribute("versionContratoDto", versionContratoDto);
            model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
            model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
            model.addAttribute("hasServerErrors", true);
            return "desarrollador/interno/crearVersionApi";
        }

        //log.info("[DEBUG] Entro a guardarVersionApi() para API ID={}, formato={}, tieneArchivo={}",

        try {

            contratoApiService.validarYProcesarContrato(versionContratoDto, api);

        } catch (ContratoApiService.ContratoValidationException e) {
        //    log.error("[ERROR] Fallo la validacion de contrato: {}", e.getMessage());

            boolean subioArchivo = versionContratoDto.getArchivo() != null
                    && !versionContratoDto.getArchivo().isEmpty();
            // Rechaza el campo correcto para que el error aparezca al lado del input
            bindingResult.rejectValue(subioArchivo ? "archivo" : "contenido",
                    "error.contrato", e.getMessage());
            model.addAttribute("errorBack", "Falló la validación de contrato: " + e.getMessage());
            return "desarrollador/interno/crearVersionApi";

        } catch (Exception e) {
            //log.error("[ERROR] Fallo la validacion de contrato: {}", e.getMessage());
            model.addAttribute("errorBack", "Falló la validación de contrato");
            return "desarrollador/interno/crearVersionApi";

        } finally {
            // Marca el método activo para que el radio/visual se mantenga
            boolean subioArchivo = versionContratoDto.getArchivo() != null
                    && !versionContratoDto.getArchivo().isEmpty();
            versionContratoDto.setDesdeArchivo(subioArchivo);
            versionContratoDto.setNombreAPI(api.getNombre());

            model.addAttribute("versionContratoDto", versionContratoDto);
            model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
            model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
            // Indicar al template que se deben mostrar errores de servidor
            model.addAttribute("hasServerErrors", true);
        }

    redirectAttributes.addFlashAttribute("success", "Creaste la versión "+versionContratoDto.getVersion()+" de tu API " + api.getNombre() + " exitosamente");
//    return "redirect:/dev/int/" + api.getIdApi() + "/versiones/" + versionApi.getIdVersion() + "/revisionFinal";
    return "redirect:/dev/int/" + api.getIdApi() + "/versiones/" + versionContratoDto.getIdVersion() + "/docs";
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
    public String guardarDocGeneralesApi(@Valid @ModelAttribute("docGeneralDto") DocGeneralDTO docGeneralDto, BindingResult bindingResult,
                                         Model model, Authentication auth, HttpSession session,
                                         RedirectAttributes redirectAttributes) {
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
            docGeneralDto.setNombreApi(api.getNombre());
            docGeneralDto.setNumeroVersion(versionApi.getVersion());
            model.addAttribute("errorBack", "No se pudo guardar, verifique errores");
            model.addAttribute("docGeneralDto", docGeneralDto);
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                System.err.println("Campo: " + fieldError.getField()
                        + " | Valor rechazado: " + fieldError.getRejectedValue()
                        + " | Mensaje: " + fieldError.getDefaultMessage());
            }
            return "desarrollador/interno/crearDocGeneral";
        }

        // Alto nivel: sanear y valida
        List<String[]> errores = new ArrayList<>();
        textSecurityService.validarCampo(docGeneralDto::getBeneficios, docGeneralDto::setBeneficios, "beneficios", 100, 400, errores);
        textSecurityService.validarCampo(docGeneralDto::getLimitaciones, docGeneralDto::setLimitaciones, "limitaciones", 100, 400, errores);
        textSecurityService.validarCampo(docGeneralDto::getFlujoFuncional, docGeneralDto::setFlujoFuncional, "flujoFuncional", 100, 400, errores);
        textSecurityService.validarCampo(docGeneralDto::getSla, docGeneralDto::setSla, "sla", 100, 400, errores);
        textSecurityService.validarCampo(docGeneralDto::getCostos, docGeneralDto::setCostos, "costos", 100, 400, errores);
        textSecurityService.validarCampo(docGeneralDto::getEjemplosIntegracion, docGeneralDto::setEjemplosIntegracion, "ejemplosIntegracion", 100, 400, errores);
        if (!errores.isEmpty()) {
            for (String[] err : errores) {
                bindingResult.rejectValue(err[0], "error.alto.nivel", err[1]);
            }
            docGeneralDto.setNombreApi(api.getNombre());
            docGeneralDto.setIdApi(api.getIdApi());
            docGeneralDto.setNumeroVersion(versionApi.getVersion());
            model.addAttribute("docGeneralDto", docGeneralDto);
            model.addAttribute("errorBack", "Error en documentación de alto nivel");
            model.addAttribute("paso3", versionApi.getDocumentaciones().isEmpty());
            return "desarrollador/interno/crearDocGeneral";
        } else {
            doc_alto_nivel docAltoNivel = new doc_alto_nivel();
            docAltoNivel.setApi(api);
            docAltoNivel.setBeneficios(docGeneralDto.getBeneficios());
            docAltoNivel.setLimitaciones(docGeneralDto.getLimitaciones());
            docAltoNivel.setFlujoFuncional(docGeneralDto.getFlujoFuncional());
            docAltoNivel.setSla(docGeneralDto.getSla());
            docAltoNivel.setCostos(docGeneralDto.getCostos());
            docAltoNivel.setEjemplosIntegracion(docGeneralDto.getEjemplosIntegracion());
//            docAltoNivelRepository.save(docAltoNivel);
        }

        // Doc técnica: validar y guardar
        try {
            if (docGeneralDto.getArchivosTecnicos() != null) {
                MultipartFile[] archivos = docGeneralDto.getArchivosTecnicos();
                String[] descArchivos = docGeneralDto.getDescripcionesTecnicas();
                String[] formatoArchivos = docGeneralDto.getFormatosTecnicos();

                List<String> erroresDoc = new ArrayList<>();

                if (descArchivos!=null&&descArchivos.length==archivos.length && formatoArchivos!=null&&formatoArchivos.length==archivos.length) {
                    docApiService.validaryProcesarDocs(docGeneralDto, versionApi, erroresDoc);
                } else throw new DocApiService.DocValidationException("La cantidad de archivos recibidos ("+archivos.length+") no coincide con la cantidad de descripciones o formatos.");

            } else throw new DocApiService.DocValidationException("Debe enviar al menos 1 archivo de documentación adicional");
        } catch (DocApiService.DocValidationException e) {
            //log.error("[ERROR] Fallo la validacion de docs: {}", e.getMessage());
            model.addAttribute("errorBack", e.getMessage());
            return "desarrollador/interno/crearDocGeneral";
        } finally {
            docGeneralDto.setNombreApi(api.getNombre());
            docGeneralDto.setIdApi(api.getIdApi());
            docGeneralDto.setNumeroVersion(versionApi.getVersion());
            model.addAttribute("docGeneralDto", docGeneralDto);
            model.addAttribute("paso3", versionApi.getDocumentaciones().isEmpty());
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
