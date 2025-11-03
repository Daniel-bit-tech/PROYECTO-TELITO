package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.TicketRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dev")
public class SoporteController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SoporteController.class);

    final UsuarioRepository usuarioRepository;
    final TicketRepository ticketRepository;
    final ChatbotService chatbotService;
    final ApiRepository apiRepository;
    
    public SoporteController(UsuarioRepository usuarioRepository, TicketRepository ticketRepository, 
                            ChatbotService chatbotService, ApiRepository apiRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.chatbotService = chatbotService;
        this.apiRepository = apiRepository;
    }

    @GetMapping("/soporte")
    public String showsoporte(Model model, Authentication auth, HttpSession session) {

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);

        List<Ticket> tickets = ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());
        model.addAttribute("tickets", tickets);
        model.addAttribute("usuario", usuario);

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        return "desarrollador/soporte";
    }

    /**
     * Endpoint del chatbot integrado con AWS Elastic Beanstalk.
     * Recibe JSON { "message": "..." } y devuelve { "reply": "...", "user": "..." }
     * El servicio ChatbotService se comunica con el chatbot desplegado en AWS.
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/soporte/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> payload, Authentication auth, HttpSession session) {
        String userMessage = payload == null ? "" : payload.getOrDefault("message", "");

        // Llamar al servicio de chatbot de AWS
        String reply = chatbotService.getChatbotResponse(userMessage);

        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);

        // Añadir información del usuario si está disponible
        try {
            Usuario usuario = getCurrentUser(auth, session);
            if (usuario != null) {
                response.put("user", usuario.getNombre() == null ? usuario.getDni() : usuario.getNombre());
            }
        } catch (Exception ignored) {
            // No bloquear la respuesta por errores al obtener usuario
        }

        return response;
    }

    /**
     * Muestra el formulario para crear un nuevo ticket
     */
    @GetMapping("/soporte/nuevo-ticket")
    public String showNuevoTicket(Model model, Authentication auth, HttpSession session) {
        
        try {
            // Cargar la lista de APIs disponibles para el dropdown
            List<Api> apis = apiRepository.findAll();
            logger.info("APIs cargadas: " + (apis != null ? apis.size() : "null"));
            
            if (apis == null || apis.isEmpty()) {
                logger.warn("No hay APIs disponibles en la base de datos");
                apis = new java.util.ArrayList<>();
            }
            
            model.addAttribute("apis", apis);
            
            // Agregar información de impersonación al modelo
            addImpersonationAttributes(model, session);
            
            return "desarrollador/nuevo-ticket";
            
        } catch (Exception e) {
            logger.error("Error al cargar formulario de nuevo ticket", e);
            model.addAttribute("errorMessage", "Error al cargar el formulario: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Procesa la creación de un nuevo ticket
     */
    @PostMapping("/soporte/crear-ticket")
    public String crearTicket(@RequestParam("asunto") String asunto,
                             @RequestParam("idApi") Integer idApi,
                             @RequestParam("descripcion") String descripcion,
                             @RequestParam(value = "infoAdicional", required = false) String infoAdicional,
                             Authentication auth,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        try {
            logger.info("=== CREANDO TICKET ===");
            logger.info("Asunto: " + asunto);
            logger.info("ID API: " + idApi);
            logger.info("Descripción length: " + descripcion.length());
            
            // Obtener el usuario actual
            Usuario usuario = getCurrentUser(auth, session);
            logger.info("Usuario: " + usuario.getDni());
            
            // Buscar la API seleccionada
            Api api = apiRepository.findById(idApi).orElseThrow(() -> 
                new RuntimeException("API no encontrada"));
            logger.info("API encontrada: " + api.getNombre());
            
            // Crear el nuevo ticket
            Ticket ticket = new Ticket();
            ticket.setAsunto(asunto);
            ticket.setDescripcion(descripcion + (infoAdicional != null && !infoAdicional.isEmpty() 
                ? "\n\nInformación adicional:\n" + infoAdicional : ""));
            ticket.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            ticket.setEstado(true); // Estado activo
            ticket.setUsuario(usuario);
            ticket.setApi(api);
            
            // Guardar el ticket
            Ticket savedTicket = ticketRepository.save(ticket);
            logger.info("Ticket guardado con ID: " + savedTicket.getIdTicket());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Ticket creado exitosamente. Un miembro del equipo de QA lo atenderá pronto.");
            
            return "redirect:/dev/soporte";
            
        } catch (Exception e) {
            logger.error("ERROR AL CREAR TICKET: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al crear el ticket: " + e.getMessage());
            return "redirect:/dev/soporte/nuevo-ticket";
        }
    }


}
