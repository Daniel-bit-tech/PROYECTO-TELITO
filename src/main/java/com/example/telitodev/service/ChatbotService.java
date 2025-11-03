package com.example.telitodev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para comunicarse con el chatbot desplegado en AWS Elastic Beanstalk.
 * Hace de proxy entre el frontend y el servicio de chatbot externo.
 */
@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${chatbot.aws.url:https://ChatbotApp-env.eba-5bbtcarv.us-east-1.elasticbeanstalk.com/chat}")
    private String chatbotUrl;

    private final RestTemplate restTemplate;

    public ChatbotService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía un mensaje al chatbot y retorna la respuesta.
     * NOTA: Por ahora usa lógica local en lugar de llamar a un servicio externo
     * para evitar problemas de conectividad.
     *
     * @param message Mensaje del usuario
     * @return Respuesta del chatbot
     */
    public String getChatbotResponse(String message) {
        logger.info("Procesando mensaje del chatbot: {}", message);
        
        // Por ahora usamos directamente la lógica local
        // En el futuro esto puede llamar a un servicio de IA externo
        return getFallbackResponse(message);
    }

    /**
     * Respuesta inteligente del chatbot local.
     *
     * @param message Mensaje original del usuario
     * @return Respuesta del chatbot
     */
    private String getFallbackResponse(String message) {
        String lower = message.toLowerCase().trim();
        
        // Saludos
        if (lower.matches(".*(hola|buenos días|buenas tardes|buenas noches|hey|hi).*")) {
            return "¡Hola! Soy el asistente virtual de Telito. ¿En qué puedo ayudarte? " +
                   "Puedo ayudarte con información sobre APIs, documentación, crear tickets de soporte, " +
                   "o resolver dudas técnicas.";
        }
        
        // Tickets y soporte
        if (lower.matches(".*(ticket|soporte|problema|error|ayuda|issue).*")) {
            return "Puedo ayudarte a crear un ticket de soporte. Haz clic en el botón 'Nuevo Ticket' " +
                   "en el panel derecho, o cuéntame qué problema estás experimentando y te guiaré en el proceso.";
        }
        
        // Documentación
        if (lower.matches(".*(document|documentación|doc|api|endpoint|guía|tutorial).*")) {
            return "Puedes encontrar toda nuestra documentación en la sección 'APIs' del portal. " +
                   "Tenemos guías completas, ejemplos de código y referencias de API. " +
                   "¿Qué API específica te interesa?";
        }
        
        // API Key
        if (lower.matches(".*(api key|clave|credencial|autenticación|token).*")) {
            return "Para obtener tu API Key, ve a la sección 'Onboarding Técnico' en tu dashboard. " +
                   "Allí podrás solicitar una API Key que se aprobará automáticamente según tus permisos.";
        }
        
        // Rate limits
        if (lower.matches(".*(límite|rate|429|cuota|excedido).*")) {
            return "Los límites de rate-limiting varían según tu plan. El error 429 indica que has " +
                   "excedido el límite. Puedes consultar los límites específicos en la documentación " +
                   "de cada API o contactar para aumentar tu cuota.";
        }
        
        // Ejemplos de código
        if (lower.matches(".*(ejemplo|código|code|sample|snippet).*")) {
            return "En la documentación de cada API encontrarás ejemplos de código en múltiples lenguajes: " +
                   "curl, JavaScript, Python, Java y más. También puedes usar el 'Sandbox' para probar " +
                   "las APIs directamente.";
        }
        
        // Sandbox
        if (lower.matches(".*(sandbox|probar|test|prueba).*")) {
            return "Puedes usar nuestro Sandbox para probar las APIs sin afectar datos de producción. " +
                   "Ve a la sección 'Sandbox' en el menú principal para comenzar.";
        }
        
        // Agradecimientos
        if (lower.matches(".*(gracias|thanks|thank you).*")) {
            return "¡De nada! Si tienes más preguntas, no dudes en escribirme. " +
                   "También puedes crear un ticket si necesitas asistencia más especializada.";
        }
        
        // Despedidas
        if (lower.matches(".*(adiós|chao|hasta luego|bye|goodbye).*")) {
            return "¡Hasta luego! Si necesitas ayuda en el futuro, aquí estaré. ¡Que tengas un excelente día!";
        }
        
        // Respuesta por defecto
        return "Entiendo que tienes una consulta sobre: \"" + message + "\". " +
               "Puedo ayudarte con temas de APIs, documentación, tickets de soporte y más. " +
               "¿Podrías ser más específico sobre lo que necesitas, o prefieres crear un ticket " +
               "para que un agente especializado te asista?";
    }

    /**
     * Verifica la disponibilidad del servicio de chatbot.
     *
     * @return true si el servicio está disponible
     */
    public boolean isServiceAvailable() {
        try {
            Map<String, String> testPayload = new HashMap<>();
            testPayload.put("message", "test");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(testPayload, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)(ResponseEntity<?>)restTemplate.exchange(
                    chatbotUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("Servicio de chatbot no disponible: {}", e.getMessage());
            return false;
        }
    }
}
