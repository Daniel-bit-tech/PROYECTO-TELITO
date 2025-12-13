package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.EntornoDto;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.service.DocMDService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/apis")

public class ApiController extends BaseController {

    private final DocMDService docMDService;

    final ApiRepository apiRepository;
    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;
    final VersionApiRepository versionApiRepository;
    final EjemplosCodigoRepository ejemplosCodigoRepository;
    final DominioRepository dominioRepository;
    final TagRepository tagRepository;
    private final ContratoRepository contratoRepository;
    final SolicitudAccesoRepository solicitudAccesoRepository;

    public ApiController(ApiRepository apiRepository, UsuarioRepository usuarioRepository, VersionApiRepository versionApiRepository, DocMDService docMDService, DocumentacionRepository documentacionRepository, VersionApiRepository versionApiRepository1, EjemplosCodigoRepository ejemplosCodigoRepository, DominioRepository dominioRepository, TagRepository tagRepository, ContratoRepository contratoRepository, SolicitudAccesoRepository solicitudAccesoRepository) {
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.docMDService = docMDService;
        this.documentacionRepository = documentacionRepository;
        this.versionApiRepository = versionApiRepository1;
        this.ejemplosCodigoRepository = ejemplosCodigoRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.contratoRepository = contratoRepository;
        this.solicitudAccesoRepository = solicitudAccesoRepository;
    }


    @GetMapping()
    public String catalogo(@RequestParam(value = "dominios",required = false) List<Integer> selDominios,
                           @RequestParam(value = "tags", required = false) List<Integer> selTags,
                           @RequestParam(value = "nombre", required = false) String nombre,
                            Model model, Authentication auth, HttpSession session) {
        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: "+dominios + " \nTags: " + tags);

        List<Api> apis = apiRepository.findByFilters(nombre, selDominios, selTags);
        Map<Integer, String> accesoMap = new HashMap<>();
        for (Api api : apis) {
            System.out.println("api " + api.getNombre());
        }

        if (auth != null && auth.isAuthenticated()) {
            // Obtener el usuario correcto considerando impersonación usando BaseController
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            List<SolicitudAcceso> misSolicitudes = solicitudAccesoRepository.findByUsuario_Dni(usuario.getDni());
            Map<Integer, Boolean> solicitudesMap = misSolicitudes.stream()
                    .collect(Collectors.toMap(s -> s.getApi().getIdApi(), SolicitudAcceso::getEstado));

            for (Api api : apis) {
                // CASO A: Es mi propio equipo (Dueño) -> Acceso Total
                if (usuario.getEquipo() != null &&
                        api.getEquipo() != null &&
                        usuario.getEquipo().getIdEquipo().equals(api.getEquipo().getIdEquipo())) {

                    accesoMap.put(api.getIdApi(), "PROPIO");

                }
                // CASO B: Ya solicité acceso antes
                else if (solicitudesMap.containsKey(api.getIdApi())) {
                    Boolean estado = solicitudesMap.get(api.getIdApi());

                    if (Boolean.TRUE.equals(estado)) {
                        accesoMap.put(api.getIdApi(), "APROBADO");
                    } else {
                        accesoMap.put(api.getIdApi(), "PENDIENTE");
                    }
                }

                else {
                    accesoMap.put(api.getIdApi(), "SOLICITAR");
                }
            }
        }
        model.addAttribute("accesoMap", accesoMap);
        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());

        model.addAttribute("apis", apis);
        model.addAttribute("selTags", selTags);
        model.addAttribute("selDominios", selDominios);
        model.addAttribute("nombre", nombre);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/apis";
    }

    @PostMapping("/solicitar-acceso")
    @PreAuthorize("isAuthenticated()")
    public String solicitarAcceso(@RequestParam("idApi") Integer idApi,
                                  @RequestParam("motivo") String motivo,
                                  Authentication auth, HttpSession session) {

        Usuario usuario = getCurrentUser(auth, session);
        Optional<Api> apiOpt = apiRepository.findById(idApi);

        if (apiOpt.isPresent()) {

            if (!solicitudAccesoRepository.existsByUsuario_DniAndApi_IdApi(usuario.getDni(), idApi)) {
                SolicitudAcceso solicitud = new SolicitudAcceso();
                solicitud.setUsuario(usuario);
                solicitud.setApi(apiOpt.get());
                solicitud.setDescripcionUso(motivo);
                solicitud.setEstado(false);
                solicitud.setFechaSolicitud(new java.sql.Timestamp(System.currentTimeMillis()));

                solicitudAccesoRepository.save(solicitud);
            }
        }
        return "redirect:/apis?exitoSolicitud=true";
    }



//    @GetMapping("/{idApi}/docs")
//    @PreAuthorize("isAuthenticated()")
//    public String verDocsApi(@PathVariable Integer idApi,
//                             Model model, Authentication auth, HttpSession session) throws IOException {
//        Usuario usuario = getCurrentUser(auth, session);
//        model.addAttribute("usuario", usuario);
//        // Agregar información de impersonación al modelo usando BaseController
//        addImpersonationAttributes(model, session);
//
//        Optional<Api> apiX = apiRepository.findById(idApi);
//        if (apiX.isPresent()) {
//            Api api = apiX.get();
//            model.addAttribute("api", api);
//        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el api");
//
//        Documentacion documentacion = documentacionRepository.findByApi_IdApiAndFormato(idApi, Documentacion.FormatoDoc.MARKDOWN);
//
//        List<String> nombresSecs = docMDService.nombresSecsReadme(idApi);
//        model.addAttribute("nombresSecs", nombresSecs);
//
//        return "general/docs/apiDoc";
//    }
    @GetMapping("/{idApi}/docs")
    @PreAuthorize("isAuthenticated()")
    public String vistaBaseDocsApi(@PathVariable Integer idApi,
                             Model model, Authentication auth, HttpSession session) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent()) {
            Api api = apiX.get();
            model.addAttribute("api", api);
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el api");

        Documentacion documentacion = documentacionRepository.findByApi_IdApiAndFormato(idApi, Documentacion.FormatoDoc.MARKDOWN);

        List<String> nombresSecs = docMDService.nombresSecsReadme(idApi);
        model.addAttribute("nombresSecs", nombresSecs);

        return "general/docs/docsApiBase";
    }


    @GetMapping("/{id}/test")
    @PreAuthorize("isAuthenticated()")
    public String testApi(@PathVariable Integer id, Model model, Authentication auth, HttpSession session) {
        Optional<Api> api = apiRepository.findById(id);
        if (api.isPresent()) {
            model.addAttribute("api", api.get());
        }

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/sandbox";
    }
    // Endpoint de Versiones de Api específica
    @GetMapping("/{id}/versiones")
    @ResponseBody
    public List<VersionApi> obtenerVersiones(@PathVariable Integer id) {
        return versionApiRepository.findByApi_IdApi(id);
//        return versionApiRepository.findAll();
    }

}
