# 🔐 Guía de Implementación OAuth2 - Telito Developer Portal

## 📋 Resumen de Implementación

Se ha implementado exitosamente un sistema de **autenticación dual** que permite:

- ✅ **Autenticación Interna**: Usuarios corporativos (DNI + Contraseña)
- ✅ **Autenticación Externa**: Usuarios externos via Google OAuth2
- ✅ **Onboarding Automático**: Creación automática de usuarios externos

---

## 🗂️ Archivos Modificados/Creados

### 📄 Scripts de Base de Datos
- `add_tipo_acceso_column.sql` - Modificaciones de esquema para OAuth2
- `oauth2_setup_verification.sql` - Verificación de configuración

### ⚙️ Configuración
- `pom.xml` - Dependencia OAuth2 Client
- `application.properties` - Configuración Google OAuth2

### 🏗️ Backend (Java)
- `Usuario.java` - Entity extendida con campos OAuth2
- `SecurityConfig.java` - Configuración de seguridad dual
- `OAuth2UserService.java` - Servicio de procesamiento OAuth2
- `UsuarioRepository.java` - Métodos de consulta OAuth2
- `LoginController.java` - Endpoints para manejo OAuth2

### 🎨 Frontend
- `login.html` - Vista actualizada con botón Google OAuth2

---

## 🚀 Pasos de Implementación

### 1. **Preparar Base de Datos**
```sql
-- Ejecutar en orden:
source add_tipo_acceso_column.sql;
source oauth2_setup_verification.sql;
```

### 2. **Configurar Google OAuth2**
1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear/seleccionar proyecto
3. Habilitar Google+ API
4. Crear credenciales OAuth2:
   - Tipo: Aplicación web
   - URIs autorizados: `http://localhost:8080`, `https://tu-dominio.com`
   - URIs de redirección: `http://localhost:8080/login/oauth2/code/google`

### 3. **Actualizar application.properties**
```properties
# Configuración OAuth2 Google
spring.security.oauth2.client.registration.google.client-id=TU_CLIENT_ID_AQUI
spring.security.oauth2.client.registration.google.client-secret=TU_CLIENT_SECRET_AQUI
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google
spring.security.oauth2.client.registration.google.client-name=Google

spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v2/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=email
```

### 4. **Reiniciar Aplicación**
```bash
./mvnw spring-boot:run
```

---

## 🔍 Flujo de Autenticación

### Autenticación Interna (Existente)
1. Usuario ingresa DNI/email y contraseña
2. `UsuarioDetailService` valida credenciales
3. Spring Security autentica
4. Redirección según rol

### Autenticación Externa (Nueva)
1. Usuario hace clic en "Continuar con Google"
2. Redirección a Google OAuth2
3. Usuario autoriza en Google
4. Google redirecciona con código
5. `OAuth2UserService.processOAuth2User()`:
   - Busca usuario existente por Google ID
   - Si no existe, crea nuevo usuario automáticamente
   - Asigna rol "DEVELOPER" por defecto
6. Autenticación completada
7. Redirección a dashboard apropiado

---

## 🎯 Características del Sistema

### Usuarios Internos
- **Tipo**: `interno`
- **Autenticación**: DNI/Email + Contraseña
- **Roles**: Todos los roles disponibles
- **Gestión**: Admin puede crear/modificar

### Usuarios Externos
- **Tipo**: `externo`
- **Autenticación**: Google OAuth2
- **Roles**: DEVELOPER por defecto
- **Gestión**: Creación automática en primer login
- **Datos**: Nombre, email y avatar desde Google

---

## 🔧 Configuración de Seguridad

### SecurityConfig.java
```java
// Configuración dual de autenticación
http
    .authorizeHttpRequests(authz -> authz
        .requestMatchers("/login", "/oauth2/**", "/login/oauth2/**").permitAll()
        .anyRequest().authenticated()
    )
    .formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/home")
        .failureUrl("/login?error=true")
    )
    .oauth2Login(oauth2 -> oauth2
        .loginPage("/login")
        .defaultSuccessUrl("/oauth2-success")
        .failureUrl("/oauth2-error")
        .userInfoEndpoint(userInfo -> userInfo
            .userService(oauth2UserService)
        )
    );
```

---

## 📊 Monitoreo y Debugging

### Logs Importantes
```bash
# OAuth2 User Processing
🔍 Procesando usuario OAuth2: user@example.com
📋 Atributos OAuth2: {sub=123, name=John Doe, email=user@example.com}

# Creación de Usuario Nuevo
🆕 Creando nuevo usuario externo: user@example.com
✅ Usuario externo creado exitosamente con DNI: EXT_123

# Autenticación Exitosa
✅ Usuario OAuth2 autenticado: user@example.com
```

### Verificación en Base de Datos
```sql
-- Ver usuarios externos creados
SELECT dni, nombre, correo, tipo_acceso, oauth_provider, oauth_provider_id, fecha_registro
FROM usuario 
WHERE tipo_acceso = 'externo'
ORDER BY fecha_registro DESC;

-- Estadísticas por tipo de acceso
SELECT 
    tipo_acceso,
    COUNT(*) as cantidad,
    COUNT(DISTINCT oauth_provider) as proveedores_unicos
FROM usuario 
GROUP BY tipo_acceso;
```

---

## 🛡️ Seguridad y Mejores Prácticas

### Validaciones Implementadas
- ✅ Verificación de email válido desde Google
- ✅ Generación automática de DNI único para externos
- ✅ Asignación segura de roles por defecto
- ✅ Validación de integridad de datos OAuth2

### Recomendaciones de Producción
1. **Variables de Entorno**: Mover client-id y client-secret a variables de entorno
2. **HTTPS**: Configurar SSL/TLS en producción
3. **Rate Limiting**: Implementar límites de intentos de login
4. **Logging**: Configurar logs de auditoría detallados
5. **Monitoring**: Supervisar intentos de OAuth2 fallidos

---

## 🔄 URLs y Endpoints

### Nuevos Endpoints
- `GET /oauth2/authorization/google` - Iniciar OAuth2 con Google
- `GET /login/oauth2/code/google` - Callback de Google OAuth2
- `GET /oauth2-success` - Manejo de éxito OAuth2
- `GET /oauth2-error` - Manejo de errores OAuth2

### URLs Existentes (Sin Cambios)
- `GET /login` - Página de login
- `POST /login` - Procesamiento de login interno
- `GET /home` - Redirección basada en rol

---

## 📞 Soporte y Troubleshooting

### Problemas Comunes

**1. Error "unauthorized_client"**
- Verificar Client ID y Client Secret
- Verificar URIs autorizados en Google Console

**2. Error "redirect_uri_mismatch"**
- Verificar URI de redirección en Google Console
- Debe ser exactamente: `http://localhost:8080/login/oauth2/code/google`

**3. Usuario no se crea automáticamente**
- Verificar logs de `OAuth2UserService`
- Verificar que rol "DEVELOPER" existe en BD

**4. Redirección incorrecta después de OAuth2**
- Verificar que el usuario tiene rol válido
- Verificar configuración de `defaultSuccessUrl`

### Contacto
Para soporte técnico, consultar con el equipo de desarrollo.

---

**✅ Implementación completada exitosamente**  
*Sistema de autenticación dual OAuth2 + credenciales internas*