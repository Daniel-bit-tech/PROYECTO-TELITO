package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.entity.Reporte;
import com.example.telitodev.entity.Usuario; // <-- Importa la clase Usuario
import com.example.telitodev.repository.ReporteRepository;
import com.example.telitodev.repository.UsuarioRepository; // <-- Importa el repositorio de Usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class ReporteController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/reportes")
    public String showReporteView(Model model, Authentication auth,
                                  @RequestParam(required = false) String[] formType,
                                  @RequestParam(required = false) String formEstado,
                                  @RequestParam(required = false) String formFecha) {
        // Obtén el objeto Usuario y agrégalo al modelo
        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        model.addAttribute("usuario", usuario);

        // Filtrar los reportes según los parámetros
        List<Reporte> reportes = reporteRepository.findAll(); // Aquí puedes agregar la lógica de filtros

        // Agregar la lista de reportes al modelo
        model.addAttribute("reportes", reportes);

        return "qa/reportes";
    }
}