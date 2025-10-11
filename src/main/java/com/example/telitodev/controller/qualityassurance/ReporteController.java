package com.example.telitodev.controller.qualityassurance;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Reporte;
import com.example.telitodev.entity.Usuario; // <-- Importa la clase Usuario
import com.example.telitodev.repository.ReporteRepository;
import com.example.telitodev.repository.UsuarioRepository; // <-- Importa el repositorio de Usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/qa")
@PreAuthorize("hasAnyRole('QA', 'SADMIN')")
public class ReporteController extends BaseController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/reportes")
    public String showReportesView(Model model,
                                   Authentication auth,
                                   HttpSession session,
                                   @RequestParam(value = "estados", required = false) List<String> estados,
                                   @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                   @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                   @RequestParam(value = "nombreApi", required = false) String nombreApi,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {

        // 🧩 Obtener usuario autenticado y datos de impersonación
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        addImpersonationAttributes(model, session);
        model.addAttribute("usuario", usuario);

        // 📅 Convertir fechas a Timestamp
        Timestamp inicio = null;
        Timestamp fin = null;
        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = Timestamp.valueOf(fechaInicio + " 00:00:00");
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = Timestamp.valueOf(fechaFin + " 23:59:59");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 📄 Configurar paginación (orden descendente por fecha de creación)
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        Page<Reporte> reportesPage = reporteRepository.findByFiltersPaged(estados, inicio, fin, nombreApi, pageable);

        // ⚙️ Evitar error si la página solicitada excede el total
        if (page >= reportesPage.getTotalPages() && reportesPage.getTotalPages() > 0) {
            pageable = PageRequest.of(reportesPage.getTotalPages() - 1, size);
            reportesPage = reporteRepository.findByFiltersPaged(estados, inicio, fin, nombreApi, pageable);
            page = reportesPage.getTotalPages() - 1;
        }

        // 📦 Pasar datos al modelo
        model.addAttribute("reportes", reportesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportesPage.getTotalPages());
        model.addAttribute("selectedEstados", estados);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("nombreApi", nombreApi);
        model.addAttribute("pageSize", size);

        return "qa/reportes";
    }



    @GetMapping("/reporteDetalle")
    public String showReporteDetalleView(Model model, Authentication auth, HttpSession session,
                                         @RequestParam("idReporte") Integer idReporte) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

        // Agregar atributos de impersonación
        addImpersonationAttributes(model, session);

        model.addAttribute("usuario", usuario);

        Reporte reporte = reporteRepository.findById(idReporte).orElse(null);

        if (reporte == null) {
            return "redirect:/qa/reportes?error=Reporte no encontrado";
        }

        // Agregar el reporte al modelo
        model.addAttribute("reporte", reporte);

        return "qa/reporteDetalle";
    }
}