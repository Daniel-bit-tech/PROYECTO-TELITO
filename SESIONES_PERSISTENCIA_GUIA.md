# 🔐 GUÍA DE PERSISTENCIA DE SESIONES - TELITO DEV

## ✅ Estado Actual de la Configuración

Tu aplicación **YA ESTÁ CONFIGURADA** para persistir sesiones en la base de datos MySQL. Las sesiones se mantienen incluso si reinicias el servidor.

---

## 📋 Configuración Implementada

### 1. **Dependencias Maven** ✅
```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-jdbc</artifactId>
</dependency>
```

### 2. **Application.properties** ✅
```properties
# Persistencia de sesiones en MySQL
spring.session.store-type=jdbc
spring.session.jdbc.initialize-schema=always
spring.session.timeout=604800s  # 7 días

# Cookie de sesión configurada para 7 días
server.servlet.session.cookie.max-age=604800

# Remember-me habilitado
# Token válido por 7 días (604,800 segundos)
```

### 3. **SecurityConfig.java** ✅
```java
.rememberMe(remember -> remember
    .key("remember-me-telito-key-2025")
    .rememberMeParameter("remember-me")
    .tokenValiditySeconds(604800)  // 7 días
    .userDetailsService(usuarioDetailService))
```

---

## 🔍 Cómo Funciona

### **Persistencia en Base de Datos**
1. **Tablas Spring Session**: Se crean automáticamente en `db_telito`:
   - `SPRING_SESSION` - Almacena información de sesiones
   - `SPRING_SESSION_ATTRIBUTES` - Almacena atributos de sesión

2. **Al hacer login**:
   - Se crea un registro en `SPRING_SESSION`
   - La cookie `JSESSIONID` se guarda en el navegador
   - Si marcas "Recordarme", se crea un token adicional

3. **Al reiniciar el servidor**:
   - Las sesiones permanecen en la base de datos
   - El usuario NO necesita volver a hacer login
   - La aplicación recupera la sesión desde MySQL

4. **Limpieza automática**:
   - Se ejecuta cada hora: `spring.session.jdbc.cleanup-cron=0 0 * * * *`
   - Elimina sesiones expiradas (más de 7 días)

---

## 🧪 Pruebas para Verificar

### **Test 1: Sesión Normal (Sin Remember-Me)**
```bash
1. Inicia sesión sin marcar "Recordarme"
2. Navega por la aplicación
3. Detén el servidor (Ctrl+C o stop en IDE)
4. Vuelve a iniciar el servidor
5. Recarga la página en el navegador
6. ✅ Deberías seguir con la sesión activa (sin login)
```

### **Test 2: Sesión con Remember-Me**
```bash
1. Inicia sesión y MARCA "Recordarme"
2. Navega por la aplicación
3. CIERRA EL NAVEGADOR completamente
4. Vuelve a abrir el navegador
5. Ve a http://localhost:8080
6. ✅ Deberías entrar directamente sin login
```

### **Test 3: Verificar Sesiones en BD**
Ejecuta el script SQL incluido:
```sql
-- Ver sesiones activas
SELECT 
    SESSION_ID,
    PRINCIPAL_NAME,
    FROM_UNIXTIME(CREATION_TIME/1000) AS created_at,
    FROM_UNIXTIME(LAST_ACCESS_TIME/1000) AS last_access,
    FROM_UNIXTIME(EXPIRY_TIME/1000) AS expires_at
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000;
```

---

## 🐛 Troubleshooting

### **Problema: Las sesiones no persisten**

#### Verificación 1: Tablas existen
```sql
USE db_telito;
SHOW TABLES LIKE 'SPRING_SESSION%';
```
**Solución**: Si no existen, ejecuta `verify_spring_session_tables.sql`

#### Verificación 2: Cookie del navegador
1. Abre DevTools (F12)
2. Ve a Application → Cookies
3. Busca `JSESSIONID` y `remember-me`
4. Verifica que `Max-Age` sea `604800` (7 días)

**Solución**: Si `Max-Age` es `Session`, revisar `application.properties`

#### Verificación 3: Logs del servidor
```bash
# Busca en los logs:
"Using 'JDBC' store for session"
"Creating new JDBC session"
```

**Solución**: Si no aparece, verifica que `spring.session.store-type=jdbc` esté configurado

