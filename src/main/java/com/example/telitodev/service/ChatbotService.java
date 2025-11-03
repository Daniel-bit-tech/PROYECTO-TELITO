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
        if (lower.matches(".*(hola|buenos días|buenas tardes|buenas noches|hey|hi|saludos).*")) {
            return "¡Hola! 👋 Soy el asistente virtual de Telito. Estoy aquí para ayudarte con:\n\n" +
                   "• Información sobre nuestras APIs\n" +
                   "• Documentación técnica\n" +
                   "• Solución de problemas comunes\n" +
                   "• Guías de integración\n\n" +
                   "¿En qué puedo ayudarte hoy?";
        }
        
        // Documentación de APIs
        if (lower.matches(".*(document|documentación|doc|api|endpoint|guía|tutorial|manual).*")) {
            return "📚 **Documentación de APIs**\n\n" +
                   "Puedes encontrar toda nuestra documentación técnica en la sección 'APIs' del portal:\n\n" +
                   "• Guías de inicio rápido\n" +
                   "• Referencias completas de endpoints\n" +
                   "• Ejemplos de código en múltiples lenguajes\n" +
                   "• Casos de uso comunes\n\n" +
                   "¿Necesitas información sobre alguna API específica?";
        }
        
        // API Key y autenticación
        if (lower.matches(".*(api key|clave|credencial|autenticación|token|acceso|permiso).*")) {
            return "🔐 **API Keys y Autenticación**\n\n" +
                   "Para obtener tu API Key:\n" +
                   "1. Ve a 'Onboarding Técnico' en tu dashboard\n" +
                   "2. Solicita una nueva API Key\n" +
                   "3. Se aprobará automáticamente según tus permisos\n\n" +
                   "Recuerda mantener tus credenciales seguras y nunca compartirlas públicamente.";
        }
        
        // Rate limits y errores 429
        if (lower.matches(".*(límite|rate|429|cuota|excedido|throttle|quota).*")) {
            return "⚡ **Rate Limiting**\n\n" +
                   "El error 429 indica que has excedido el límite de solicitudes.\n\n" +
                   "**Soluciones:**\n" +
                   "• Implementa exponential backoff en tus llamadas\n" +
                   "• Verifica los headers 'X-RateLimit-Remaining'\n" +
                   "• Consulta los límites en la documentación de cada API\n" +
                   "• Para aumentar tu cuota, contacta al equipo de QA";
        }
        
        // Ejemplos de código
        if (lower.matches(".*(ejemplo|código|code|sample|snippet|integr).*")) {
            return "💻 **Ejemplos de Código**\n\n" +
                   "En la documentación de cada API encontrarás ejemplos en:\n" +
                   "• cURL (línea de comandos)\n" +
                   "• JavaScript / Node.js\n" +
                   "• Python\n" +
                   "• Java\n" +
                   "• PHP\n\n" +
                   "También puedes copiar y pegar los snippets directamente desde el portal.";
        }
        
        // Sandbox y testing
        if (lower.matches(".*(sandbox|probar|test|prueba|demo|testing).*")) {
            return "🧪 **Sandbox y Pruebas**\n\n" +
                   "Usa nuestro entorno Sandbox para probar las APIs sin afectar datos de producción:\n\n" +
                   "• Datos de prueba precargados\n" +
                   "• Sin límites de rate\n" +
                   "• Endpoints idénticos a producción\n\n" +
                   "Ve a la sección 'Sandbox' en el menú principal para comenzar.";
        }
        
        // Errores comunes
        if (lower.matches(".*(error|fallo|no funciona|problema|bug|404|500|401|403).*")) {
            return "🔧 **Solución de Problemas**\n\n" +
                   "**Errores comunes:**\n" +
                   "• 401: Verifica tu API Key\n" +
                   "• 403: Revisa tus permisos\n" +
                   "• 404: Confirma la URL del endpoint\n" +
                   "• 500: Error del servidor (reintenta en unos minutos)\n\n" +
                   "¿Qué error específico estás experimentando?";
        }
        
        // Webhooks
        if (lower.matches(".*(webhook|notificación|evento|callback).*")) {
            return "🔔 **Webhooks**\n\n" +
                   "Configura webhooks para recibir notificaciones en tiempo real:\n\n" +
                   "• Eventos de creación, actualización y eliminación\n" +
                   "• Firma HMAC para validar autenticidad\n" +
                   "• Reintentos automáticos en caso de fallo\n\n" +
                   "Consulta la documentación de Webhooks para más detalles.";
        }
        
        // Versiones de API
        if (lower.matches(".*(versión|version|v1|v2|actualización|deprecado).*")) {
            return "📦 **Versionado de APIs**\n\n" +
                   "Actualmente soportamos múltiples versiones:\n\n" +
                   "• v1: Versión estable (recomendada)\n" +
                   "• v2: Versión beta con nuevas características\n\n" +
                   "Las versiones deprecadas se anuncian con 6 meses de anticipación.";
        }
        
        // Agradecimientos
        if (lower.matches(".*(gracias|thanks|thank you|bien|perfecto|excelente).*")) {
            return "¡De nada! 😊 Estoy aquí para ayudarte. Si tienes más preguntas, no dudes en escribirme.";
        }
        
        // Despedidas
        if (lower.matches(".*(adiós|chao|hasta luego|bye|goodbye|nos vemos).*")) {
            return "¡Hasta luego! 👋 Si necesitas ayuda en el futuro, aquí estaré. ¡Que tengas un excelente día!";
        }
        
        // Respuesta cuando NO puede ayudar - AQUÍ es cuando ofrece crear ticket
        return "🤔 Hmm, no estoy seguro de cómo ayudarte con eso específicamente.\n\n" +
               "**Opciones:**\n" +
               "1️⃣ Reformula tu pregunta con más detalles\n" +
               "2️⃣ Consulta la documentación en la sección 'APIs'\n" +
               "3️⃣ **Crea un ticket de soporte** para que el equipo de QA pueda ayudarte personalmente\n\n" +
               "Para crear un ticket, haz clic en el botón '➕ Nuevo Ticket' arriba.";
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
