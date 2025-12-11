# 🤖 Guía de Merge - Chatbot Flotante con Historial

## 📋 Resumen de Cambios

Esta rama (`pedroRama`) implementa un chatbot flotante global con persistencia de historial en todo el portal de desarrolladores.

### ✨ Características Implementadas

- ✅ Chatbot flotante en esquina inferior derecha
- ✅ Disponible en todas las vistas del portal de desarrolladores
- ✅ Historial de conversación persistente durante la sesión (sessionStorage)
- ✅ Respuestas inteligentes con enlaces y ejemplos de código
- ✅ Se limpia automáticamente al hacer logout

---

## 📁 Archivos Modificados/Creados

### 1. **`src/main/resources/templates/desarrollador/footer.html`** ⭐ NUEVO/CRÍTICO

**Estado:** Archivo completamente nuevo y crítico para el funcionamiento.

**Descripción:** Componente reutilizable que contiene:
- Footer del portal
- Chatbot flotante (HTML + CSS + JavaScript)
- Lógica de persistencia con sessionStorage

**🔴 ACCIÓN REQUERIDA EN MERGE:**
```
MANTENER COMPLETO - Este archivo NO debe ser modificado
Si no existe en la rama destino: AGREGAR COMPLETO
Si existe: REEMPLAZAR con esta versión
```

**Contenido clave:**
- Estilos CSS con prefijo `-global` para evitar conflictos
- JavaScript encapsulado en IIFE
- Key de sessionStorage: `telito_chatbot_history`
- Botón flotante con ID: `chat-fab-global`
- Contenedor del chat con ID: `chat-container-global`

---

### 2. **`src/main/java/com/example/telitodev/service/ChatbotService.java`** ⭐ MODIFICADO

**Estado:** Archivo existente con mejoras significativas.

**🔴 ACCIÓN REQUERIDA EN MERGE:**

#### Mantener TODO el método `getFallbackResponse()` (líneas ~48-250)

Este método contiene las respuestas inteligentes del chatbot:

```java
private String getFallbackResponse(String message) {
    String lower = message.toLowerCase().trim();
    
    // ============ CONVERSACIÓN PERSONAL Y SOCIAL ============
    // Respuestas a: nombre, cómo estás, chistes, edad, capacidades
    
    // ============ SALUDOS Y BIENVENIDAS ============
    // Respuestas a: hola, buenos días, etc.
    
    // ============ DOCUMENTACIÓN Y APIs ============
    // Respuestas con enlaces a /apis, /dev/onboarding, /dev/sandbox
    // Incluye ejemplos de código en JavaScript, Python, cURL
    // Snippets con HTML <pre> tags para sintaxis coloreada
    
    // ============ SOLUCIÓN DE PROBLEMAS ============
    // Rate limiting, errores HTTP, webhooks, versiones
    
    // Respuesta por defecto con enlaces de ayuda
}
```

**Características que NO deben perderse:**
- ✅ Enlaces HTML con rutas correctas: `/apis`, `/dev/sandbox`, `/dev/onboarding`, `/dev/soporte/nuevo-ticket`
- ✅ Bloques de código con estilos CSS inline (`<pre style='background: #282c34;...'>`)
- ✅ Respuestas conversacionales (nombre, chistes, cómo estás)
- ✅ Ejemplos de código completos en JavaScript, Python, cURL
- ✅ Manejo de errores HTTP con enlaces de solución

**⚠️ CONFLICTO POTENCIAL:**
Si en la otra rama hay cambios en el endpoint del chatbot o en la URL de AWS:
```java
@Value("${chatbot.aws.url:https://ChatbotApp-env.eba-5bbtcarv.us-east-1.elasticbeanstalk.com/chat}")
private String chatbotUrl;
```
**SOLUCIÓN:** Mantener la URL de la otra rama si es diferente, pero conservar TODO el método `getFallbackResponse()`.

---

### 3. **`src/main/resources/templates/desarrollador/soporte.html`** 🔄 MODIFICADO

**🔴 ACCIÓN REQUERIDA EN MERGE:**

**ELIMINAR** el chatbot inline antiguo si existe, y **AGREGAR** al final del archivo (antes de `</body>`):

```html
<!-- Footer y Chatbot -->
<div th:replace="~{desarrollador/footer :: footer}"></div>
</body>
</html>
```