#### Verificación 4: Limpieza de cookies
Si persisten problemas, limpia las cookies:
```bash
1. DevTools (F12) → Application → Cookies
2. Clic derecho en tu dominio → Clear
3. Cierra el navegador completamente
4. Vuelve a hacer login con "Recordarme" marcado
```

---

## ⚙️ Configuración Avanzada

### **Cambiar duración de sesiones**
Edita en `application.properties`:
```properties
# Cambiar de 7 días a 30 días (2,592,000 segundos)
spring.session.timeout=2592000s
server.servlet.session.cookie.max-age=2592000
# En SecurityConfig.java:
.tokenValiditySeconds(2592000)
```

### **Ver sesiones activas en tiempo real**
Endpoint de debug disponible en:
```
GET http://localhost:8080/api/session-debug/info
```

### **Limpiar sesiones manualmente**
```sql
-- Eliminar sesiones expiradas
DELETE FROM SPRING_SESSION 
WHERE EXPIRY_TIME < UNIX_TIMESTAMP() * 1000;

-- Eliminar TODAS las sesiones (logout forzado)
DELETE FROM SPRING_SESSION_ATTRIBUTES;
DELETE FROM SPRING_SESSION;
```

---

## 🔒 Seguridad

### **Características Habilitadas**:
- ✅ **Session Fixation Protection**: Cambia el ID de sesión después del login
- ✅ **HttpOnly Cookies**: Las cookies no son accesibles desde JavaScript
- ✅ **Max Sessions**: Máximo 5 sesiones concurrentes por usuario
- ✅ **Remember-Me Token**: Token seguro con hash BCrypt
- ✅ **Auto-cleanup**: Limpieza automática cada hora

### **Recomendaciones de Producción**:
1. Cambiar `server.servlet.session.cookie.secure=true` (requiere HTTPS)
2. Cambiar la clave remember-me en `SecurityConfig.java`
3. Habilitar Redis para mejor rendimiento (opcional):
   ```properties
   spring.session.store-type=redis
   spring.redis.host=tu-redis-server
   ```

---

## 📊 Monitoreo

### **Queries útiles**:

```sql
-- Contar sesiones activas
SELECT COUNT(*) AS sesiones_activas
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000;

-- Ver usuarios con sesión activa
SELECT DISTINCT PRINCIPAL_NAME, COUNT(*) as num_sesiones
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000
GROUP BY PRINCIPAL_NAME;

-- Sesiones por usuario
SELECT 
    PRINCIPAL_NAME,
    COUNT(*) as total_sesiones,
    MAX(FROM_UNIXTIME(LAST_ACCESS_TIME/1000)) as ultima_actividad
FROM SPRING_SESSION
GROUP BY PRINCIPAL_NAME;
```

---

## ✨ Resultado Esperado

Con esta configuración:

1. ✅ **Sin reiniciar servidor**: Usuario mantiene sesión indefinidamente (hasta 7 días sin actividad)
2. ✅ **Con reinicio de servidor**: Usuario mantiene sesión, NO necesita hacer login de nuevo
3. ✅ **Con cierre de navegador**: Si marcó "Recordarme", entra automáticamente
4. ✅ **Sin cierre de navegador**: Sesión persiste incluso sin "Recordarme"
5. ✅ **Expiración**: Después de 7 días sin actividad, se requiere login de nuevo

---

## 🎯 Próximos Pasos

1. **Ejecutar el script SQL** para verificar las tablas:
   ```bash
   # En MySQL Workbench o tu cliente SQL
   source verify_spring_session_tables.sql
   ```

2. **Probar la persistencia**:
   - Login → Reiniciar servidor → Verificar sesión activa

3. **Verificar con DevTools**:
   - F12 → Application → Cookies → Ver `JSESSIONID` y `remember-me`

4. **Consultar sesiones activas**:
   - Ejecutar queries de monitoreo en la base de datos

---

## 📞 Soporte

Si después de seguir esta guía las sesiones no persisten, verifica:
1. ✅ Tablas `SPRING_SESSION` y `SPRING_SESSION_ATTRIBUTES` existen
2. ✅ La dependencia `spring-session-jdbc` está en el pom.xml
3. ✅ La cookie `JSESSIONID` tiene `Max-Age=604800`
4. ✅ Los logs muestran "Using 'JDBC' store for session"

**Logs adicionales** para debugging:
```properties
logging.level.org.springframework.session=DEBUG
logging.level.org.springframework.security=DEBUG
```
