# 🇵🇪 Integración API RENIEC - Autocomplete DNI

## 📋 Descripción

Sistema de autocompletado de datos personales mediante consulta al **RENIEC** (Registro Nacional de Identificación y Estado Civil) de Perú usando la API autorizada **apiperu.dev**.

Cuando un usuario ingresa su DNI de 8 dígitos, el sistema automáticamente consulta y completa:
- ✅ Nombres
- ✅ Apellido Paterno  
- ✅ Apellido Materno

---

## 🏗️ Arquitectura

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐      ┌──────────┐
│  Frontend   │─────▶│   Backend    │─────▶│ ReniecAPI   │─────▶│  RENIEC  │
│  (HTML/JS)  │      │ Spring Boot  │      │ apiperu.dev │      │ (Oficial)│
└─────────────┘      └──────────────┘      └─────────────┘      └──────────┘
```

### Flujo de datos:
1. **Usuario** escribe DNI de 8 dígitos
2. **Frontend** detecta DNI completo y llama a `/api/reniec/dni/{dni}`
3. **Backend** valida el DNI y consulta a `apiperu.dev` con API Key
4. **API Externa** consulta RENIEC oficial
5. **Backend** recibe nombres y apellidos
6. **Frontend** autocompleta los campos del formulario

---

## 📁 Archivos Creados

### Backend (Spring Boot)

#### 1. `ReniecResponseDto.java`
```
src/main/java/com/example/telitodev/dto/ReniecResponseDto.java
```
**Propósito:** DTO para mapear la respuesta de la API de RENIEC

**Campos principales:**
- `success` (boolean): Indica si la consulta fue exitosa
- `data` (ReniecData): Datos de la persona
  - `dni`: DNI consultado
  - `nombres`: Nombres completos
  - `apellidoPaterno`: Apellido paterno
  - `apellidoMaterno`: Apellido materno
  - `nombreCompleto`: Nombre completo concatenado
- `message` (String): Mensaje de respuesta

#### 2. `ReniecService.java`
```
src/main/java/com/example/telitodev/service/ReniecService.java
```
**Propósito:** Servicio que maneja la lógica de consulta a RENIEC

**Métodos principales:**
- `consultarDNI(String dni)`: Consulta un DNI en RENIEC
- `isServiceAvailable()`: Verifica si la API está disponible
- `esValidoDNI(String dni)`: Valida formato de DNI (8 dígitos)

**Características:**
- ✅ Validación de formato DNI
- ✅ Manejo de errores HTTP (404, 401, 500)
- ✅ Logging detallado
- ✅ Timeout configurable
- ✅ Bearer token authentication

#### 3. `ReniecController.java`
```
src/main/java/com/example/telitodev/controller/api/ReniecController.java
```
**Propósito:** Controlador REST para endpoints de RENIEC

**Endpoints:**

**GET `/api/reniec/dni/{dni}`**
- Consulta un DNI específico
- Retorna datos de la persona o error
- Validaciones: longitud, solo números

**GET `/api/reniec/health`**
- Health check del servicio
- Verifica disponibilidad de la API externa

**Respuesta exitosa:**
```json
{
  "success": true,
  "data": {
    "dni": "12345678",
    "nombres": "JUAN CARLOS",
    "apellidoPaterno": "PEREZ",
    "apellidoMaterno": "GARCIA",
    "nombreCompleto": "PEREZ GARCIA JUAN CARLOS"
  },
  "message": "Consulta exitosa"
}
```

**Respuesta de error:**
```json
{
  "success": false,
  "message": "DNI no encontrado en RENIEC"
}
```

### Frontend (JavaScript)

#### 4. `reniec-autocomplete.js`
```
src/main/resources/static/js/reniec-autocomplete.js
```
**Propósito:** Script reutilizable para autocompletar campos del formulario

**Características:**
- ✅ Detecta DNI completo (8 dígitos) automáticamente
- ✅ Consulta automática al perder foco (blur) o presionar Enter
- ✅ Indicador de carga visual
- ✅ Notificaciones toast
- ✅ Validación de solo números
- ✅ Marca campos autocompletados con color verde
- ✅ Manejo de errores elegante
- ✅ Compatible con CSRF tokens

**Función principal:**
```javascript
initReniecAutocomplete({
    dniInputId: 'dni',
    nombresInputId: 'nombre',
    apellidoPaternoInputId: 'apellidoPaterno',
    apellidoMaternoInputId: 'apellidoMaterno',
    submitButtonId: 'submitBtn', // Opcional
    addSearchButton: false // Opcional
});
```

### Configuración

#### 5. `application.properties` (modificado)
```properties
# API RENIEC
reniec.api.url=https://dniruc.apiperu.dev/api/v1/dni
reniec.api.key=${RENIEC_API_KEY}
reniec.api.timeout=5000
```

---

## 🚀 Instalación y Configuración

### 1. Obtener API Key

1. Ve a **https://apiperu.dev/**
2. Regístrate y obtén tu API Key
3. Guarda tu API Key de forma segura

### 2. Configurar Variables de Entorno

**En desarrollo (local):**

**Windows (PowerShell):**
```powershell
$env:RENIEC_API_KEY="tu-api-key-aqui"
```

**Windows (CMD):**
```cmd
set RENIEC_API_KEY=tu-api-key-aqui
```

**Linux/Mac:**
```bash
export RENIEC_API_KEY="tu-api-key-aqui"
```

**En producción:**
Configurar la variable de entorno en tu servidor/contenedor.

### 3. Verificar Instalación

Ejecutar el health check:
```bash
curl http://localhost:8080/api/reniec/health
```

Respuesta esperada:
```json
{
  "available": true,
  "message": "Servicio de RENIEC disponible"
}
```

---

## 💻 Uso en Formularios

### Ejemplo 1: Formulario de Registro

```html
<!DOCTYPE html>
<html>
<head>
    <title>Registro</title>
    <!-- Incluir el script -->
    <script src="/js/reniec-autocomplete.js"></script>
