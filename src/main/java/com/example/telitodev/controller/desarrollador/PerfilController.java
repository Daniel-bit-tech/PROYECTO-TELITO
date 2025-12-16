package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PerfilController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final CredencialApiRepository credencialApiRepository;
    final TicketRepository ticketRepository;
    final SolicitudAccesoRepository solicitudAccesoRepository;
    final FeedbackRepository feedbackRepository;
    
    public PerfilController(UsuarioRepository usuarioRepository,
                           CredencialApiRepository credencialApiRepository,
                           TicketRepository ticketRepository,
                           SolicitudAccesoRepository solicitudAccesoRepository,
                           FeedbackRepository feedbackRepository) {
        this.usuarioRepository = usuarioRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.ticketRepository = ticketRepository;
        this.solicitudAccesoRepository = solicitudAccesoRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/perfil")
    public String showperfil(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);
        
        // ACTIVIDADES RECIENTES DEL USUARIO
        
        // 1. Últimas 3 credenciales API generadas
        List<CredencialApi> credencialesRecientes = List.of();
        try {
            credencialesRecientes = credencialApiRepository
                .findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni())
                .stream()
                .limit(3)
                .toList();
            System.out.println("✅ Credenciales cargadas: " + credencialesRecientes.size());
        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar credenciales: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("credencialesRecientes", credencialesRecientes);
        
        // 2. Últimos 3 tickets de soporte
        List<Ticket> ticketsRecientes = List.of();
        try {
            ticketsRecientes = ticketRepository
                .findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni())
                .stream()
                .limit(3)
                .toList();
            System.out.println("✅ Tickets cargados: " + ticketsRecientes.size());
        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar tickets: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("ticketsRecientes", ticketsRecientes);
        
        // 3. Últimas 3 solicitudes de acceso a APIs
        List<SolicitudAcceso> solicitudesRecientes = List.of();
        try {
            solicitudesRecientes = solicitudAccesoRepository
                .findByUsuario_DniOrderByFechaSolicitudDesc(usuario.getDni())
                .stream()
                .limit(3)
                .toList();
            System.out.println("✅ Solicitudes cargadas: " + solicitudesRecientes.size());
        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar solicitudes: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("solicitudesRecientes", solicitudesRecientes);
        
        // 4. Últimos 3 feedbacks enviados (deshabilitado temporalmente por datos huérfanos)
        List<Feedback> feedbacksRecientes = List.of();
        model.addAttribute("feedbacksRecientes", feedbacksRecientes);
        
        System.out.println("✅ Perfil cargado correctamente para usuario: " + usuario.getDni());

        return "desarrollador/perfil";
    }
}