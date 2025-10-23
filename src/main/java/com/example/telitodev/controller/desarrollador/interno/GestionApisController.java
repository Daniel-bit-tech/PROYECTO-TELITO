package com.example.telitodev.controller.desarrollador.interno;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.ApiCreacionDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.LogApi;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.VersionApi;
import com.example.telitodev.repository.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Controller
@PreAuthorize("hasAnyRole('DEV','DEVINT')")
@RequestMapping("/dev/int")
public class GestionApisController extends BaseController {

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

        if (bindingResult.hasErrors()) {
            model.addAttribute("listaDominios", dominioRepository.findAll());
            model.addAttribute("listaTags", tagRepository.findAll());
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

            VersionApi version = new VersionApi();
            version.setApi(api);

            model.addAttribute("version", version);

        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la API solicitada");


        return "desarrollador/interno/crearVersionApi";
    }

    @PostMapping("/guardarVersionApi")
    public String guardarVersionApi(Model model, Authentication auth, HttpSession session, @ModelAttribute VersionApi versionApi, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        Optional<Api> apiX = apiRepository.findById(versionApi.getApi().getIdApi());
        if (apiX.isPresent() && apiX.get().getUsuario().equals(usuario)) {
            Api api = apiX.get();

            if (bindingResult.hasErrors()) {
                return "desarrollador/interno/crearVersionApi";
            }

            versionApiRepository.save(versionApi);

            redirectAttributes.addFlashAttribute("msg", "Creaste la primera version de tu API "+api.getNombre()+" exitosamente");

            return "redirect:/dev/int/"+api.getIdApi()+"/versiones/"+versionApi.getIdVersion()+"/contrato";

        } else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo guardar la version");
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