</head>
<body>
    <form id="registroForm">
        <!-- Campo DNI -->
        <div>
            <label>DNI:</label>
            <input type="text" 
                   id="dni" 
                   name="dni" 
                   maxlength="8" 
                   placeholder="Ingresa tu DNI"
                   required>
        </div>

        <!-- Campos que se autocompletarán -->
        <div>
            <label>Nombres:</label>
            <input type="text" id="nombre" name="nombre" required>
        </div>

        <div>
            <label>Apellido Paterno:</label>
            <input type="text" id="apellidoPaterno" name="apellidoPaterno" required>
        </div>

        <div>
            <label>Apellido Materno:</label>
            <input type="text" id="apellidoMaterno" name="apellidoMaterno" required>
        </div>

        <button type="submit" id="submitBtn">Registrar</button>
    </form>

    <!-- Inicializar el autocompletado -->
    <script>
        // Inicializar cuando el DOM esté listo
        document.addEventListener('DOMContentLoaded', function() {
            initReniecAutocomplete({
                dniInputId: 'dni',
                nombresInputId: 'nombre',
                apellidoPaternoInputId: 'apellidoPaterno',
                apellidoMaternoInputId: 'apellidoMaterno',
                submitButtonId: 'submitBtn'
            });
        });
    </script>
</body>
</html>
```

### Ejemplo 2: Formulario con Botón de Búsqueda Manual

```html
<script>
    initReniecAutocomplete({
        dniInputId: 'dni',
        nombresInputId: 'nombre',
        apellidoPaternoInputId: 'apellidoPaterno',
        apellidoMaternoInputId: 'apellidoMaterno',
        addSearchButton: true  // Agrega botón de búsqueda manual
    });
</script>
```

### Ejemplo 3: Thymeleaf (Spring Boot)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Crear Usuario</title>
    <!-- CSRF Token para Spring Security -->
    <meta name="_csrf" th:content="${_csrf.token}"/>
    <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
    
    <!-- Script RENIEC -->
    <script th:src="@{/js/reniec-autocomplete.js}"></script>
</head>
<body>
    <form th:action="@{/admin/usuarios/crear}" method="post">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        
        <div>
            <label>DNI:</label>
            <input type="text" id="newUserDni" name="dni" maxlength="8" required>
        </div>

        <div>
            <label>Nombres:</label>
            <input type="text" id="newUserNombre" name="nombre" required>
        </div>

        <div>
            <label>Apellido Paterno:</label>
            <input type="text" id="newUserApellidoPaterno" name="apellidoPaterno" required>
        </div>

        <div>
            <label>Apellido Materno:</label>
            <input type="text" id="newUserApellidoMaterno" name="apellidoMaterno" required>
        </div>

        <button type="submit">Crear Usuario</button>
    </form>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            initReniecAutocomplete({
                dniInputId: 'newUserDni',
                nombresInputId: 'newUserNombre',
                apellidoPaternoInputId: 'newUserApellidoPaterno',
                apellidoMaternoInputId: 'newUserApellidoMaterno'
            });
        });
    </script>
</body>
</html>
```

---

## 🎨 Estilos Visuales

El script incluye estilos automáticos:

### Campos Autocompletados
Los campos completados automáticamente se marcan con:
- ✅ Fondo verde claro (`#e8f5e9`)
- ✅ Borde verde (`#28a745`)
- ✅ Atributo `data-reniec-verified="true"`

