package com.example.telitodev.controller.productowner;

import com.example.telitodev.dto.DashboardPoDTO;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.DashboardPoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SUPERADMIN')")
public class DashboardPoController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DashboardPoService dashboardPoService;

    @GetMapping("/dashboard")
    public String getDashboard(Model model, Authentication authentication) {

        // 1. Obtener el DNI del PO logueado
        String userDni = authentication.getName();

        // 2. Buscar al usuario y su organización
        Usuario po = usuarioRepository.findById(userDni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDni));

        // --- CORRECCIÓN AQUÍ ---
        // El método correcto es getOrganizacion()
        if (po.getOrganizacion() == null) {
            // Si el PO no tiene organización, mostrar una vista de error o especial
            model.addAttribute("error", "No estás asignado a ninguna organización.");
            return "po/sin-organizacion"; // Vista de error simple
        }

        // 3. Obtener los datos del dashboard para esa organización
        // --- CORRECIÓN AQUÍ ---
        // El método correcto es getOrganizacion()
        DashboardPoDTO dashboardData = dashboardPoService.getDashboardData(po.getOrganizacion().getIdOrganizacion());

        // 4. Pasar los datos a la vista
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("po", po);

        return "po/dashboard"; // Ruta al archivo HTML
    }
}
