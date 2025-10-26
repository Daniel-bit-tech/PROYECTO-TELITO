package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiCreacionDTO;
import com.example.telitodev.dto.DocGeneralDTO;
import com.example.telitodev.dto.VersionContratoDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.ContratoApiService;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping(value = {"/dev/int","/dev/int/misApis"})
public class GestionApisController extends BaseController {

    private final ContratoApiService contratoApiService;
//    private final S3DocsApiService s3DocsApiService;

    final ApiRepository apiRepository;
    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;
    final EstadoApiRepository estadoApiRepository;
    final VersionApiRepository versionApiRepository;
    final ProyectoRepository proyectoRepository;
    final ContratoRepository contratoRepository;

    public GestionApisController(ContratoApiService contratoApiService, ApiRepository apiRepository, UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository, DominioRepository dominioRepository, TagRepository tagRepository, EstadoApiRepository estadoApiRepository, VersionApiRepository versionApiRepository, ProyectoRepository proyectoRepository, ContratoRepository contratoRepository) {
        this.contratoApiService = contratoApiService;
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.estadoApiRepository = estadoApiRepository;
        this.versionApiRepository = versionApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.contratoRepository = contratoRepository;
    }

    @GetMapping("/nuevaApi")    //Paso 1
    public String vistaCreacionApi(Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        ApiCreacionDTO apiDto = new ApiCreacionDTO();
        model.addAttribute("apiDto", apiDto);

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());
        model.addAttribute("hoy", LocalDateTime.now());

