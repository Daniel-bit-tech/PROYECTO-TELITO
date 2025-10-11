# API Usuarios

## Descripción general

La **API de Usuarios** permite la gestión centralizada de cuentas, autenticación y perfiles de usuario dentro del sistema.  
Proporciona endpoints REST para registrar nuevos usuarios, iniciar sesión, consultar y actualizar perfiles, y gestionar contraseñas.  

Todas las respuestas se devuelven en formato **JSON**.  
La API sigue convenciones REST y utiliza códigos de estado HTTP estándar.

---

## Base URL

```
https://api.midominio.com/v1/usuarios
```

---

## Autenticación

### Tipo
**Bearer Token (JWT)**

### Flujo de autenticación
1. El usuario se registra o inicia sesión mediante los endpoints `/register` o `/login`.
2. El servidor responde con un **token JWT**.
3. El cliente debe incluir este token en el encabezado de cada solicitud protegida:

```
Authorization: Bearer <token>
```

### Ejemplo de encabezado
```
GET /v1/usuarios/perfil
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
```

---

## Endpoints

### 1. Registro de usuario

**POST** `/register`

Registra un nuevo usuario en el sistema.

#### Parámetros (body JSON)
| Campo    | Tipo   | Requerido | Descripción                  |
|----------|--------|-----------|------------------------------|
| nombre   | string | ✔️        | Nombre completo del usuario. |
| email    | string | ✔️        | Correo electrónico único.    |
| password | string | ✔️        | Contraseña segura.           |

#### Respuesta (201)
```json
{
  "id": "b1a92f13-234d-4a01-9e52-2a893ed1d342",
  "nombre": "Nuevo Usuario",
  "email": "usuario@example.com",
  "createdAt": "2025-10-10T12:45:22Z"
}
```

#### Errores comunes
| Código | Mensaje              | Descripción                   |
|--------|----------------------|-------------------------------|
| 400    | EMAIL_ALREADY_EXISTS | El correo ya está registrado. |
| 422    | VALIDATION_ERROR     | Campos inválidos o faltantes. |

---

### 2. Inicio de sesión

**POST** `/login`

Autentica a un usuario y devuelve un token JWT.

#### Parámetros (body JSON)
| Campo    | Tipo   | Requerido | Descripción                    |
|----------|--------|-----------|--------------------------------|
| email    | string | ✔️        | Correo del usuario registrado. |
| password | string | ✔️        | Contraseña del usuario.        |

#### Respuesta (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

#### Errores comunes
| Código | Mensaje             | Descripción                  |
|--------|---------------------|------------------------------|
| 401    | INVALID_CREDENTIALS | Credenciales incorrectas.    |
| 429    | TOO_MANY_ATTEMPTS   | Límite de intentos excedido. |

---

### 3. Obtener perfil

**GET** `/perfil`

Obtiene la información del perfil del usuario autenticado.

#### Headers
```
Authorization: Bearer <token>
```

#### Respuesta (200)
```json
{
  "id": "b1a92f13-234d-4a01-9e52-2a893ed1d342",
  "nombre": "Usuario",
  "email": "usuario@example.com",
  "avatarUrl": "https://cdn.midominio.com/avatars/luis.jpg",
  "rol": "user",
  "createdAt": "2025-10-10T12:45:22Z"
}
```

---

### 4. Actualizar perfil

**PUT** `/perfil`

Actualiza los datos del perfil del usuario autenticado.

#### Body JSON
| Campo     | Tipo   | Requerido | Descripción         |
|-----------|--------|-----------|---------------------|
| nombre    | string | Opcional  | Nombre del usuario. |
| avatarUrl | string | Opcional  | URL del avatar.     |

#### Respuesta (200)
```json
{
  "id": "b1a92f13-234d-4a01-9e52-2a893ed1d342",
  "nombre": "Usuario Modificado",
  "email": "usuario@example.com",
  "avatarUrl": "https://cdn.midominio.com/avatars/luis2.jpg"
}
```

---

### 5. Cambiar contraseña

**POST** `/cambiar-password`

Permite actualizar la contraseña actual del usuario autenticado.

#### Body JSON
| Campo          | Tipo   | Requerido | Descripción                   |
|----------------|--------|-----------|-------------------------------|
| actualPassword | string | ✔️        | Contraseña actual.            |
| nuevaPassword  | string | ✔️        | Nueva contraseña segura.      |

#### Respuesta (200)
```json
{
  "message": "Contraseña actualizada correctamente."
}
```

#### Errores comunes
| Código | Mensaje           | Descripción                   |
|--------|-------------------|-------------------------------|
| 400    | PASSWORD_MISMATCH | Contraseña actual incorrecta. |

---

### 6. Eliminar cuenta

**DELETE** `/eliminar`

Elimina permanentemente la cuenta del usuario autenticado.

#### Respuesta (204)
Sin contenido.

---

## Esquemas de datos

### Usuario
```json
{
  "id": "uuid",
  "nombre": "string",
  "email": "string",
  "rol": "string",
  "avatarUrl": "string",
  "createdAt": "ISODate"
}
```

### Token
```json
{
  "token": "string",
  "expiresIn": "number"
}
```

---

## Códigos de error globales

| Código | Mensaje      | Descripción general         |
|--------|--------------|-----------------------------|
| 400    | BAD_REQUEST  | Parámetros incorrectos.     |
| 401    | UNAUTHORIZED | Token ausente o inválido.   |
| 403    | FORBIDDEN    | Acceso no permitido.        |
| 404    | NOT_FOUND    | Recurso no encontrado.      |
| 409    | CONFLICT     | Conflicto de datos.         |
| 500    | SERVER_ERROR | Error interno del servidor. |

---

## Versionado

Versión actual: **v1.0.0**

Cambios futuros se comunicarán mediante encabezado:
```
X-API-Version: 1
```

---

## Ejemplo de flujo completo

```bash
# Registrar
curl -X POST https://api.midominio.com/v1/usuarios/register   -H "Content-Type: application/json"   -d '{"nombre": "Luis", "email": "luis@example.com", "password": "12345"}'

# Login
curl -X POST https://api.midominio.com/v1/usuarios/login   -H "Content-Type: application/json"   -d '{"email": "luis@example.com", "password": "12345"}'

# Perfil
curl -X GET https://api.midominio.com/v1/usuarios/perfil   -H "Authorization: Bearer <token>"
```

---

## Metadatos

| Campo    | Valor          |
|----------|----------------|
| API Name | API Usuarios   |
| Versión  | 1.0.0          |
| Formato  | REST + JSON    |
| Autor    | Equipo Backend |
| Fecha    | 2025-10-10     |
| Licencia | MIT            |