**⚠️ NO mantener:**
- ❌ Código de chatbot duplicado en el HTML
- ❌ Estilos CSS inline para chatbot antiguo
- ❌ JavaScript inline del chatbot viejo

---

### 4. **Todas las vistas del portal de desarrolladores** 🔄 MODIFICADAS

Las siguientes vistas fueron actualizadas para incluir el footer con chatbot:

```
✅ developer.html
✅ sandbox.html
✅ nuevo-ticket.html
✅ cambiar-password.html
✅ perfil.html
✅ onboarding.html
✅ metricas.html
✅ feedback.html
✅ apis.html
✅ proyectos.html
✅ proyecto-detalle.html
✅ issueDevDetalle.html
✅ issuesDev.html
```

**🔴 ACCIÓN REQUERIDA EN MERGE:**

En **CADA UNA** de estas vistas, agregar antes de `</body>`:

```html
<!-- Footer y Chatbot -->
<div th:replace="~{desarrollador/footer :: footer}"></div>
</body>
</html>
```

**⚠️ VERIFICAR:**
- Si en la otra rama agregaron nuevas vistas de desarrollador, también incluir el footer
- Si cambiaron la estructura de alguna vista, adaptar pero mantener la inclusión del footer

---

## 🔧 Controladores y Endpoints

### **`SoporteController.java`** - Sin cambios críticos

El endpoint del chatbot ya existe:
```java
@PostMapping("/dev/soporte/chat")
@ResponseBody
public Map<String, String> chat(@RequestBody Map<String, String> payload, ...)
```

**🟢 ACCIÓN:** Este controlador no tiene conflictos, mantener como está.

---

## 🎨 Estilos CSS - Namespacing

Todos los estilos del chatbot usan el sufijo `-global` para evitar conflictos:

```css
.chat-fab
.chat-container-global
.chat-header-global
.chat-messages-global
.chat-input-global
.message-global
.message-user-global
.message-bot-global
```

**🟢 VENTAJA:** No hay riesgo de conflictos CSS con otros componentes.

---

## 🔑 Variables JavaScript - Scope Aislado

El JavaScript del chatbot está encapsulado en un IIFE para evitar conflictos:

```javascript
(function() {
    const chatFabGlobal = document.getElementById('chat-fab-global');
    const CHAT_STORAGE_KEY = 'telito_chatbot_history';
    // ... todo el código aislado
})();
```

**🟢 VENTAJA:** No contamina el scope global, sin conflictos con otro JavaScript.

---

## 📊 SessionStorage

El chatbot usa `sessionStorage` con una clave específica:

```javascript
const CHAT_STORAGE_KEY = 'telito_chatbot_history';
```

**Funcionalidad:**
- Guarda historial de conversación durante la sesión
- Persiste al navegar entre páginas
- Se limpia automáticamente al cerrar el navegador o hacer logout

**🟢 VENTAJA:** No interfiere con otros datos de sessionStorage o localStorage.

---

## 🚨 Puntos Críticos de Conflicto

### 1. **Si la otra rama modificó `ChatbotService.java`:**

**PROBLEMA:** Puede haber conflictos en el método `getFallbackResponse()`.

**SOLUCIÓN:**
1. Revisar cambios de ambas ramas
2. **PRIORIZAR** mantener las respuestas mejoradas de esta rama (con HTML, enlaces, ejemplos)
3. Si la otra rama agregó nuevas respuestas, integrarlas SIN eliminar las existentes
4. Mantener la estructura de if/matches para detección de patrones

### 2. **Si la otra rama creó un `footer.html` diferente:**

**PROBLEMA:** Conflicto total de archivos.

**SOLUCIÓN:**
1. **Comparar ambos footers**
2. Si el de la otra rama tiene contenido de footer importante (legal, links, etc.), integrar ese contenido en el `<footer>` de esta rama
3. **NO ELIMINAR** el chatbot de esta rama
4. El archivo final debe contener: `<footer>` (de la otra rama si aplica) + `<div id="chat-fab-global">` (de esta rama)

### 3. **Si la otra rama modificó las vistas de desarrollador:**

**PROBLEMA:** Puede faltar la inclusión del footer.

**SOLUCIÓN:**
1. Agregar manualmente en cada vista nueva/modificada:
   ```html
   <div th:replace="~{desarrollador/footer :: footer}"></div>
   ```
2. Colocar justo antes de `</body>`
3. Verificar que no se duplique si ya existe

---

