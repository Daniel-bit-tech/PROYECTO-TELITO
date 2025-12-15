# 🤖 Mejoras del Chatbot - Funcionalidades Inteligentes

## ✅ Nuevos Endpoints Implementados

### 1. **Información del Usuario** 
`GET /dev/soporte/chatbot/info-usuario`

**Respuesta:**
```json
{
  "success": true,
  "usuario": {
    "nombre": "Juan Pérez",
    "rol": "Desarrollador",
    "organizacion": {
      "nombre": "Tech Solutions",
      "descripcion": "Empresa de desarrollo de software"
    },
    "equipo": {
      "nombre": "Equipo Alpha"
    }
  }
}
```

**Casos de uso en el chatbot:**
- "¿Quién soy?"
- "¿A qué organización pertenezco?"
- "¿Cuál es mi equipo?"
- "Mi información"

---

### 2. **Mis Tickets**
`GET /dev/soporte/chatbot/mis-tickets`

**Respuesta:**
```json
{
  "success": true,
  "tickets": [
    {
      "id": 15,
      "asunto": "Error en API de pagos",
      "estado": "Activo",
      "fechaCreacion": "2025-12-12 10:30:00",
      "api": "API Pagos"
    }
  ],
  "total": 5
}
```

**Casos de uso en el chatbot:**
- "¿Cuántos tickets tengo?"
- "Muéstrame mis tickets"
- "Estado de mis tickets"
- "Tickets pendientes"
- "Mis solicitudes"

---

### 3. **Información de la Organización**
`GET /dev/soporte/chatbot/mi-organizacion`

**Respuesta:**
```json
{
  "success": true,
  "organizacion": {
    "nombre": "Tech Solutions",
    "descripcion": "Empresa de desarrollo de software",
    "totalMiembros": 8,
    "productOwner": {
      "nombre": "María García",
      "correo": "maria@tech.com"
    },
    "miembros": [
      {
        "nombre": "Juan Pérez",
        "correo": "juan@tech.com",
        "rol": "Desarrollador"
      }
    ]
  }
}
```

**Casos de uso en el chatbot:**
- "¿Quién es mi Product Owner?"
- "¿Quiénes son mis compañeros?"
- "Miembros de mi equipo"
- "¿A quién reporto?"
- "Contacto del PO"
- "Lista de desarrolladores"

---

### 4. **APIs Disponibles** (Ya existente)
`GET /dev/soporte/chatbot/apis`

**Casos de uso en el chatbot:**
- "¿Qué APIs hay?"
- "Lista de APIs"
- "APIs disponibles"

---

## 🎯 Comandos Inteligentes Sugeridos

### Comandos Personales
```
"quién soy" → Muestra info del usuario
"mi información" → Muestra info del usuario
"mi perfil" → Muestra info del usuario
```

### Comandos de Tickets
```
"mis tickets" → Lista todos los tickets del usuario
"tickets activos" → Filtra solo tickets activos
"cuántos tickets tengo" → Muestra el total
"último ticket" → Muestra el más reciente
```

### Comandos de Organización
```
"mi organización" → Info de la organización
"quién es mi po" → Muestra info del Product Owner
"mi product owner" → Muestra info del Product Owner
"miembros de mi equipo" → Lista todos los miembros
"desarrolladores" → Lista desarrolladores de la org
"contactos" → Lista miembros con correos
```

### Comandos de APIs
```
"qué apis hay" → Lista APIs disponibles
"apis disponibles" → Lista APIs disponibles
"lista de apis" → Lista APIs disponibles
```

### Comandos de Ayuda
```
"ayuda" → Muestra todos los comandos disponibles
"qué puedes hacer" → Muestra capacidades
"comandos" → Lista de comandos
```

---

## 💡 Implementación en el Footer.html

### Ejemplo de implementación en el chatbot:

