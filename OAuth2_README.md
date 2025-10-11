# PROYECTO-TELITO - OAuth2 Implementation

## ✅ OAuth2 Google Authentication - COMPLETADO

Este proyecto implementa autenticación OAuth2 con Google que automáticamente crea usuarios en la base de datos con roles apropiados.

## 🔧 Configuración Inicial

### 1. Variables de Entorno

Copia el archivo `.env.example` a `.env` y configura las siguientes variables:

```bash
# Google OAuth2 Configuration
GOOGLE_OAUTH2_CLIENT_ID=tu-client-id-aqui.apps.googleusercontent.com
GOOGLE_OAUTH2_CLIENT_SECRET=tu-client-secret-aqui

# Base de datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=telito_dev
DB_USERNAME=tu_usuario_mysql
DB_PASSWORD=tu_password_mysql
```

### 2. Configuración Google OAuth2

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la **Google+ API**
4. Ve a **APIs & Services > Credentials**
5. Crea **OAuth 2.0 Client IDs**
6. Configura los **Authorized redirect URIs**:
   - `http://localhost:8080/login/oauth2/code/google`
   - `https://tu-dominio.com/login/oauth2/code/google` (para producción)

## 🚀 Funcionalidades Implementadas

### ✅ Autenticación OAuth2 con Google
- Los usuarios pueden hacer login con su cuenta de Google
- No necesitan registrarse manualmente

### ✅ Creación Automática de Usuarios
- Al hacer login por primera vez, se crea automáticamente un usuario en la BD
- Se asigna el rol `DEVELOPER` por defecto
- Se almacena la información de Google (email, nombre, Google ID)

### ✅ Gestión de Roles
- Los usuarios OAuth2 obtienen el rol `ROLE_DEV`
- Pueden acceder a rutas `/dev/**` 
- El sistema de permisos funciona igual que con usuarios normales

### ✅ Integración con Sistema Existente
- Compatible con el sistema de autenticación tradicional (usuario/contraseña)
- Funciona con el sistema de impersonación
- Respeta los filtros de usuarios activos

## 🔧 Componentes Técnicos

### Archivos Principales

1. **`OAuth2UserService.java`** - Procesa usuarios OAuth2 y los crea en la BD
2. **`OAuth2AuthenticationSuccessHandler.java`** - Actualiza autoridades después del login
3. **`SecurityConfig.java`** - Configuración de Spring Security con OAuth2
4. **`OAuth2Config.java`** - Bean de configuración OAuth2
5. **`ImpersonationService.java`** - Actualizado para soportar usuarios OAuth2

### Flujo de Autenticación

1. Usuario hace clic en "Login with Google"
2. Spring Security redirige a Google OAuth2
3. Usuario autoriza la aplicación en Google
4. Google redirige de vuelta con el código de autorización
5. `OAuth2UserService` procesa al usuario:
   - Si es nuevo: crea usuario en BD con rol DEVELOPER
   - Si existe: actualiza información si es necesario
6. `OAuth2AuthenticationSuccessHandler` actualiza las autoridades en SecurityContext
7. Usuario es redirigido al dashboard correspondiente

## 🛡️ Seguridad

- ✅ Credenciales OAuth2 en variables de entorno
- ✅ No hay información sensible en el código fuente
- ✅ `.gitignore` configurado para excluir archivos de configuración sensibles
- ✅ Logs de debug eliminados para producción

## 🚧 Desarrollo Local

```bash
# 1. Configura las variables de entorno
cp .env.example .env
# Edita .env con tus credenciales reales

# 2. Ejecuta la aplicación
./mvnw spring-boot:run

# 3. Ve a http://localhost:8080/login
# 4. Haz clic en "Login with Google"
```

## 📋 TODO/Futuras Mejoras

- [ ] Soporte para más proveedores OAuth2 (Microsoft, GitHub, etc.)
- [ ] Configuración de roles por dominio de email
- [ ] Interfaz de administración para gestionar usuarios OAuth2
- [ ] Logs de auditoría para accesos OAuth2

## 🔍 Troubleshooting

### Error: "Invalid client credentials"
- Verifica que `GOOGLE_OAUTH2_CLIENT_ID` y `GOOGLE_OAUTH2_CLIENT_SECRET` estén configurados correctamente
- Confirma que el redirect URI esté configurado en Google Cloud Console

### Error: "Usuario no encontrado en la base de datos"
- Verifica que la tabla `Usuario` tenga las columnas `oauth_provider_id` y `oauth_provider`
- Confirma que existe un rol con nombre "DEV" o "DEVELOPER" en la tabla `Rol`

### Usuarios OAuth2 no pueden acceder al dashboard
- Verifica que el usuario se creó con `tipo_acceso = 'externo'`
- Confirma que el rol asignado tiene permisos para las rutas requeridas

---

## 📄 Licencia

Este proyecto es parte del sistema TELITO para la gestión de desarrollo y testing.