        return "desarrollador/interno/crearApi";
    }

    @PostMapping("/guardarApi")
    public String crearApi(Model model, Authentication auth, HttpSession session, @ModelAttribute("apiDto") ApiCreacionDTO apiDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Api apiExistente = apiRepository.findByNombreIgnoreCase(apiDto.getNombre());
        if (bindingResult.hasErrors() || apiExistente != null) {
            model.addAttribute("listaDominios", dominioRepository.findAll());
            model.addAttribute("listaTags", tagRepository.findAll());
            if (apiExistente != null) {
                model.addAttribute("duplicado", true);
                bindingResult.rejectValue("nombre", "duplicado","Ya se tiene registrada una API con ese nombre, pruebe con otro.");
            }
            return "desarrollador/interno/crearApi";
        }

        Api api = new Api(apiDto.getNombre(), apiDto.getDescripcion(), apiDto.getDominio(), apiDto.getTag(), apiDto.getEndpointURL());
        api.setEstadoApi(estadoApiRepository.getByEstado("Inactivo"));
        api.setUsuario(usuario);
        api.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));

        apiRepository.save(api);

        redirectAttributes.addFlashAttribute("msg", "Api "+api.getNombre()+" creada exitosamente");

        return "redirect:/dev/int/"+api.getIdApi()+"/nuevaVersion";
    }

    @GetMapping("/{idApi}/nuevaVersion")    //Paso 2 o acaso una nueva versión
    public String vistaCrearVersionApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") Integer idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();
            model.addAttribute("paso2", false);
            if (api.getVersionesApi().isEmpty()) {
                model.addAttribute("paso2", true);
            }

            VersionContratoDTO versionContratoDto = new VersionContratoDTO();
            versionContratoDto.setIdAPI(api.getIdApi());
            versionContratoDto.setNombreAPI(api.getNombre());
            versionContratoDto.setEstadoVersion(VersionApi.EstadoVersion.EN_CONSTRUCCION);
            model.addAttribute("versionContratoDto", versionContratoDto);
            model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
            model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada");


        return "desarrollador/interno/crearVersionApi";
    }

    @PostMapping("/guardarVersionApi")
    public String guardarVersionApi(Model model, Authentication auth, HttpSession session, @ModelAttribute VersionContratoDTO versionContratoDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(versionContratoDto.getIdAPI());
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();
            versionContratoDto.setNombreAPI(api.getNombre());

            if (bindingResult.hasErrors()) {
                versionContratoDto.setIdAPI(api.getIdApi());
                versionContratoDto.setNombreAPI(api.getNombre());
                model.addAttribute("versionContratoDto", versionContratoDto);
                model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
                model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
                return "desarrollador/interno/crearVersionApi";
            }

            // Validar y procesar contrato
            String contenidoContrato;
            try {
                contenidoContrato = contratoApiService.validarYProcesarContrato(versionContratoDto);
            } catch (Exception e) {
                versionContratoDto.setIdAPI(api.getIdApi());
                versionContratoDto.setNombreAPI(api.getNombre());
                bindingResult.rejectValue("contenido", "error.contrato", e.getMessage());
                model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
                model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
                return "desarrollador/interno/crearVersionApi";
            }

            VersionApi versionApi = new VersionApi();
            ContratoApi contratoApi = new ContratoApi();

            if (api.getVersionesApi().isEmpty()) {      // si es primera version: PASO2
                versionApi.setApi(api);
                versionApi.setVersion(versionContratoDto.getVersion());
                versionApi.setFechaPublicacion(versionContratoDto.getFechaPublicacion());
                versionApi.setEstadoVersion(versionContratoDto.getEstadoVersion());

                contratoApi.setVersionApi(versionApi);
                contratoApi.setFormato(versionContratoDto.getFormato());
                contratoApi.setContenido(contenidoContrato);

            } else {    // si hay más versiones: no es PASO2

            }

            contratoRepository.save(contratoApi);

            redirectAttributes.addFlashAttribute("msg", "Creaste la primera version de tu API "+api.getNombre()+" exitosamente");

            return "redirect:/dev/int/"+api.getIdApi()+"/versiones/"+versionApi.getIdVersion()+"/docs";

        } else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la version");
    }

    @GetMapping("/{idApi}/versiones/{idVersion}/docs")
    public String vistaDocsVersionApi (Model model, Authentication auth, HttpSession session, @PathVariable("idApi") Integer idApi, @PathVariable("idVersion") Integer idVersion) {

        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();

            Optional<VersionApi> versionApiX = versionApiRepository.findById(idVersion);
            if (versionApiX.isPresent() && versionApiX.get().getApi().equals(api)) {
                VersionApi versionApi = versionApiX.get();

                model.addAttribute("paso3", false);
                if (versionApi.getDocumentaciones().isEmpty()) {
                    model.addAttribute("paso3", true);
                }

                DocGeneralDTO docGeneralDto = new DocGeneralDTO();
                docGeneralDto.setIdApi(api.getIdApi());
                docGeneralDto.setNombreApi(api.getNombre());
                docGeneralDto.setIdVersion(versionApi.getIdVersion());
                docGeneralDto.setNumeroVersion(versionApi.getVersion());

                model.addAttribute("docGeneralDto", docGeneralDto);



            } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la versión solicitada");

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada");


        return "desarrollador/interno/crearDocGeneral";

    }

    @PostMapping("/guardarDocsApi")
    public String guardarDocGeneralesApi(Model model, Authentication auth, HttpSession session, @ModelAttribute DocGeneralDTO docGeneralDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(docGeneralDto.getIdApi());
        Optional<VersionApi> versionApiX = versionApiRepository.findById(docGeneralDto.getIdVersion());
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();
            docGeneralDto.setNombreApi(api.getNombre());

            if (!(versionApiX.isPresent() && versionApiX.get().getApi().equals(api))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación");
            }
            VersionApi versionApi = versionApiX.get();

            if (bindingResult.hasErrors()) {
                model.addAttribute("docGeneralDto", docGeneralDto);
                model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
                model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
                return "desarrollador/interno/crearVersionApi";
            }

            // Validar y procesar archivos
            List<String> urlsDocs = new ArrayList<>();
            String contenidoContrato;
            try {
//                urlsDocs = ;
            } catch (Exception e) {
                bindingResult.rejectValue("contenido", "error.contrato", e.getMessage());
                model.addAttribute("estadosVer", VersionApi.EstadoVersion.values());
                model.addAttribute("formatosCont", ContratoApi.FormatoContrato.values());
                return "desarrollador/interno/crearVersionApi";
            }

            Documentacion documentacion = new Documentacion();
            List<Documentacion> docsApi = new ArrayList<>();
            doc_alto_nivel docAltoNivel = new doc_alto_nivel();

            if (versionApi.getDocumentaciones().isEmpty()) {      // si es primera docs: PASO3



            } else {    // si hay más versiones: no es PASO2

            }


            redirectAttributes.addFlashAttribute("msg", "Creaste la documentación de la versión "+versionApi.getVersion()+" de tu API "+api.getNombre()+" exitosamente");

            return "redirect:/dev/int/"+api.getIdApi()+"/versiones/"+versionApi.getIdVersion()+"/docs";

        } else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la documentación");
    }






    @GetMapping("{idApi}/versiones")
    public String verVersionesApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") String idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        // FALTA

        return "desarrollador/interno/versionesApi";

    }

    @GetMapping("{idApi}/versiones/{idVersion}")
    public String verVersionesApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") String idApi, @PathVariable("idVersion") String idVersion) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        // FALTA

        return null;
    }

}
