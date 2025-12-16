# Crear Organización con Primer Product Owner

## 📋 Descripción

Esta funcionalidad permite crear una nueva organización junto con su primer Product Owner (PO) en un solo flujo unificado.

## ✨ Características

### Lo que hace automáticamente:

1. **Crea la organización** con su información básica:
   - Nombre de la organización
   - Dominio de correo corporativo (ej: empresa.com)
   - Descripción (opcional)
   - Visibilidad (pública/privada)

2. **Crea el primer Product Owner**:
   - Consulta datos desde RENIEC usando el DNI
   - Auto-completa nombre y apellidos
   - Genera correo corporativo automáticamente: `nombre.apellido@dominio.com`
   - Asigna rol "PO" (Product Owner)
   - Crea token de confirmación de 6 dígitos

3. **Envía email de confirmación**:
   - Código de 6 dígitos al email del PO
   - Expira en 3 minutos (modo prueba)
   - El PO debe confirmar su cuenta para activarla

## 🎯 Flujo de Uso

### Paso 1: Abrir el Modal
1. Ir a **Admin** → **Gestión de Usuarios**
2. Hacer clic en el botón verde **"Nueva Organización + PO"**

### Paso 2: Datos de la Organización
Completar los campos de organización:

```
┌─────────────────────────────────────┐
│ 🏢 Datos de la Organización         │
├─────────────────────────────────────┤
│ Nombre: Acme Corporation            │
│ Dominio: acme.com                   │
│ Descripción: Empresa de tecnología  │
│ ☑ Organización pública              │
└─────────────────────────────────────┘
```

**Importante sobre el dominio:**
- ✅ Correcto: `empresa.com` o `miempresa.pe`
- ❌ Incorrecto: `@empresa.com` o `https://empresa.com`

### Paso 3: Consultar DNI en RENIEC
1. Ingresar el **DNI del Product Owner** (8 dígitos)
2. Hacer clic en **"Consultar DNI"**
3. Los campos de nombre y apellidos se auto-completan

```
┌─────────────────────────────────────┐
│ 👤 Datos del Primer Product Owner   │
├─────────────────────────────────────┤
│ DNI: 12345678 [Consultar DNI]       │
│ Nombre: Juan (auto-completado)      │
│ Apellido Pat.: Pérez (auto)         │
│ Apellido Mat.: García (auto)        │
└─────────────────────────────────────┘
```

### Paso 4: Completar Datos del PO
1. **Email personal**: Para recibir el código de confirmación
2. **Contraseña temporal**: Mínimo 8 caracteres
3. **Confirmar contraseña**: Debe coincidir

```
┌─────────────────────────────────────┐
│ Email: juan.perez@gmail.com         │
│ Contraseña: ••••••••                │
│ Confirmar: ••••••••                 │
├─────────────────────────────────────┤
│ ✨ Se generará automáticamente:     │
│ 📧 juan.perez@acme.com              │
│ 👔 Rol: Product Owner (PO)          │
│ ⏳ Estado: Pendiente confirmación   │
└─────────────────────────────────────┘
```

### Paso 5: Crear y Enviar
1. Hacer clic en **"Crear Organización y Enviar Email al PO"**
2. El sistema:
   - ✅ Crea la organización
   - ✅ Crea el usuario PO
   - ✅ Genera el correo corporativo
   - ✅ Envía código de confirmación por email

### Paso 6: Confirmación del PO
El Product Owner debe:
1. Revisar su email
2. Copiar el código de 6 dígitos
3. Ingresar a la página de confirmación
4. Establecer su contraseña final

## 🔐 Seguridad

### Validaciones Implementadas:

**Organización:**
- ✅ Nombre de organización mínimo 3 caracteres
- ✅ Dominio único (no puede haber duplicados)
- ✅ Formato de dominio válido (empresa.com)

**Product Owner:**
- ✅ DNI de 8 dígitos exactos
- ✅ DNI único (no puede estar registrado)
- ✅ Email único (no puede estar en uso)
- ✅ Contraseña mínimo 8 caracteres
- ✅ Contraseñas deben coincidir

**Anti-spam:**
- ✅ Máximo 5 tokens por IP por hora
- ✅ Código expira en 3 minutos (modo prueba)
- ✅ Un solo token válido por email/DNI

