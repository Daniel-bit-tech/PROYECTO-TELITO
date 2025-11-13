package com.example.telitodev.service;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio especializado para manejar la creación de tickets a través del chatbot.
 * Incluye lógica de procesamiento de lenguaje natural para extraer información de tickets.
 */
@Service
public class ChatbotTicketService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotTicketService.class);
    
    private final TicketRepository ticketRepository;
    private final ApiRepository apiRepository;
    
    public ChatbotTicketService(TicketRepository ticketRepository, ApiRepository apiRepository) {
        this.ticketRepository = ticketRepository;
        this.apiRepository = apiRepository;
    }

    /**
     * Analiza el mensaje del usuario para determinar si quiere crear un ticket
     */
    public boolean esIntentCrearTicket(String mensaje) {
        mensaje = mensaje.toLowerCase();
        String[] palabrasClave = {
            "crear ticket", "nuevo ticket", "reportar problema", "tengo un problema",
            "crear reporte", "reportar bug", "error en", "problema con",
            "no funciona", "ayuda con", "soporte para"
        };
        
        for (String palabra : palabrasClave) {
            if (mensaje.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrae información de un ticket desde el mensaje del usuario usando NLP básico
     */
    public Map<String, String> extraerInfoTicket(String mensaje) {
        Map<String, String> info = new HashMap<>();
        
        // Extraer asunto/título (primer tema mencionado)
        String asunto = extraerAsunto(mensaje);
        if (asunto != null) {
            info.put("asunto", asunto);
        }
        
        // Extraer descripción (usar todo el mensaje como base)
        info.put("descripcion", mensaje);
        
        // Extraer API mencionada
        String apiNombre = extraerApiMencionada(mensaje);
        if (apiNombre != null) {
            info.put("apiNombre", apiNombre);
        }
        
        return info;
    }

    /**
     * Extrae el asunto del mensaje
     */
    private String extraerAsunto(String mensaje) {
        // Patrones para identificar asuntos comunes
        Pattern[] patrones = {
            Pattern.compile("problema con (.+?)(?:\\.|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("error en (.+?)(?:\\.|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("no funciona (.+?)(?:\\.|$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ayuda con (.+?)(?:\\.|$)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patrones) {
            Matcher matcher = pattern.matcher(mensaje);
            if (matcher.find()) {
                String asunto = matcher.group(1).trim();
                return "Problema con " + asunto;
            }
        }
        
        // Si no encuentra un patrón específico, generar asunto genérico
        if (mensaje.length() > 50) {
            return "Soporte: " + mensaje.substring(0, 47) + "...";
        } else {
            return "Solicitud de soporte: " + mensaje;
        }
    }

    /**
     * Busca si se menciona alguna API específica en el mensaje
     */
    private String extraerApiMencionada(String mensaje) {
        List<Api> apis = apiRepository.findAll();
        mensaje = mensaje.toLowerCase();
        
        for (Api api : apis) {
            String nombreApi = api.getNombre().toLowerCase();
            if (mensaje.contains(nombreApi)) {
                return api.getNombre();
            }
        }
        return null;
    }

    /**
     * Encuentra la API por nombre (usado por el chatbot)
     */
    public Api buscarApiPorNombre(String nombreApi) {
        List<Api> apis = apiRepository.findAll();
        
        for (Api api : apis) {
            if (api.getNombre().equalsIgnoreCase(nombreApi)) {
                return api;
            }
        }
        
        // Buscar coincidencia parcial
        for (Api api : apis) {
            if (api.getNombre().toLowerCase().contains(nombreApi.toLowerCase()) ||
                nombreApi.toLowerCase().contains(api.getNombre().toLowerCase())) {
                return api;
            }
        }
        
        return null;
    }

    /**
     * Crea un ticket basado en la información extraída
     */
    public Ticket crearTicketDesdeInfo(Map<String, String> info, Usuario usuario, Api api) {
        Ticket ticket = new Ticket();
        
        // Configurar campos básicos
        ticket.setAsunto(info.getOrDefault("asunto", "Solicitud de soporte"));
        ticket.setDescripcion(info.getOrDefault("descripcion", "Sin descripción") + 
                            "\n\n[Ticket creado automáticamente por el chatbot]");
        ticket.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        ticket.setEstado(true); // Activo
        ticket.setUsuario(usuario);
        ticket.setApi(api);
        
        return ticketRepository.save(ticket);
    }

    /**
     * Obtiene una lista simplificada de APIs para mostrar al usuario
     */
    public List<Map<String, Object>> obtenerApisDisponibles() {
        List<Api> apis = apiRepository.findAll();
        List<Map<String, Object>> apisSimplificadas = new ArrayList<>();
        
        for (Api api : apis) {
            Map<String, Object> apiInfo = new HashMap<>();
            apiInfo.put("id", api.getIdApi());
            apiInfo.put("nombre", api.getNombre());
            apiInfo.put("descripcion", api.getDescripcion());
            apisSimplificadas.add(apiInfo);
        }
        
        return apisSimplificadas;
    }

    /**
     * Genera respuestas del chatbot para el proceso de creación de tickets
     */
    public String generarRespuestaChatbot(String contexto, Map<String, String> infoTicket) {
        switch (contexto) {
            case "confirmar_creacion":
                return "¡Perfecto! Voy a crear un ticket de soporte para ti. " +
                       "He detectado que el problema es: " + infoTicket.get("asunto") + 
                       ". ¿Es correcto?";
                       
            case "seleccionar_api":
                return "Necesito saber qué API está relacionada con tu problema. " +
                       "¿Podrías decirme el nombre de la API o seleccionar una de la lista?";
                       
            case "ticket_creado":
                return "¡Excelente! He creado el ticket #" + infoTicket.get("ticketId") + 
                       " para tu problema. Un miembro del equipo de QA lo revisará pronto. " +
                       "Puedes ver el estado del ticket en la sección de soporte.";
                       
            case "error_creacion":
                return "Lo siento, hubo un problema al crear el ticket. " +
                       "¿Podrías intentar nuevamente o contactar directamente al soporte?";
                       
            default:
                return "¿En qué puedo ayudarte hoy? Puedo ayudarte a crear un ticket de soporte " +
                       "si tienes algún problema con las APIs.";
        }
    }
}
