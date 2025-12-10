package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiCreacionDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.example.telitodev.service.creacionApi.DocApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping(value = {"/dev/int","/dev/int/misApis"})
public class GestionApisController extends BaseController {

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
    private final S3DocsApiService s3DocsApiService;

    public GestionApisController(ApiRepository apiRepository, UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository, DominioRepository dominioRepository, TagRepository tagRepository, EstadoApiRepository estadoApiRepository, VersionApiRepository versionApiRepository, ProyectoRepository proyectoRepository, ContratoRepository contratoRepository, S3DocsApiService s3DocsApiService) {
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.estadoApiRepository = estadoApiRepository;
        this.versionApiRepository = versionApiRepository;
        this.proyectoRepository = proyectoRepository;
        this.contratoRepository = contratoRepository;
        this.s3DocsApiService = s3DocsApiService;
    }


    @GetMapping()
    public String listaApisDeEquipoUsuario(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(value = "dominios",required = false) List<Integer> selDominios,
                                     @RequestParam(value = "tags", required = false) List<Integer> selTags,
                                     @RequestParam(value = "nombre", required = false) String nombre,
                                     @RequestParam(value = "estados", required = false) List<Integer> selEstados,
                                     Model model, Authentication auth, HttpSession session) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());

        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

//        Page<Api> listaApis = apiRepository.findByUserOrgAndFilters(nombre, selDominios, selTags, selEstados, usuario.getOrganizacion().getIdOrganizacion(), pageable);
        Integer idEquipoUsuario = Optional.ofNullable(usuario.getEquipo())
                .map(Equipo::getIdEquipo)
                .orElse(null);
        List<Api> listaApis = apiRepository.findByFilterAndDniUsuario(nombre, selDominios, selTags, idEquipoUsuario);
        model.addAttribute("listaApis", listaApis);

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());
        model.addAttribute("listaEstados", estadoApiRepository.findAll());

        model.addAttribute("selDominios", selDominios);
        model.addAttribute("selTags", selTags);
        model.addAttribute("selEstados", selEstados);

        return "desarrollador/interno/listaMisApis";

    }

    @GetMapping("/{idApi}")
    public String verDetalleApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") Integer idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getEquipo().equals(usuario.getEquipo())) {
            Api api = apiX.get();
            model.addAttribute("api", api);

            Documentacion docReadMe = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(api.getIdApi(), Documentacion.FormatoDoc.MARKDOWN, "Descripción ténica");
            if (docReadMe != null) {
                model.addAttribute("docReadMe", docReadMe);
            } else {
                model.addAttribute("docReadMe", new Documentacion());
            }

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la API solicitada");


        model.addAttribute("currentView", "listado");
        model.addAttribute("subView", "general");
        return "desarrollador/interno/gestionApiBase";
    }

    /***
     * Enpoint para renderizar sección de navbar secundario
     * @param idApi es el ID de la Api a buscar
     * @param section es el nombre de la seccion a mostrar
     * @param model
     * @param auth
     * @param session
     * @return un html dinámico de la sección con info de la Api
     */
    @GetMapping("/{idApi}/section/{section}")
    public String cargarSeccion(@PathVariable Integer idApi, @PathVariable String section,
                                Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getEquipo().equals(usuario.getEquipo())) {
            Api api = apiX.get();
            model.addAttribute("api", apiX.get());

            switch (section) {
                case "general":
                    model.addAttribute("api", api);
                    model.addAttribute("apiCreDto", new ApiCreacionDTO(idApi,api.getNombre(),api.getDescripcion(),api.getEndpointUrl(),api.getDominio().getIdDominio(),api.getTag().getIdTag()));

                    model.addAttribute("listaDominios", dominioRepository.findAll());
                    model.addAttribute("listaTags", tagRepository.findAll());
                    model.addAttribute("listaEstados", estadoApiRepository.findAll());
                    return "desarrollador/interno/secciones :: general";
                case "documentacion":
                    model.addAttribute("docs", documentacionRepository.findByApi_IdApiOrderByFechaCreacionDesc(idApi));
                    return "desarrollador/interno/secciones :: documentacion";
//                case "comunidad":
//                    return "desarrollador/interno/secciones :: comunidad";
//
                case "readme":
                    Documentacion readme = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(api.getIdApi(), Documentacion.FormatoDoc.MARKDOWN, "Documentación técnica");
                    model.addAttribute("readme", readme);
                    if (readme!=null) {
                        try {
                            model.addAttribute("url", s3DocsApiService.generarUrlDescarga(readme.getUrlDocumento(), Duration.ofMinutes(5)));
                        } catch (DocApiService.DocValidationException e) {
                            model.addAttribute("url", null);
                            model.addAttribute("error", "No se puede observar el archivo readMe.");
                        }
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {

                            JsonNode jsonNode = objectMapper.readTree(readme.getContenido());
                            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

                            String jsonContent = objectMapper.writeValueAsString(jsonNode);

                            model.addAttribute("contenidoJson", jsonContent);
                        } catch (Exception e) {
                            model.addAttribute("contenidoJson", readme.getContenido()); // Fallback como string
                        }
                    }
                    return "desarrollador/interno/secciones :: readme";
                default:
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta API");
        }
    }

    @GetMapping("{idApi}/versiones")
    public String verVersionesApi(Model model, Authentication auth, HttpSession session, RedirectAttributes redirectAttributes,
                                  @PathVariable("idApi") Integer idApi, @RequestParam(value = "idVersion", required = false) Integer idVersion) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("listaEstados", VersionApi.EstadoVersion.values());

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getEquipo().equals(usuario.getEquipo())) {
            Api api = apiX.get();
            model.addAttribute("api", api);

            List<VersionApi> versiones = versionApiRepository.findByApi_IdApi(idApi);
            if (!versiones.isEmpty()) {
                model.addAttribute("versiones", versiones);
                VersionApi versionSel = versiones.get(0);
                if (idVersion!=null ) {
                    versionSel = versionApiRepository.findByIdVersionAndApi_IdApi(idVersion,idApi);
                    if (versionSel==null) return "redirect:/dev/int/misApis/"+idApi+"/versiones";
                }
                model.addAttribute("versionSeleccionada", versionSel);
                model.addAttribute("estadoActual", versionSel.getEstadoVersion());

                ContratoApi contrato = versionSel.getContratoApi();
                if (contrato!=null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode jsonNode = objectMapper.readTree(contrato.getContenido());
                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

                        String jsonContent = objectMapper.writeValueAsString(jsonNode);

                        model.addAttribute("contenidoJson", jsonContent);
                    } catch (Exception e) {
                        model.addAttribute("contenidoJson", contrato.getContenido()); // Fallback como string
                    }
                }

            } else {
                model.addAttribute("versiones", null);
            }

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la API solicitada");

        model.addAttribute("currentView", "versionado");
        return "desarrollador/interno/gestion/versionesApi";
    }

    @GetMapping("/{idApi}/ajustes")
    public String verDetalleConfigApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") Integer idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getEquipo().equals(usuario.getEquipo())) {
            model.addAttribute("api", apiX.get());
            model.addAttribute("listaEstados", estadoApiRepository.findAll());
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la API solicitada");

        model.addAttribute("currentView", "ajustes");
        return "desarrollador/interno/ajustesApi";
    }

    @PostMapping("/estadoApi")
    public String editarEstadoApi(Authentication auth, HttpSession session,
                                 @RequestParam Integer idApi, @RequestParam Integer estadoApi,
                                 RedirectAttributes redirectAttributes) {

        Usuario usuario = getCurrentUser(auth, session);

        Api api = apiRepository.findById(idApi)
                .filter(a -> a.getEquipo().equals(usuario.getEquipo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo encontrar la Api solicitada."));

        try {
            EstadoApi nuevoEstadoApi = new EstadoApi();
            api.setEstadoApi(nuevoEstadoApi);
            apiRepository.save(api);

            redirectAttributes.addFlashAttribute("toastMessage", "Estado actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Estado inválido ");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/dev/int/misApis/"+api.getIdApi()+"/ajustes";
    }

    @DeleteMapping("/eliminar")
    public String eliminarApi(Authentication auth, HttpSession session,
                              @RequestParam Integer idApi) {

        Usuario usuario = getCurrentUser(auth, session);


        return "redirect:/dev/int/misApis/";
    }

}
