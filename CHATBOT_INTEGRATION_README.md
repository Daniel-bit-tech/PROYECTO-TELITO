# 🤖 Integración del Chatbot AWS con TELITO Developer Portal

## ✅ Estado de la Implementación
**Completado** - El chatbot de AWS Elastic Beanstalk está integrado con el portal de desarrolladores.

## 📋 Componentes Implementados

### 1. **Backend - ChatbotService.java**
- **Ubicación:** `src/main/java/com/example/telitodev/service/ChatbotService.java`
- **Función:** Servicio que actúa como proxy entre el frontend y el chatbot de AWS
- **Características:**
  - Realiza llamadas HTTP POST al endpoint de AWS
  - Maneja errores y proporciona respuestas de respaldo (fallback)
  - Incluye logging detallado para debugging
  - Método `isServiceAvailable()` para verificar el estado del servicio

### 2. **Backend - SoporteController.java**
- **Ubicación:** `src/main/java/com/example/telitodev/controller/desarrollador/SoporteController.java`
- **Endpoints:**
  - `GET /dev/soporte` - Vista principal del centro de soporte
  - `POST /dev/soporte/chat` - Endpoint del chatbot
- **Características:**
  - Inyecta `ChatbotService` para comunicarse con AWS
  - Retorna JSON: `{ "reply": "...", "user": "..." }`
  - Incluye información del usuario autenticado

### 3. **Frontend - soporte.html**
- **Ubicación:** `src/main/resources/templates/desarrollador/soporte.html`
- **Características:**
  - Interfaz de chat moderna y responsive
  - Llamadas asíncronas con `fetch()` al backend
  - Manejo de CSRF token automático
  - Indicador visual "Escribiendo..." durante las peticiones
  - Manejo de errores con mensajes amigables

### 4. **Configuración - application.properties**
- **Nueva propiedad:**
```properties
chatbot.aws.url=https://ChatbotApp-env.eba-5bbtcarv.us-east-1.elasticbeanstalk.com/chat
```
- Permite cambiar la URL del chatbot sin modificar código

## 🔧 Configuración

### URL del Chatbot AWS
El servicio está configurado para usar:
```
https://ChatbotApp-env.eba-5bbtcarv.us-east-1.elasticbeanstalk.com/chat
```

Para cambiar la URL, edita `application.properties`:
```properties
chatbot.aws.url=https://tu-nuevo-endpoint.com/chat
```

## 🚀 Cómo Usar

### 1. Compilar el Proyecto
**En IntelliJ IDEA:**
- Ventana Maven → Lifecycle → `compile` o `package`

**Desde terminal PowerShell:**
```powershell
cd 'C:\Users\cs\Desktop\Nueva carpeta\PROYECTO-TELITO'
.\mvnw.cmd clean package -DskipTests
```

### 2. Ejecutar la Aplicación
**En IntelliJ:**
- Localiza la clase con `@SpringBootApplication`
- Click derecho → Run

**Desde terminal:**
```powershell
.\mvnw.cmd spring-boot:run
```

### 3. Acceder al Chatbot
1. Abre el navegador en: `http://localhost:8080`
2. Inicia sesión
3. Navega a: `http://localhost:8080/dev/soporte`
4. Escribe en el chat y prueba

## 🧪 Pruebas

### Probar el Endpoint Directamente
**Con PowerShell:**
```powershell
$body = @{ message = 'hola' } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/dev/soporte/chat' -Method Post -Body $body -ContentType 'application/json'
```

**Respuesta esperada:**
```json
{
  "reply": "Respuesta del chatbot de AWS",
  "user": "Nombre del usuario autenticado"
}
```

### Verificar Logs
El servicio genera logs detallados:
```
INFO  - Llamando al chatbot de AWS: https://... con mensaje: hola
INFO  - Respuesta del chatbot recibida: ...
```

En caso de error:
```
ERROR - Error al comunicarse con el chatbot de AWS: ...
```

## 🔄 Flujo de Datos

```
Usuario escribe en el chat
    ↓
Frontend (soporte.html)
    ↓ fetch('/dev/soporte/chat', { message: '...' })
    ↓
SoporteController.chat()
    ↓
ChatbotService.getChatbotResponse()
    ↓ HTTP POST
    ↓
AWS Elastic Beanstalk
    ↓ https://ChatbotApp-env.eba-5bbtcarv.us-east-1.elasticbeanstalk.com/chat
    ↓ { "message": "..." }
    ↓
Chatbot procesa y responde
    ↓ { "reply": "..." }
    ↓
ChatbotService retorna la respuesta
    ↓
SoporteController añade info del usuario
    ↓
Frontend muestra la respuesta en el chat
```

## 🛡️ Manejo de Errores

### Respuestas de Respaldo (Fallback)
Si el servicio de AWS no está disponible, `ChatbotService` proporciona respuestas básicas:
- Saludos
- Información sobre tickets
- Información sobre documentación
- Mensaje de error genérico

### Logs de Error
Todos los errores se registran con stack trace completo para debugging.

## 📝 Notas Importantes

1. **CORS:** El endpoint tiene `@CrossOrigin(origins = "*")` para facilitar pruebas. En producción, restringe los orígenes.

2. **CSRF:** El frontend incluye el token CSRF automáticamente en las peticiones POST.

3. **Timeout:** Por defecto, `RestTemplate` tiene un timeout infinito. Considera agregar configuración de timeout para producción.

4. **Seguridad:** El endpoint requiere autenticación (heredado de la configuración de Spring Security).

## 🔧 Mejoras Futuras (Opcional)

### 1. Configurar Timeout
```java
public ChatbotService() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000); // 5 segundos
    factory.setReadTimeout(10000);   // 10 segundos
    this.restTemplate = new RestTemplate(factory);
}
```

### 2. Implementar Caché
Cachear respuestas frecuentes para reducir llamadas a AWS:
```java
@Cacheable(value = "chatbot-responses", key = "#message")
public String getChatbotResponse(String message) {
    // ...
}
```

### 3. Circuit Breaker
Usar Resilience4j para evitar saturar el servicio de AWS en caso de fallos:
```java
@CircuitBreaker(name = "chatbot", fallbackMethod = "getFallbackResponse")
public String getChatbotResponse(String message) {
    // ...
}
```

### 4. Métricas
Agregar métricas para monitorear el rendimiento:
- Número de llamadas exitosas/fallidas
- Tiempo de respuesta promedio
- Tasa de uso del fallback

## 📞 Soporte

Si encuentras algún problema:
1. Revisa los logs de la aplicación
2. Verifica que el endpoint de AWS esté disponible
3. Prueba el endpoint directamente con PowerShell
4. Revisa la configuración en `application.properties`

## ✅ Checklist de Verificación

- [x] ChatbotService creado
- [x] SoporteController actualizado
- [x] Frontend integrado con fetch()
- [x] Configuración en application.properties
- [x] Manejo de errores implementado
- [x] Logging configurado
- [ ] Pruebas en ambiente local
- [ ] Despliegue en producción

---
**Última actualización:** 3 de noviembre de 2025
