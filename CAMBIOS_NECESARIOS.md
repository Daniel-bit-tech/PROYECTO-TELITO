# RESUMEN DE CAMBIOS NECESARIOS EN EL CÓDIGO

## ✅ Cambios Completados

### 1. Entidad Proyecto
- ❌ Comentado: `usuarioLider` (atributo)
- ❌ Comentado: `getUsuarioLider()` y `setUsuarioLider()`
- **Razón**: La columna `dni_po_lider` NO existe en la tabla `proyecto`

### 2. Entidad API
- ❌ Comentado: `usuario` (atributo)
- ❌ Comentado: `getUsuario()` y `setUsuario()`
- **Razón**: La columna `idUsuario` NO existe en la tabla `api`

---

## ⚠️ Controladores que Requieren Cambios

### A. ProyectosPoController.java (línea 137)
```java
// COMENTAR ESTA VALIDACIÓN - usuarioLider no existe
if (!proyHasApi.getProyecto().getUsuarioLider().equals(usuario)
        && !proyHasApi.getProyecto().getOrganizacion().equals(usuario.getOrganizacion())) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este proyecto.");
}
```
**Reemplazar con**: Solo validar por organización

---

### B. ProyectosController.java
**Línea 162**: `proyecto.setUsuarioLider(usuario);`  
**Línea 192**: `proyectoOptional.get().getUsuarioLider().equals(usuario)`  
**Línea 234**: `existente.getUsuarioLider().equals(usuario)`  
**Línea 238**: `proyecto.setUsuarioLider(existente.getUsuarioLider());`  
**Línea 247**: `proyecto.setUsuarioLider(usuario);`  

**Acción**: Comentar todas estas líneas relacionadas con usuarioLider

---

### C. IssueController.java
**Línea 215**: `notif.setUsuario(newIssue.getReporte().getApi().getUsuario());`  
**Línea 315**: `Usuario desarrollador = issue.getReporte().getApi().getUsuario();`  
**Línea 356**: `Usuario dev = issue.getReporte().getApi().getUsuario();`  

**Acción**: Comentar - `api.getUsuario()` no existe

---

### D. ReporteController.java
**Línea 287**: `notif.setUsuario(usuario);` - Esta línea está bien  
**Pero hay referencia a API que podría fallar**

---

## 🛠️ Recomendación

### Opción 1: Arreglar el Código (Rápido)
1. Comentar TODAS las validaciones que usan `getUsuarioLider()`
2. Comentar TODAS las referencias a `api.getUsuario()`
3. Reemplazar la lógica de validación por solo verificar `organizacion`

### Opción 2: Arreglar la Base de Datos
1. Ejecutar el script `add_dni_po_lider_to_proyecto.sql`
2. Crear script similar para agregar `idUsuario` a la tabla `api`
3. Asignar desarrolladores a las APIs existentes

---

## 📋 Script SQL Necesario para API

```sql
USE db_telito;

SET SQL_SAFE_UPDATES = 0;

-- Agregar columna idUsuario a la tabla api
ALTER TABLE api 
ADD COLUMN idUsuario VARCHAR(8) NULL
AFTER idEstado;

-- Asignar un desarrollador (rol 3) por defecto
UPDATE api 
SET idUsuario = (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1)
WHERE idUsuario IS NULL;

-- Hacer NOT NULL
ALTER TABLE api 
MODIFY COLUMN idUsuario VARCHAR(8) NOT NULL;

-- Agregar foreign key
ALTER TABLE api
ADD CONSTRAINT fk_api_usuario
FOREIGN KEY (idUsuario) REFERENCES usuario(dni)
ON DELETE RESTRICT
ON UPDATE CASCADE;

SET SQL_SAFE_UPDATES = 1;
```

---

## ✅ Siguiente Paso Inmediato

**¿Qué prefieres?**
1. Comentar todo el código que usa estas relaciones (la app funcionará pero sin esas validaciones)
2. Ejecutar los scripts SQL para agregar las columnas faltantes (más completo pero requiere asignar datos)