## 📊 Estados del Process

### Estado de la Organización:
```
CREADA ──────────────────────> ACTIVA
   │                              │
   └──> Tiene al menos 1 PO      │
                                  │
                       ┌──────────┘
                       │
                       v
              ✅ Completamente funcional
```

### Estado del Product Owner:
```
TOKEN ENVIADO ──> CUENTA CONFIRMADA ──> PUEDE INICIAR SESIÓN
      │                  │                       │
      │                  │                       v
      │                  │              ✅ Acceso completo
      │                  │                 como PO
      │                  │
      v                  v
 ⏰ Expira en      🔐 Contraseña
   3 minutos         establecida
```

## 🛠️ Archivos Modificados

### Backend:
1. **AdminUsuarioController.java**
   - Nuevo endpoint: `POST /admin/gestion-usuarios/crear-organizacion-con-po`
   - Método: `crearOrganizacionConPo()`
   - Transaccional: Sí (rollback si falla algo)

2. **OrganizacionRepository.java**
   - Nuevo método: `existsByDominioCorreo(String dominio)`

3. **RolRepository.java**
   - Nuevo método: `findByNombreRolIgnoreCase(String nombreRol)`

### Frontend:
1. **gestion-usuarios-dev-style.html**
   - Nuevo botón: "Nueva Organización + PO"
   - Nuevo modal: `newOrgWithPoModal`
   - Nueva función JS: `createOrganizacionConPo()`

## 📝 Ejemplo Completo

### Entrada:
```json
{
  "nombreOrganizacion": "Tech Solutions S.A.C.",
  "dominioCorreo": "techsolutions.com",
  "descripcionOrganizacion": "Empresa de desarrollo de software",
  "publica": true,
  
  "dni": "12345678",
  "nombre": "María",
  "apellidoPaterno": "González",
  "apellidoMaterno": "López",
  "correo": "maria.gonzalez@gmail.com",
  "contrasena": "Password123!"
}
```

### Resultado:
```
✅ Organización creada:
   - ID: 15
   - Nombre: Tech Solutions S.A.C.
   - Dominio: techsolutions.com
   
✅ Product Owner creado:
   - DNI: 12345678
   - Nombre: María González López
   - Email personal: maria.gonzalez@gmail.com
   - Email corporativo: maria.gonzalez@techsolutions.com
   - Rol: PO
   - Estado: Pendiente de confirmación
   
📧 Email enviado con código: 456789
⏰ Expira: 2025-01-20 10:33:00
```

## 🐛 Troubleshooting

### Problema: "Ya existe una organización con ese dominio"
**Solución:** El dominio de correo debe ser único. Usa otro dominio.

### Problema: "Ya existe un usuario con ese DNI"
**Solución:** El DNI ya está registrado. Verifica en la lista de usuarios.

### Problema: "Email no pudo ser enviado"
**Solución:** 
1. Verifica la configuración SMTP en `application.properties`
2. La organización y usuario se crearon correctamente
3. Puedes activar la cuenta manualmente desde "Tokens Pendientes"

### Problema: "Se ha excedido el límite de solicitudes"
**Solución:** Máximo 5 creaciones por hora desde una misma IP. Espera 1 hora.

## 🎓 Notas Técnicas

### Transaccionalidad:
El método `crearOrganizacionConPo()` es **@Transactional**, lo que significa:
- ✅ Si algo falla, TODO se revierte (rollback)
- ✅ Organización y PO se crean juntos o no se crea nada
- ✅ No quedan datos inconsistentes

### Generación del Correo Corporativo:
```java
nombre.apellido@dominio.com

Normalización:
- "María José" → "maria.jose"
- "García Pérez" → "garcia.perez"
- "Ñoño Núñez" → "nono.nunez"
```

### Token de Confirmación:
- 6 dígitos numéricos
- Expira en 3 minutos (configurable)
- Se invalida después del primer uso
- Se invalidan otros tokens del mismo email/DNI

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs del servidor (console output)
2. Verifica la configuración SMTP
3. Confirma que la base de datos esté actualizada
4. Consulta `ORDEN_SCRIPTS_BD.md` para los scripts necesarios

---
**Última actualización:** 2025-01-20  
**Versión:** 1.0.0  
**Autor:** GitHub Copilot
