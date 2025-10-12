package com.example.telitodev.controller.productowner;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/po")
public class OrganizacionController {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final ProyectoRepository proyectoRepository;

    public OrganizacionController(UsuarioRepository usuarioRepository,
                                  OrganizacionRepository organizacionRepository,
                                  ProyectoRepository proyectoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacionRepository = organizacionRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @GetMapping("/organizacion")
    public String showOrganizacion(Model model, Authentication auth) {
        try {
            // 1. Obtener usuario logueado
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);

            // 2. Obtener organización del usuario
            Organizacion organizacion = organizacionRepository.findByUsuarioDni(usuario.getDni());

            if (organizacion == null) {
                // Si no tiene organización, mostrar página vacía
                return "po/organizacion";
            }

            model.addAttribute("organizacion", organizacion);

            // 3. Obtener miembros de la organización (con roles cargados)
            List<Usuario> miembros = usuarioRepository.findByOrganizacionIdWithRol(organizacion.getIdOrganizacion());
            model.addAttribute("miembros", miembros);

            // 4. Obtener proyectos activos de la organización
            List<Proyecto> proyectosActivos = proyectoRepository.findProyectosActivosByOrganizacionId(organizacion.getIdOrganizacion());
            model.addAttribute("proyectosActivos", proyectosActivos);

            // 5. Obtener APIs únicas de la organización
            List<Api> apisUnicas = obtenerApisUnicasDeOrganizacion(organizacion);
            model.addAttribute("apis", apisUnicas);

            return "po/organizacion";

        } catch (Exception e) {
            // En caso de error, igual mostrar la página pero sin datos adicionales
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            model.addAttribute("usuario", usuario);
            return "po/organizacion";
        }
    }

    // MÉTODO ORIGINAL - SIN CAMBIOS
    @GetMapping("/solicitudAcceso")
    public String showSolicitudAcceso(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);
        return "po/solicitudAcceso";
    }

    // Método auxiliar para obtener APIs únicas de la organización
    private List<Api> obtenerApisUnicasDeOrganizacion(Organizacion organizacion) {
        try {
            // Opción 1: Usar el método del repository si existe
            List<Proyecto> proyectosConApis = proyectoRepository.findByOrganizacionIdWithApis(organizacion.getIdOrganizacion());

            if (proyectosConApis != null && !proyectosConApis.isEmpty()) {
                return proyectosConApis.stream()
                        .filter(proyecto -> proyecto.getProyectoHasApis() != null)
                        .flatMap(proyecto -> proyecto.getProyectoHasApis().stream())
                        .map(ProyectoHasApi::getApi)
                        .distinct()
                        .collect(Collectors.toList());
            }

            // Opción 2: Si no hay proyectos con APIs, usar las relaciones lazy
            if (organizacion.getProyectos() != null) {
                return organizacion.getProyectos().stream()
                        .filter(proyecto -> proyecto.getProyectoHasApis() != null)
                        .flatMap(proyecto -> proyecto.getProyectoHasApis().stream())
                        .map(ProyectoHasApi::getApi)
                        .distinct()
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            // Si hay error de lazy loading, devolver lista vacía
        }

        return new ArrayList<>();
    }
}