## ✅ Checklist de Merge

Antes de completar el merge, verificar:

### Archivos
- [ ] `footer.html` existe y está completo (HTML + CSS + JS del chatbot)
- [ ] `ChatbotService.java` tiene el método `getFallbackResponse()` mejorado
- [ ] Todas las vistas de desarrollador incluyen: `<div th:replace="~{desarrollador/footer :: footer}"></div>`

### Funcionalidad
- [ ] El botón flotante aparece en la esquina inferior derecha
- [ ] El chatbot se abre/cierra correctamente
- [ ] Los mensajes se envían y reciben respuestas
- [ ] El historial persiste al navegar entre páginas
- [ ] Los enlaces en las respuestas funcionan correctamente
- [ ] El historial se limpia al hacer logout

### Rutas correctas en respuestas
- [ ] `/apis` - Catálogo de APIs
- [ ] `/dev/sandbox` - Sandbox
- [ ] `/dev/onboarding` - Onboarding
- [ ] `/dev/soporte/nuevo-ticket` - Crear ticket
- [ ] `/dev/perfil` - Perfil de usuario

### Consola del navegador
- [ ] No hay errores de JavaScript
- [ ] Aparecen logs de debug: `🔍 Cargando historial...`, `💾 Historial guardado...`
- [ ] SessionStorage contiene la key `telito_chatbot_history`

---

## 🧪 Testing Post-Merge

Después del merge, probar:

1. **Navegación con historial:**
   - Abrir chatbot en `/dev/home`
   - Escribir 3-4 mensajes
   - Navegar a `/apis`
   - Abrir chatbot y verificar que los mensajes persisten

2. **Respuestas inteligentes:**
   - "¿Cómo te llamas?" → Debe responder "Telito Bot"
   - "muéstrame ejemplos de código" → Debe mostrar snippets en JS/Python/cURL
   - "documentación de APIs" → Debe enviar enlace clickeable a `/apis`

3. **Limpieza de historial:**
   - Hacer logout
   - Volver a iniciar sesión
   - Abrir chatbot → Debe mostrar mensajes de bienvenida (historial limpio)

4. **Todas las vistas:**
   - Visitar cada vista del portal de desarrolladores
   - Verificar que el botón flotante aparece en todas

---

## 🆘 Troubleshooting

### Problema: "El chatbot no aparece en ninguna vista"
**Causa:** Falta incluir el footer en las vistas.
**Solución:** Agregar `<div th:replace="~{desarrollador/footer :: footer}"></div>` antes de `</body>` en cada vista.

### Problema: "El historial no persiste al navegar"
**Causa:** SessionStorage no se está guardando/cargando.
**Solución:** 
- Verificar en consola los logs `💾 Historial guardado`
- Verificar que la key `telito_chatbot_history` existe en sessionStorage (F12 → Application → Session Storage)
- Asegurar que el JavaScript se está ejecutando en cada página

### Problema: "Los enlaces del chatbot dan error 404"
**Causa:** Rutas incorrectas en `ChatbotService.java`.
**Solución:** Verificar que las rutas sean:
- `/apis` (NO `/dev/apis`)
- `/dev/sandbox`
- `/dev/onboarding`
- `/dev/soporte/nuevo-ticket`

### Problema: "El chatbot se ve mal o sin estilos"
**Causa:** Faltan los estilos CSS del `footer.html`.
**Solución:** Verificar que todo el `<style>` del footer.html esté presente (líneas ~10-230).

### Problema: "Múltiples chatbots aparecen"
**Causa:** Código de chatbot duplicado en vistas individuales.
**Solución:** Eliminar código inline de chatbot en vistas, mantener solo la inclusión del footer.

---

## 📞 Contacto

Si tienes dudas durante el merge, contactar a:
- **Desarrollador:** Pedro (rama `pedroRama`)
- **Feature:** Chatbot flotante con historial persistente

---

## 📌 Notas Finales

- El chatbot es **completamente funcional offline** (no requiere AWS ni servicios externos)
- Todo el procesamiento es **local en Spring Boot**
- Usa **expresiones regulares** para detectar intenciones del usuario
- Es **extensible**: fácil agregar nuevas respuestas en `ChatbotService.java`
- **Ligero**: No afecta el rendimiento de la aplicación

**Fecha de última actualización:** Noviembre 19, 2025
**Rama:** `pedroRama`
**Versión:** 1.0
