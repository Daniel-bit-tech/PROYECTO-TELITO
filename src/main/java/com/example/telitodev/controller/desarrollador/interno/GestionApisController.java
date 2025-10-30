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

    public GestionApisController(ApiRepository apiRepository, UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository, DominioRepository dominioRepository, TagRepository tagRepository, EstadoApiRepository estadoApiRepository, VersionApiRepository versionApiRepository, ProyectoRepository proyectoRepository, ContratoRepository contratoRepository) {
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


    @GetMapping()
    public String listaApisDeUsuario(@RequestParam(value = "dominios",required = false) List<Integer> selDominios,
                                     @RequestParam(value = "tags", required = false) List<Integer> selTags,
                                     @RequestParam(value = "nombre", required = false) String nombre,
                                     @RequestParam(value = "estados", required = false) List<Integer> selEstado,
                                     Model model, Authentication auth, HttpSession session) {
        String dominios = selDominios == null ? null : selDominios.toString();
        String tags = selTags == null ? null : selTags.toString();
        System.out.println("Doms: "+dominios + " \nTags: " + tags);

        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        List<Api> listaApis = apiRepository.findByFilterAndDniUsuario(nombre, selDominios, selTags,usuario.getDni());
        model.addAttribute("listaApis", listaApis);

        model.addAttribute("listaDominios", dominioRepository.findAll());
        model.addAttribute("listaTags", tagRepository.findAll());
        model.addAttribute("listaEstados", estadoApiRepository.findAll());


        return "desarrollador/interno/listaMisApis";

    }


    @GetMapping("/{idApi}")
    public String verDetalleApi(Model model, Authentication auth, HttpSession session, @PathVariable("idApi") Integer idApi) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();
            model.addAttribute("api", api);
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la API solicitada");


        model.addAttribute("currentView", "listado");
        model.addAttribute("subView", "general");
        return "desarrollador/interno/gestionApiBase";
    }

    /*

     */
    @GetMapping("/{idApi}/section/{section}")
    public String cargarSeccion(@PathVariable Integer idApi, @PathVariable String section,
                                Model model, Authentication auth, HttpSession session) {

        System.out.println("Solicitud de seccion de API: "+idApi+" a SECCION: "+section);

        Usuario usuario = getCurrentUser(auth, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            model.addAttribute("api", apiX.get());

            switch (section) {
                case "general": return "desarrollador/interno/secciones :: general";
                case "versionado": return "desarrollador/interno/secciones :: versionado";
                case "documentacion": return "desarrollador/interno/secciones :: documentacion";
                case "comunidad":
                    return "desarrollador/interno/sections/comunidad";
                default:
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta API");
        }
    }

    @GetMapping("{idApi}/versiones/{idVersion}")
    public String verVersionesApi(Model model, Authentication auth, HttpSession session, RedirectAttributes redirectAttributes,
                                  @PathVariable("idApi") Integer idApi, @PathVariable("idVersion") Integer idVersion) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(idApi);
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();

            List<VersionApi> versiones = versionApiRepository.findByApi_IdApi(idApi);
            if (!versiones.isEmpty()) {
                model.addAttribute("versiones", versiones);
                if (versiones.stream().anyMatch(version -> version.getIdVersion().equals(idVersion))) {
                    model.addAttribute("versionActiva",idVersion);
                } else model.addAttribute("versionActiva",versiones.get(0).getIdVersion());
            } else {
                redirectAttributes.addFlashAttribute("msg","Debe crear su primera version para la API "+ api.getNombre());
                return "redirect:/dev/int/"+api.getIdApi()+"/nuevaVersion";
            }

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la API solicitada");

        return null;
    }

}
