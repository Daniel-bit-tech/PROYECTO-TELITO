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
import java.util.Optional;

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
            logger.info("📋 Cargando formulario de nuevo ticket...");
            
            // Obtener usuario actual
            Usuario usuario = getCurrentUser(auth, session);
            model.addAttribute("usuario", usuario);
            
            // Cargar la lista de APIs usando query nativa para evitar problemas con lazy loading
            List<Object[]> apisBasicas = apiRepository.findBasicApiInfo();
            logger.info("APIs cargadas: " + (apisBasicas != null ? apisBasicas.size() : "null"));
            
            // Convertir a una lista simple para el formulario
            List<Map<String, Object>> apisSimplificadas = new java.util.ArrayList<>();
            
            if (apisBasicas != null && !apisBasicas.isEmpty()) {
                for (Object[] row : apisBasicas) {
                    Map<String, Object> apiData = new HashMap<>();
                    apiData.put("idApi", row[0]);
                    apiData.put("nombre", row[1] != null ? row[1].toString() : "Sin nombre");
                    apiData.put("descripcion", row[2] != null ? row[2].toString() : "Sin descripción");
                    apisSimplificadas.add(apiData);
                }
            } else {
                logger.warn("No hay APIs disponibles en la base de datos");
            }
            
            model.addAttribute("apis", apisSimplificadas);
            
            // Agregar información de impersonación al modelo
            addImpersonationAttributes(model, session);
            
            logger.info("✅ Formulario cargado correctamente con " + apisSimplificadas.size() + " APIs");
            
            return "desarrollador/nuevo-ticket";
            
        } catch (Exception e) {
            logger.error("❌ ERROR al cargar formulario de nuevo ticket: " + e.getMessage(), e);
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
            logger.info("📦 Datos recibidos: " + ticketData);

            // Obtener datos del ticket desde el JSON
            String asunto = (String) ticketData.get("asunto");
            Object idApiObj = ticketData.get("idApi");
            logger.info("🔍 idApi recibido: " + idApiObj + " Tipo: " + (idApiObj != null ? idApiObj.getClass().getName() : "null"));
            
            Integer idApi = Integer.valueOf(idApiObj.toString());
            String descripcion = (String) ticketData.get("descripcion");
            String infoAdicional = (String) ticketData.get("infoAdicional");
            
            logger.info("📝 Asunto: " + asunto);
            logger.info("🔑 ID API convertido: " + idApi);
            
            // Obtener el usuario actual
            Usuario usuario = getCurrentUser(auth, session);
            logger.info("👤 Usuario: " + usuario.getDni());
            
            // Verificar que la API existe sin cargarla (evita problemas con relaciones LAZY)
            logger.info("🔍 Verificando existencia de API con ID: " + idApi);
            
            // Usar query nativa simple para verificar existencia
            Object[] apiInfo = apiRepository.findBasicApiInfo().stream()
                .filter(row -> row[0].equals(idApi))
                .findFirst()
                .orElse(null);
                
            if (apiInfo == null) {
                logger.error("❌ API no encontrada en la base de datos. ID buscado: " + idApi);
                throw new RuntimeException("API no encontrada");
            }
            
            logger.info("✅ API verificada: " + apiInfo[1] + " (ID: " + idApi + ")");
            
            // Crear la descripción completa
            String descripcionCompleta = descripcion + 
                (infoAdicional != null && !infoAdicional.isEmpty() 
                    ? "\n\n[Información adicional del chatbot]:\n" + infoAdicional 
                    : "");
            
            // Insertar el ticket directamente con query nativo para evitar problemas con relaciones de API
            logger.info("📝 Insertando ticket con query nativo...");
            ticketRepository.insertTicketNative(
                asunto,
                descripcionCompleta,
                new Timestamp(System.currentTimeMillis()),
                1,  // estado activo (1 para BIT(1) en MySQL)
                usuario.getDni(),
                idApi
            );
            
            logger.info("✅ Ticket creado exitosamente por chatbot");
            
            // Respuesta exitosa (no tenemos el ID generado con @Modifying, pero el ticket se creó)
            response.put("success", true);
            response.put("message", "Ticket creado exitosamente");
            
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
            logger.info("📡 Obteniendo lista de APIs para chatbot...");
            
            // Usar query nativa que solo trae los campos básicos sin tocar relaciones
            List<Object[]> apisBasicas = apiRepository.findBasicApiInfo();
            
            logger.info("✅ APIs encontradas: " + apisBasicas.size());
            
            // Convertir los resultados a Map
            List<Map<String, Object>> apisSimplificadas = new java.util.ArrayList<>();
            
            for (Object[] row : apisBasicas) {
                Map<String, Object> apiData = new HashMap<>();
                apiData.put("id", row[0]);
                apiData.put("nombre", row[1] != null ? row[1].toString() : "Sin nombre");
                apiData.put("descripcion", row[2] != null ? row[2].toString() : "Sin descripción");
                apisSimplificadas.add(apiData);
            }
            
            logger.info("✅ APIs procesadas correctamente: " + apisSimplificadas.size());
            
            response.put("success", true);
            response.put("apis", apisSimplificadas);
            
        } catch (Exception e) {
            logger.error("❌ ERROR AL OBTENER APIS PARA CHATBOT: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Error al cargar las APIs. Por favor, contacta al administrador.");
        }
        
        return response;
    }

    /**
     * Endpoint para obtener información contextual del usuario para el chatbot
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/soporte/chatbot/info-usuario")
    @ResponseBody
    public Map<String, Object> obtenerInfoUsuario(Authentication auth, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario usuario = getCurrentUser(auth, session);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("nombre", usuario.getNombre() + " " + (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : ""));
            userInfo.put("rol", usuario.getRol() != null ? usuario.getRol().getNombreRol() : "Desarrollador");
            
            // Información de organización
            if (usuario.getOrganizacion() != null) {
                Map<String, Object> orgInfo = new HashMap<>();
                orgInfo.put("nombre", usuario.getOrganizacion().getNombre());
                orgInfo.put("descripcion", usuario.getOrganizacion().getDescripcion());
                userInfo.put("organizacion", orgInfo);
            }
            
            // Información del equipo
            if (usuario.getEquipo() != null) {
                Map<String, Object> equipoInfo = new HashMap<>();
                equipoInfo.put("nombre", usuario.getEquipo().getNombre());
                userInfo.put("equipo", equipoInfo);
            }
            
            response.put("success", true);
            response.put("usuario", userInfo);
            
        } catch (Exception e) {
            logger.error("Error al obtener información del usuario", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * Endpoint para obtener los tickets del usuario
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/soporte/chatbot/mis-tickets")
    @ResponseBody
    public Map<String, Object> obtenerMisTickets(Authentication auth, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("📋 Obteniendo tickets del usuario...");
            Usuario usuario = getCurrentUser(auth, session);
            logger.info("👤 Usuario: " + usuario.getDni());
            
            List<Ticket> tickets = ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());
            logger.info("✅ Tickets encontrados: " + tickets.size());
            
            List<Map<String, Object>> ticketsSimplificados = new java.util.ArrayList<>();
            for (Ticket ticket : tickets) {
                try {
                    Map<String, Object> ticketData = new HashMap<>();
                    ticketData.put("id", ticket.getIdTicket());
                    ticketData.put("asunto", ticket.getAsunto());
                    ticketData.put("estado", ticket.getEstado() ? "Activo" : "Cerrado");
                    ticketData.put("fechaCreacion", ticket.getFechaCreacion() != null ? ticket.getFechaCreacion().toString() : "N/A");
                    
                    // Intentar obtener el nombre de la API de forma segura
                    String apiNombre = "N/A";
                    try {
                        if (ticket.getApi() != null) {
                            apiNombre = ticket.getApi().getNombre();
                        }
                    } catch (Exception apiEx) {
                        logger.warn("No se pudo cargar el nombre de la API para ticket " + ticket.getIdTicket());
                    }
                    ticketData.put("api", apiNombre);
                    
                    ticketsSimplificados.add(ticketData);
                } catch (Exception ticketEx) {
                    logger.error("Error al procesar ticket " + ticket.getIdTicket(), ticketEx);
                }
            }
            
            response.put("success", true);
            response.put("tickets", ticketsSimplificados);
            response.put("total", ticketsSimplificados.size());
            logger.info("✅ Tickets procesados: " + ticketsSimplificados.size());
            
        } catch (Exception e) {
            logger.error("❌ ERROR al obtener tickets del usuario: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * Endpoint para obtener miembros de la organización del usuario
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/soporte/chatbot/mi-organizacion")
    @ResponseBody
    public Map<String, Object> obtenerInfoOrganizacion(Authentication auth, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("🏢 Obteniendo información de organización...");
            Usuario usuario = getCurrentUser(auth, session);
            logger.info("👤 Usuario: " + usuario.getDni());
            
            if (usuario.getOrganizacion() == null) {
                logger.warn("⚠️ Usuario no tiene organización asignada");
                response.put("success", false);
                response.put("error", "No perteneces a ninguna organización");
                return response;
            }
            
            Integer orgId = usuario.getOrganizacion().getIdOrganizacion();
            logger.info("🏢 ID Organización: " + orgId);
            
            List<Usuario> miembros = usuarioRepository.findByOrganizacionIdWithRol(orgId);
            logger.info("✅ Miembros encontrados: " + miembros.size());
            
            Map<String, Object> orgInfo = new HashMap<>();
            orgInfo.put("nombre", usuario.getOrganizacion().getNombre());
            orgInfo.put("descripcion", usuario.getOrganizacion().getDescripcion() != null ? usuario.getOrganizacion().getDescripcion() : "Sin descripción");
            
            List<Map<String, Object>> miembrosSimplificados = new java.util.ArrayList<>();
            Usuario productOwner = null;
            
            for (Usuario miembro : miembros) {
                try {
                    Map<String, Object> miembroData = new HashMap<>();
                    String nombreCompleto = miembro.getNombre();
                    if (miembro.getApellidoPaterno() != null && !miembro.getApellidoPaterno().isEmpty()) {
                        nombreCompleto += " " + miembro.getApellidoPaterno();
                    }
                    miembroData.put("nombre", nombreCompleto);
                    miembroData.put("correo", miembro.getCorreo());
                    
                    String rolNombre = "Desarrollador";
                    try {
                        if (miembro.getRol() != null) {
                            rolNombre = miembro.getRol().getNombreRol();
                            if (rolNombre.equals("PO")) {
                                productOwner = miembro;
                                miembroData.put("esProductOwner", true);
                            }
                        }
                    } catch (Exception rolEx) {
                        logger.warn("No se pudo obtener rol del usuario " + miembro.getDni());
                    }
                    
                    miembroData.put("rol", rolNombre);
                    miembrosSimplificados.add(miembroData);
                } catch (Exception miembroEx) {
                    logger.error("Error al procesar miembro " + miembro.getDni(), miembroEx);
                }
            }
            
            orgInfo.put("miembros", miembrosSimplificados);
            orgInfo.put("totalMiembros", miembrosSimplificados.size());
            
            if (productOwner != null) {
                Map<String, Object> poInfo = new HashMap<>();
                poInfo.put("nombre", productOwner.getNombre() + " " + (productOwner.getApellidoPaterno() != null ? productOwner.getApellidoPaterno() : ""));
                poInfo.put("correo", productOwner.getCorreo());
                orgInfo.put("productOwner", poInfo);
            }
            
            response.put("success", true);
            response.put("organizacion", orgInfo);
            
        } catch (Exception e) {
            logger.error("Error al obtener información de la organización", e);
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
