package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.TicketRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ChatbotService;
import com.example.telitodev.service.ChatbotTicketService;
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
    final ChatbotTicketService chatbotTicketService;
    
    public SoporteController(UsuarioRepository usuarioRepository, TicketRepository ticketRepository, 
                            ChatbotService chatbotService, ApiRepository apiRepository,
                            ChatbotTicketService chatbotTicketService) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.chatbotService = chatbotService;
        this.apiRepository = apiRepository;
        this.chatbotTicketService = chatbotTicketService;
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
     * Endpoint para que el chatbot cree tickets automáticamente
     * Recibe JSON y devuelve JSON con el resultado
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/soporte/chatbot/crear-ticket")
    @ResponseBody
    public Map<String, Object> crearTicketChatbot(@RequestBody Map<String, Object> ticketData,
                                                 Authentication auth,
                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("=== CHATBOT CREANDO TICKET ===");

            // Obtener datos del ticket desde el JSON
            String asunto = (String) ticketData.get("asunto");
            Integer idApi = Integer.valueOf(ticketData.get("idApi").toString());
            String descripcion = (String) ticketData.get("descripcion");
            String infoAdicional = (String) ticketData.get("infoAdicional");
            
            logger.info("Asunto: " + asunto);
            logger.info("ID API: " + idApi);
            
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
                ? "\n\n[Información adicional del chatbot]:\n" + infoAdicional : ""));
            ticket.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
            ticket.setEstado(true); // Estado activo
            ticket.setUsuario(usuario);
            ticket.setApi(api);
            
            // Guardar el ticket
            Ticket savedTicket = ticketRepository.save(ticket);
            logger.info("Ticket creado por chatbot con ID: " + savedTicket.getIdTicket());
            
            // Respuesta exitosa
            response.put("success", true);
            response.put("ticketId", savedTicket.getIdTicket());
            response.put("message", "Ticket creado exitosamente. ID: " + savedTicket.getIdTicket());
            
        } catch (Exception e) {
            logger.error("ERROR AL CREAR TICKET VIA CHATBOT: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * Endpoint para obtener la lista de APIs disponibles para el chatbot
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/soporte/chatbot/apis")
    @ResponseBody
    public Map<String, Object> obtenerApisParaChatbot(Authentication auth, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Api> apis = apiRepository.findAll();
            
            // Simplificar los datos de las APIs para el chatbot
            List<Map<String, Object>> apisSimplificadas = apis.stream().map(api -> {
                Map<String, Object> apiData = new HashMap<>();
                apiData.put("id", api.getIdApi());
                apiData.put("nombre", api.getNombre());
                apiData.put("descripcion", api.getDescripcion());
                return apiData;
            }).toList();
            
            response.put("success", true);
            response.put("apis", apisSimplificadas);
            
        } catch (Exception e) {
            logger.error("ERROR AL OBTENER APIS PARA CHATBOT: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * Procesa la creación de un nuevo ticket desde formulario web
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
