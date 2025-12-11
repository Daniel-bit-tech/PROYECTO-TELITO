# 🔧 SOLUCIÓN: Problema de Sesión No Persistente

## 📋 Cambios Realizados

### 1. Configuración de Sesión Extendida
Se ha aumentado el tiempo de vida de las sesiones de 2 horas a **7 días (604,800 segundos)**:

**SecurityConfig.java:**
- `rememberMe()` configurado con validez de 7 días
- `sessionFixation().changeSessionId()` para seguridad después del login

**application.properties:**
- `spring.session.timeout=604800s` (7 días)
- `server.servlet.session.timeout=10080m` (7 días)
- `server.servlet.session.cookie.max-age=604800` (7 días)
- Limpieza de sesiones cada hora en lugar de cada 5 minutos

### 2. Controlador de Diagnóstico
Se ha creado `SessionDebugController.java` con tres endpoints útiles:

#### **GET /api/session-debug/info**
Muestra información completa de la sesión actual:
- ID de sesión
- Cookies
- Atributos de sesión
- Información de autenticación
- Estado del SessionRegistry
- Sesiones en base de datos

#### **GET /api/session-debug/test-persistence**
Prueba si la sesión persiste entre requests:
- Incrementa un contador cada vez que se llama
- Si el contador aumenta, la sesión funciona correctamente

#### **GET /api/session-debug/verify-tables**
Verifica que las tablas de Spring Session existan:
- Cuenta sesiones en SPRING_SESSION
- Cuenta atributos en SPRING_SESSION_ATTRIBUTES
- Muestra sesiones activas

### 3. Script SQL de Verificación
Se ha creado `verificar_spring_session.sql` para:
- Crear tablas si no existen
- Verificar estructura
- Limpiar sesiones expiradas
- Mostrar sesiones activas

## 🚀 Pasos para Resolver el Problema

### Paso 1: Verificar Tablas de Base de Datos
```sql
-- Ejecutar en MySQL Workbench o similar
USE db_telito;
SOURCE verificar_spring_session.sql;
```

O manualmente:
```sql
USE db_telito;

-- Verificar si existen las tablas
SHOW TABLES LIKE 'SPRING_SESSION%';

-- Si no existen, crear con el script verificar_spring_session.sql
```

### Paso 2: Reiniciar la Aplicación
```powershell
# Detener la aplicación (Ctrl+C si está corriendo)
# Compilar y ejecutar
mvn clean package -DskipTests
java -jar target/telitodev-0.0.1-SNAPSHOT.jar
```

### Paso 3: Probar la Sesión

1. **Login con Remember Me:**
   - Ve a http://localhost:8080/login
   - Ingresa tus credenciales
   - **MARCA el checkbox "Recordar sesión" o "Mantener sesión iniciada"**
   - Inicia sesión

2. **Verificar que la sesión persista:**
   - Cierra el navegador completamente
   - Abre el navegador nuevamente
   - Ve a http://localhost:8080
   - Deberías estar aún conectado

3. **Usar endpoints de diagnóstico:**

   ```
   GET http://localhost:8080/api/session-debug/info
   ```
   Verifica:
   - `sessionId` está presente
   - `authenticated: true`
   - `cookies.JSESSIONID` existe
   - `dbSessionCount` > 0

   ```
   GET http://localhost:8080/api/session-debug/test-persistence
   ```
   Llama este endpoint varias veces, el `testCounter` debe aumentar.

   ```
   GET http://localhost:8080/api/session-debug/verify-tables
   ```
   Debe mostrar:
   - `springSessionExists: true`
   - `springSessionAttributesExists: true`
   - `status: "OK"`

## 🔍 Diagnóstico de Problemas Comunes

### Problema: Las tablas no existen
**Síntoma:** `/api/session-debug/verify-tables` retorna error.

**Solución:**
```sql
USE db_telito;
SOURCE verificar_spring_session.sql;
```

### Problema: El checkbox "Remember Me" no está visible
**Solución:** Verificar en `login.html` que exista:
```html
<input type="checkbox" name="remember-me" class="login__check-input" id="user-check">
```

### Problema: La sesión se pierde al cerrar el navegador
**Posibles causas:**

1. **No marcaste el checkbox "Remember Me"**
   - Solución: Marcar el checkbox al hacer login

2. **El navegador está configurado para borrar cookies al cerrarse**
   - Chrome: Settings → Privacy → Cookies → "Clear cookies and site data when you close all windows"
   - Firefox: Settings → Privacy → Cookies → "Delete cookies when Firefox is closed"
   - Solución: Desactivar esta opción

3. **Las tablas de Spring Session no existen**
   - Verificar con: `/api/session-debug/verify-tables`
   - Solución: Ejecutar `verificar_spring_session.sql`

4. **El servidor está reiniciándose**
   - Las sesiones en memoria se pierden al reiniciar
   - Con Spring Session JDBC, las sesiones persisten incluso después de reiniciar

### Problema: Error "Table 'db_telito.SPRING_SESSION' doesn't exist"
**Solución:**
```sql
USE db_telito;

CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES LONGBLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) 
        REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## ✅ Verificación Final

Una vez aplicados los cambios:

1. ✅ Las tablas SPRING_SESSION y SPRING_SESSION_ATTRIBUTES existen
2. ✅ La aplicación inicia sin errores
3. ✅ El checkbox "Remember Me" está visible en el login
4. ✅ Al hacer login y marcar "Remember Me", la sesión persiste al cerrar el navegador
5. ✅ `/api/session-debug/info` muestra información correcta
6. ✅ `/api/session-debug/test-persistence` incrementa el contador
7. ✅ `/api/session-debug/verify-tables` retorna status "OK"

## 📊 Configuración Actual

- **Duración de sesión:** 7 días (604,800 segundos)
- **Cookie max-age:** 7 días
- **Remember Me:** 7 días
- **Máximo sesiones concurrentes:** 5 por usuario
- **Limpieza automática:** Cada hora
- **Storage:** JDBC (MySQL tabla SPRING_SESSION)
- **Cookie name:** JSESSIONID
- **Cookie secure:** false (para desarrollo local)
- **Cookie httpOnly:** true (seguridad)
- **Cookie sameSite:** lax

## 🔒 Notas de Seguridad

- La configuración actual es para **desarrollo local**
- En **producción**, cambiar:
  - `server.servlet.session.cookie.secure=true` (requiere HTTPS)
  - `server.servlet.session.cookie.same-site=strict`
  - Considerar reducir el timeout a 24 horas o menos
  - Habilitar limpieza más frecuente de sesiones

## 📝 Logs Útiles

Para ver información de Spring Session en los logs:
```properties
# application.properties
logging.level.org.springframework.session=DEBUG
logging.level.org.springframework.security.web.session=DEBUG
```

Esto ayudará a diagnosticar problemas de sesión en tiempo real.