```javascript
async function sendMessageGlobal() {
    const message = chatInputGlobal.value.trim().toLowerCase();
    
    // Comandos de información personal
    if (message.includes('quién soy') || message.includes('mi información') || message.includes('mi perfil')) {
        await obtenerInfoUsuario();
        return;
    }
    
    // Comandos de tickets
    if (message.includes('mis tickets') || message.includes('tickets') || message.includes('cuántos tickets')) {
        await obtenerMisTickets();
        return;
    }
    
    // Comandos de organización
    if (message.includes('mi organización') || message.includes('mi equipo')) {
        await obtenerInfoOrganizacion();
        return;
    }
    
    if (message.includes('product owner') || message.includes('po') || message.includes('quién es mi po')) {
        await obtenerProductOwner();
        return;
    }
    
    // Comandos de APIs
    if (message.includes('apis') || message.includes('lista de apis')) {
        await obtenerAPIs();
        return;
    }
    
    // Si no es un comando específico, enviar al chatbot de AWS
    // ... código existente ...
}

// Función para obtener información del usuario
async function obtenerInfoUsuario() {
    try {
        const response = await fetch('/dev/soporte/chatbot/info-usuario');
        const data = await response.json();
        
        if (data.success) {
            const user = data.usuario;
            let mensaje = `👤 <strong>Tu Información:</strong><br><br>`;
            mensaje += `<strong>Nombre:</strong> ${user.nombre}<br>`;
            mensaje += `<strong>Rol:</strong> ${user.rol}<br>`;
            
            if (user.organizacion) {
                mensaje += `<strong>Organización:</strong> ${user.organizacion.nombre}<br>`;
            }
            
            if (user.equipo) {
                mensaje += `<strong>Equipo:</strong> ${user.equipo.nombre}<br>`;
            }
            
            addMessageToDOM(mensaje, 'bot');
        }
    } catch (error) {
        console.error('Error:', error);
        addMessageToDOM('❌ Error al obtener tu información', 'bot');
    }
}

// Función para obtener tickets del usuario
async function obtenerMisTickets() {
    try {
        const response = await fetch('/dev/soporte/chatbot/mis-tickets');
        const data = await response.json();
        
        if (data.success) {
            if (data.total === 0) {
                addMessageToDOM('📋 No tienes tickets creados actualmente.', 'bot');
                return;
            }
            
            let mensaje = `📋 <strong>Tus Tickets (${data.total}):</strong><br><br>`;
            
            data.tickets.slice(0, 5).forEach(ticket => {
                mensaje += `• <strong>${ticket.asunto}</strong><br>`;
                mensaje += `  Estado: ${ticket.estado} | API: ${ticket.api}<br>`;
                mensaje += `  Fecha: ${ticket.fechaCreacion}<br><br>`;
            });
            
            if (data.total > 5) {
                mensaje += `<em>Mostrando los 5 más recientes...</em>`;
            }
            
            addMessageToDOM(mensaje, 'bot');
        }
    } catch (error) {
        console.error('Error:', error);
        addMessageToDOM('❌ Error al obtener tus tickets', 'bot');
    }
}

// Función para obtener información de la organización
async function obtenerInfoOrganizacion() {
    try {
        const response = await fetch('/dev/soporte/chatbot/mi-organizacion');
        const data = await response.json();
        
        if (data.success) {
            const org = data.organizacion;
            let mensaje = `🏢 <strong>${org.nombre}</strong><br><br>`;
            mensaje += `${org.descripcion}<br><br>`;
            mensaje += `<strong>Miembros:</strong> ${org.totalMiembros}<br><br>`;
            
            if (org.productOwner) {
                mensaje += `👨‍💼 <strong>Product Owner:</strong><br>`;
                mensaje += `${org.productOwner.nombre}<br>`;
                mensaje += `📧 ${org.productOwner.correo}<br><br>`;
            }
            
            mensaje += `<strong>Equipo:</strong><br>`;
            org.miembros.slice(0, 5).forEach(miembro => {
                mensaje += `• ${miembro.nombre} - ${miembro.rol}<br>`;
            });
            
            if (org.totalMiembros > 5) {
                mensaje += `<em>... y ${org.totalMiembros - 5} miembros más</em>`;
            }
            
            addMessageToDOM(mensaje, 'bot');
        }
    } catch (error) {
        console.error('Error:', error);
        addMessageToDOM('❌ Error al obtener información de la organización', 'bot');
    }
}

// Función para obtener solo el Product Owner
async function obtenerProductOwner() {
    try {
        const response = await fetch('/dev/soporte/chatbot/mi-organizacion');
        const data = await response.json();
        
        if (data.success && data.organizacion.productOwner) {
            const po = data.organizacion.productOwner;
            let mensaje = `👨‍💼 <strong>Product Owner de ${data.organizacion.nombre}:</strong><br><br>`;
            mensaje += `<strong>Nombre:</strong> ${po.nombre}<br>`;
            mensaje += `<strong>Correo:</strong> ${po.correo}<br><br>`;
            mensaje += `Puedes contactarlo directamente para consultas sobre el proyecto.`;
            
            addMessageToDOM(mensaje, 'bot');
        } else {
            addMessageToDOM('No se encontró un Product Owner asignado a tu organización.', 'bot');
        }
    } catch (error) {
        console.error('Error:', error);
        addMessageToDOM('❌ Error al obtener información del Product Owner', 'bot');
    }
}
```

---

## 🚀 Beneficios de las Mejoras

1. **Contextualización**: El chatbot conoce al usuario y su contexto
2. **Autoservicio**: Los usuarios pueden consultar su información sin navegar
3. **Eficiencia**: Respuestas instantáneas a preguntas comunes
4. **Integración**: Datos en tiempo real desde la base de datos
5. **Experiencia Mejorada**: Interacciones más naturales y útiles

---

## 📝 Próximos Pasos

1. Implementar las funciones JavaScript en `footer.html`
2. Agregar reconocimiento de lenguaje natural mejorado
3. Considerar integrar respuestas del chatbot AWS con datos locales
4. Agregar cache para mejorar rendimiento
5. Implementar analytics para ver qué preguntan más los usuarios

---

## 🎨 Mensaje de Ayuda Actualizado

```
¡Hola! Soy el asistente virtual de Telito. ¿En qué puedo ayudarte?

Puedo ayudarte a:
• Responder preguntas sobre nuestras APIs
• Crear un ticket de soporte
• Mostrarte tu información personal
• Ver el estado de tus tickets
• Conocer a tu equipo y Product Owner
• Listar las APIs disponibles

Comandos útiles:
📌 "crear ticket" - Iniciar un nuevo ticket
📌 "mis tickets" - Ver tus tickets
📌 "mi organización" - Info de tu equipo
📌 "quién es mi po" - Contacto del PO
📌 "ayuda" - Ver todos los comandos
```