### Indicador de Carga
Aparece automáticamente durante la consulta:
- 🔄 Spinner animado
- 💬 Texto "Consultando..."

### Notificaciones Toast
- ✅ **Verde**: Consulta exitosa
- ❌ **Rojo**: Error o DNI no encontrado
- ⚠️ **Amarillo**: Advertencia
- ℹ️ **Azul**: Información

---

## 🔒 Seguridad

### 1. Validaciones Backend
- ✅ DNI debe tener exactamente 8 dígitos
- ✅ Solo caracteres numéricos
- ✅ Sanitización de entrada

### 2. API Key Protection
- ✅ API Key en variables de entorno (no en código)
- ✅ Bearer token authentication
- ✅ No expuesta al frontend

### 3. CORS
- ✅ Configurado en el controlador
- ✅ Restricción de orígenes en producción

### 4. Rate Limiting
- ⚠️ **Importante**: apiperu.dev tiene límites de requests
- 💡 **Recomendación**: Implementar cache de consultas frecuentes

---

## 🧪 Testing

### Test Manual

**1. Consultar DNI válido:**
```bash
curl -X GET "http://localhost:8080/api/reniec/dni/12345678" \
     -H "Content-Type: application/json"
```

**2. Consultar DNI inválido:**
```bash
curl -X GET "http://localhost:8080/api/reniec/dni/123" \
     -H "Content-Type: application/json"
```

**3. Health check:**
```bash
curl -X GET "http://localhost:8080/api/reniec/health"
```

### Test desde Frontend

1. Abrir formulario con autocompletado
2. Ingresar DNI: `12345678`
3. Hacer click fuera del campo o presionar Enter
4. Verificar que se autocompleten nombres y apellidos

---

## 📊 Manejo de Errores

### Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `API Key inválida o expirada` | API Key incorrecta | Verificar variable de entorno `RENIEC_API_KEY` |
| `DNI no encontrado en RENIEC` | DNI no existe | Verificar que el DNI sea real |
| `El servicio de RENIEC no está disponible` | API externa caída | Reintentar más tarde |
| `DNI inválido. Debe tener 8 dígitos` | Formato incorrecto | Ingresar exactamente 8 números |

### Logs

El sistema genera logs detallados:

```
🔍 Consultando DNI en RENIEC: 12345678
📡 Llamando a API: https://dniruc.apiperu.dev/api/v1/dni/12345678
✅ Respuesta recibida: {...}
👤 Datos mapeados: JUAN CARLOS PEREZ GARCIA
```

---

## 🚀 Despliegue en Producción

### 1. Variables de Entorno

Asegurarse de configurar en el servidor:
```bash
RENIEC_API_KEY=tu-api-key-produccion
```

### 2. HTTPS

**Importante:** En producción, usar HTTPS:
```properties
reniec.api.url=https://dniruc.apiperu.dev/api/v1/dni
```

### 3. Cache (Opcional)

Para reducir llamadas a la API:
```java
@Cacheable(value = "reniecCache", key = "#dni")
public ReniecResponseDto consultarDNI(String dni) {
    // ... código existente
}
```

### 4. Monitoreo

Implementar métricas:
- Total de consultas
- Consultas exitosas vs fallidas
- Tiempo promedio de respuesta
- Errores por tipo

---

## 📞 Soporte

### Proveedor de API
- **Sitio:** https://apiperu.dev/
- **Documentación:** https://apiperu.dev/docs
- **Soporte:** contacto@apiperu.dev

### Desarrolladores
- **Proyecto:** PROYECTO-TELITO
- **Rama:** Merge
- **Fecha:** Diciembre 2025

---

## 📝 Changelog

### v1.0.0 - Diciembre 2025
- ✅ Implementación inicial
- ✅ Integración con apiperu.dev
- ✅ Autocompletado automático
- ✅ Indicadores visuales
- ✅ Manejo de errores
- ✅ Documentación completa

---

## 🎯 Próximas Mejoras

- [ ] Cache de consultas frecuentes (Redis)
- [ ] Historial de consultas (auditoría)
- [ ] Validación de foto (API adicional)
- [ ] Búsqueda por nombres (reverse lookup)
- [ ] Dashboard de estadísticas de uso
- [ ] Rate limiting inteligente
- [ ] Fallback a consulta manual

---

## ⚖️ Legal

- ✅ **apiperu.dev** es un servicio **autorizado** por RENIEC
- ✅ Cumple con normativa de protección de datos de Perú
- ✅ Solo consulta datos públicos del RENIEC
- ⚠️ **Prohibido** almacenar datos sin consentimiento
- ⚠️ **Prohibido** usar datos para fines distintos a validación

**Nota:** Revisar términos y condiciones de apiperu.dev y RENIEC.
