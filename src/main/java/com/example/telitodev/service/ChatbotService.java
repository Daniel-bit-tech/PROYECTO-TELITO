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
     * Respuesta inteligente del chatbot local con personalidad.
     *
     * @param message Mensaje original del usuario
     * @return Respuesta del chatbot
     */
    private String getFallbackResponse(String message) {
        String lower = message.toLowerCase().trim();
        
        // ============ CONVERSACIÓN PERSONAL Y SOCIAL ============
        
        // Nombre del chatbot
        if (lower.matches(".*(cómo te llamas|como te llamas|tu nombre|quién eres|quien eres|eres un bot).*")) {
            return "¡Hola! 😊 Me llamo **Telito Bot**, soy el asistente virtual de Telito Developer Portal.\n\n" +
                   "Fui creado para ayudarte con todo lo relacionado a nuestras APIs, documentación técnica y resolver tus dudas.\n\n" +
                   "¿En qué puedo ayudarte hoy?";
        }
        
        // ¿Cómo estás? / ¿Qué tal?
        if (lower.matches(".*(cómo estás|como estas|que tal|qué tal|como te va|todo bien).*")) {
            return "¡Estoy excelente, gracias por preguntar! 🤖✨\n\n" +
                   "Como asistente virtual, siempre estoy listo para ayudarte 24/7. No me canso y me encanta resolver dudas.\n\n" +
                   "Y tú, ¿cómo estás? ¿Hay algo en lo que pueda ayudarte con las APIs de Telito?";
        }
        
        // ¿Cómo ha sido tu día?
        if (lower.matches(".*(tu día|tu dia|como ha ido|que has hecho|como va el dia).*")) {
            return "Mi día ha sido muy productivo! 📊 He estado ayudando a muchos desarrolladores como tú.\n\n" +
                   "He respondido preguntas sobre APIs, ayudado con integraciones y hasta resuelto algunos errores complicados.\n\n" +
                   "¿Y el tuyo? ¿En qué proyecto estás trabajando hoy?";
        }
        
        // Chistes o humor
        if (lower.matches(".*(chiste|broma|algo gracioso|hazme reir|haz me reir|bromea).*")) {
            return "¡Claro! 😄 Aquí va uno:\n\n" +
                   "¿Por qué los programadores prefieren el modo oscuro?\n\n" +
                   "Porque la luz atrae bugs! 🐛💡\n\n" +
                   "Jajaja... ¿Listo para seguir programando? ¿En qué puedo ayudarte?";
        }
        
        // Conversación casual (aburrimiento, charla)
        if (lower.matches(".*(aburrido|charlar|conversar|platica|habla conmigo).*")) {
            return "Me encantaría charlar contigo! 💬 Aunque mi fuerte es ayudarte con las APIs de Telito.\n\n" +
                   "Puedo contarte sobre:\n" +
                   "• Las APIs más populares de Telito\n" +
                   "• Tips de integración\n" +
                   "• Buenas prácticas de desarrollo\n\n" +
                   "O si prefieres, cuéntame: ¿en qué proyecto estás trabajando?";
        }
        
        // Edad / tiempo de existencia
        if (lower.matches(".*(cuántos años|cuantos anos|tu edad|edad tienes|cuando naciste).*")) {
            return "Soy bastante nuevo! 🎂 Fui creado recientemente para el Telito Developer Portal.\n\n" +
                   "Aunque soy joven, he sido entrenado con mucha información sobre nuestras APIs y servicios.\n\n" +
                   "Cada día aprendo más de las interacciones con desarrolladores como tú. ¿Quieres probarme con alguna pregunta técnica?";
        }
        
        // Qué puedes hacer / capacidades
        if (lower.matches(".*(qué puedes hacer|que puedes hacer|para qué sirves|para que sirves|tus capacidades).*")) {
            return "¡Puedo ayudarte con muchas cosas! 🚀\n\n" +
                   "**Mis especialidades:**\n" +
                   "✅ Explicar cómo funcionan nuestras APIs\n" +
                   "✅ Ayudarte a obtener API Keys\n" +
                   "✅ Resolver errores comunes (401, 403, 429, 500)\n" +
                   "✅ Guiarte en integraciones\n" +
                   "✅ Responder dudas sobre documentación\n" +
                   "✅ Explicar el uso del Sandbox\n\n" +
                   "Y también puedo conversar un poco! 😊 ¿Con qué empezamos?";
        }
        
        // ============ SALUDOS Y BIENVENIDAS ============
        
        // Saludos
        if (lower.matches(".*(hola|buenos días|buenas tardes|buenas noches|hey|hi|saludos).*")) {
            return "¡Hola! 👋 Soy **Telito Bot**, tu asistente virtual.\n\n" +
                   "Estoy aquí para ayudarte con:\n" +
                   "• Información sobre nuestras APIs\n" +
                   "• Documentación técnica\n" +
                   "• Solución de problemas comunes\n" +
                   "• Guías de integración\n\n" +
                   "¿En qué puedo ayudarte hoy?";
        }
        
        // ============ DOCUMENTACIÓN Y APIs ============
        
        // Documentación de APIs
        if (lower.matches(".*(document|documentación|doc|api|endpoint|guía|tutorial|manual).*")) {
            return "📚 **Documentación de APIs**\n\n" +
                   "Puedes encontrar toda nuestra documentación técnica en la sección 'APIs' del portal:\n\n" +
                   "🔗 <a href='/apis' target='_blank' style='color: #0066cc;'>Ver todas las APIs disponibles</a>\n\n" +
                   "• Guías de inicio rápido\n" +
                   "• Referencias completas de endpoints\n" +
                   "• Ejemplos de código en múltiples lenguajes\n" +
                   "• Casos de uso comunes\n\n" +
                   "¿Necesitas información sobre alguna API específica?";
        }
        
        // API Key y autenticación
        if (lower.matches(".*(api key|clave|credencial|autenticación|token|acceso|permiso).*")) {
            return "🔐 **API Keys y Autenticación**\n\n" +
                   "Para obtener tu API Key:\n\n" +
                   "🔗 <a href='/dev/onboarding' target='_blank' style='color: #0066cc;'>Ir a Onboarding Técnico</a>\n\n" +
                   "**Pasos:**\n" +
                   "1. Solicita una nueva API Key desde el portal\n" +
                   "2. Se aprobará automáticamente según tus permisos\n" +
                   "3. Copia tu clave y guárdala de forma segura\n\n" +
                   "⚠️ **Importante:** Nunca compartas tus credenciales públicamente ni las incluyas en tu código fuente.\n\n" +
                   "**Ejemplo de uso en headers:**\n" +
                   "<pre style='background: #f5f5f5; padding: 10px; border-radius: 5px;'>" +
                   "Authorization: Bearer YOUR_API_KEY\n" +
                   "Content-Type: application/json" +
                   "</pre>";
        }
        
        // Rate limits y errores 429
        if (lower.matches(".*(límite|rate|429|cuota|excedido|throttle|quota).*")) {
            return "⚡ **Rate Limiting**\n\n" +
                   "El error **429 Too Many Requests** indica que has excedido el límite de solicitudes permitidas.\n\n" +
                   "**Soluciones implementables:**\n\n" +
                   "**1. Exponential Backoff (Recomendado)**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px;'>" +
                   "async function fetchWithRetry(url, options, maxRetries = 3) {\n" +
                   "  for (let i = 0; i < maxRetries; i++) {\n" +
                   "    try {\n" +
                   "      const response = await fetch(url, options);\n" +
                   "      if (response.status === 429) {\n" +
                   "        const delay = Math.pow(2, i) * 1000; // 1s, 2s, 4s\n" +
                   "        await new Promise(r => setTimeout(r, delay));\n" +
                   "        continue;\n" +
                   "      }\n" +
                   "      return response;\n" +
                   "    } catch (error) {\n" +
                   "      if (i === maxRetries - 1) throw error;\n" +
                   "    }\n" +
                   "  }\n" +
                   "}" +
                   "</pre>\n\n" +
                   "**2. Verifica los Headers**\n" +
                   "• `X-RateLimit-Limit`: Límite total\n" +
                   "• `X-RateLimit-Remaining`: Requests restantes\n" +
                   "• `X-RateLimit-Reset`: Timestamp de reinicio\n\n" +
                   "**3. Optimiza tus llamadas**\n" +
                   "• Implementa caching cuando sea posible\n" +
                   "• Agrupa múltiples requests en batch operations\n" +
                   "• Usa webhooks en lugar de polling constante\n\n" +
                   "🔗 <a href='/apis' style='color: #0066cc;'>Ver límites por API</a>\n\n" +
                   "💡 **¿Necesitas más cuota?** <a href='/dev/soporte/nuevo-ticket' style='color: #0066cc;'>Contacta al equipo de QA</a>";
        }
        
        // Ejemplos de código
        if (lower.matches(".*(ejemplo|código|code|sample|snippet|integr).*")) {
            return "💻 **Ejemplos de Código**\n\n" +
                   "Te muestro ejemplos de cómo consumir nuestras APIs:\n\n" +
                   "**JavaScript / Node.js:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px; overflow-x: auto;'>" +
                   "const response = await fetch('https://api.telito.com/v1/endpoint', {\n" +
                   "  method: 'GET',\n" +
                   "  headers: {\n" +
                   "    'Authorization': 'Bearer YOUR_API_KEY',\n" +
                   "    'Content-Type': 'application/json'\n" +
                   "  }\n" +
                   "});\n" +
                   "const data = await response.json();\n" +
                   "console.log(data);" +
                   "</pre>\n\n" +
                   "**Python:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px; overflow-x: auto;'>" +
                   "import requests\n\n" +
                   "headers = {\n" +
                   "    'Authorization': 'Bearer YOUR_API_KEY',\n" +
                   "    'Content-Type': 'application/json'\n" +
                   "}\n" +
                   "response = requests.get('https://api.telito.com/v1/endpoint', headers=headers)\n" +
                   "data = response.json()\n" +
                   "print(data)" +
                   "</pre>\n\n" +
                   "**cURL:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px; overflow-x: auto;'>" +
                   "curl -X GET 'https://api.telito.com/v1/endpoint' \\\n" +
                   "  -H 'Authorization: Bearer YOUR_API_KEY' \\\n" +
                   "  -H 'Content-Type: application/json'" +
                   "</pre>\n\n" +
                   "🔗 <a href='/dev/sandbox' target='_blank' style='color: #0066cc;'>Probar en Sandbox</a> | " +
                   "<a href='/apis' target='_blank' style='color: #0066cc;'>Ver más ejemplos</a>";
        }
        
        // Sandbox y testing
        if (lower.matches(".*(sandbox|probar|test|prueba|demo|testing).*")) {
            return "🧪 **Sandbox y Pruebas**\n\n" +
                   "🔗 <a href='/dev/sandbox' target='_blank' style='color: #0066cc; font-weight: bold;'>Abrir Sandbox</a>\n\n" +
                   "Usa nuestro entorno Sandbox para probar las APIs sin afectar datos de producción:\n\n" +
                   "✅ Datos de prueba precargados\n" +
                   "✅ Sin límites de rate\n" +
                   "✅ Endpoints idénticos a producción\n" +
                   "✅ Respuestas en tiempo real\n\n" +
                   "**Ejemplo de prueba rápida:**\n" +
                   "<pre style='background: #f5f5f5; padding: 10px; border-radius: 5px;'>" +
                   "Endpoint: GET /sandbox/test\n" +
                   "Response: { \"status\": \"ok\", \"message\": \"Sandbox funcionando\" }" +
                   "</pre>";
        }
        
        // Errores comunes
        if (lower.matches(".*(error|fallo|no funciona|problema|bug|404|500|401|403).*")) {
            return "🔧 **Solución de Problemas**\n\n" +
                   "**Errores HTTP comunes:**\n\n" +
                   "❌ **401 Unauthorized**\n" +
                   "→ Tu API Key es inválida o está expirada\n" +
                   "→ <a href='/dev/onboarding' style='color: #0066cc;'>Genera una nueva API Key</a>\n\n" +
                   "❌ **403 Forbidden**\n" +
                   "→ No tienes permisos para este endpoint\n" +
                   "→ <a href='/dev/perfil' style='color: #0066cc;'>Revisa tus permisos</a>\n\n" +
                   "❌ **404 Not Found**\n" +
                   "→ El endpoint no existe o la URL es incorrecta\n" +
                   "→ <a href='/apis' style='color: #0066cc;'>Verifica la documentación</a>\n\n" +
                   "❌ **429 Too Many Requests**\n" +
                   "→ Has excedido el rate limit\n" +
                   "→ Implementa exponential backoff\n\n" +
                   "❌ **500 Internal Server Error**\n" +
                   "→ Error en nuestros servidores (reintenta en unos minutos)\n" +
                   "→ <a href='/dev/soporte/nuevo-ticket' style='color: #0066cc;'>Crear ticket de soporte</a>\n\n" +
                   "**Ejemplo de manejo de errores:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px;'>" +
                   "try {\n" +
                   "  const response = await fetch(url);\n" +
                   "  if (!response.ok) {\n" +
                   "    throw new Error(`HTTP ${response.status}`);\n" +
                   "  }\n" +
                   "  return await response.json();\n" +
                   "} catch (error) {\n" +
                   "  console.error('Error:', error);\n" +
                   "}" +
                   "</pre>\n\n" +
                   "¿Qué error específico estás experimentando?";
        }
        
        // Webhooks
        if (lower.matches(".*(webhook|notificación|evento|callback).*")) {
            return "🔔 **Webhooks**\n\n" +
                   "Configura webhooks para recibir notificaciones en tiempo real de eventos en tus APIs:\n\n" +
                   "**Eventos disponibles:**\n" +
                   "✅ Creación de recursos\n" +
                   "✅ Actualizaciones\n" +
                   "✅ Eliminaciones\n" +
                   "✅ Cambios de estado\n\n" +
                   "**Ejemplo de configuración:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px;'>" +
                   "POST /v1/webhooks\n" +
                   "{\n" +
                   "  \"url\": \"https://tu-servidor.com/webhook\",\n" +
                   "  \"events\": [\"resource.created\", \"resource.updated\"],\n" +
                   "  \"secret\": \"tu_secret_para_firmas\"\n" +
                   "}" +
                   "</pre>\n\n" +
                   "**Validar firma HMAC:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px;'>" +
                   "const crypto = require('crypto');\n\n" +
                   "function validateWebhook(payload, signature, secret) {\n" +
                   "  const hmac = crypto.createHmac('sha256', secret);\n" +
                   "  const digest = hmac.update(payload).digest('hex');\n" +
                   "  return digest === signature;\n" +
                   "}" +
                   "</pre>\n\n" +
                   "🔗 <a href='/apis' style='color: #0066cc;'>Ver documentación completa de Webhooks</a>\n\n" +
                   "⚠️ **Importante:** Los webhooks tienen reintentos automáticos (3 intentos) en caso de fallo.";
        }
        
        // Versiones de API
        if (lower.matches(".*(versión|version|v1|v2|actualización|deprecado).*")) {
            return "📦 **Versionado de APIs**\n\n" +
                   "Actualmente soportamos múltiples versiones de nuestras APIs:\n\n" +
                   "**v1 (Estable - Recomendada)**\n" +
                   "• Versión de producción\n" +
                   "• Totalmente soportada\n" +
                   "• Garantía de compatibilidad\n" +
                   "• Base URL: `https://api.telito.com/v1/`\n\n" +
                   "**v2 (Beta)**\n" +
                   "• Nuevas características experimentales\n" +
                   "• Puede tener cambios sin previo aviso\n" +
                   "• Base URL: `https://api.telito.com/v2/`\n\n" +
                   "**Ejemplo de uso:**\n" +
                   "<pre style='background: #282c34; color: #abb2bf; padding: 15px; border-radius: 5px;'>" +
                   "// Usando v1 (recomendado)\n" +
                   "fetch('https://api.telito.com/v1/endpoint', {\n" +
                   "  headers: { 'Authorization': 'Bearer YOUR_KEY' }\n" +
                   "})\n\n" +
                   "// Usando v2 (experimental)\n" +
                   "fetch('https://api.telito.com/v2/endpoint', {\n" +
                   "  headers: { 'Authorization': 'Bearer YOUR_KEY' }\n" +
                   "})" +
                   "</pre>\n\n" +
                   "🔗 <a href='/apis' style='color: #0066cc;'>Ver changelog de versiones</a>\n\n" +
                   "⚠️ **Política de deprecación:** Las versiones deprecadas se anuncian con 6 meses de anticipación.";
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
               "**Opciones disponibles:**\n\n" +
               "1️⃣ **Reformula tu pregunta** con más detalles técnicos\n\n" +
               "2️⃣ **Consulta la documentación:**\n" +
               "   🔗 <a href='/apis' style='color: #0066cc;'>Ver todas las APIs</a>\n" +
               "   🔗 <a href='/dev/sandbox' style='color: #0066cc;'>Probar en Sandbox</a>\n\n" +
               "3️⃣ **Crea un ticket de soporte** para ayuda personalizada:\n" +
               "   🔗 <a href='/dev/soporte/nuevo-ticket' style='color: #0066cc; font-weight: bold;'>➕ Crear Nuevo Ticket</a>\n\n" +
               "Un miembro del equipo de QA te responderá pronto. También puedes intentar preguntas como:\n" +
               "• \"¿Cómo obtengo una API Key?\"\n" +
               "• \"Muéstrame ejemplos de código\"\n" +
               "• \"¿Cómo resolver error 401?\"";
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
