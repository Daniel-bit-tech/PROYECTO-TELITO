# API Inventario

## Descripción general

La **API de Inventario** gestiona productos, existencias y movimientos de stock en almacenes.  
Permite registrar nuevos productos, actualizar su información, controlar entradas y salidas, y consultar el estado del inventario en tiempo real.  
Sigue el estilo **RESTful** y utiliza **JSON** como formato de intercambio.

---

## Base URL

```
https://api.midominio.com/v1/inventario
```

---

## Autenticación

### Tipo
**Bearer Token (JWT)**

Todos los endpoints requieren un token JWT válido en el encabezado `Authorization`:

```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. Crear producto

**POST** `/productos`

Registra un nuevo producto en el inventario.

#### Body JSON
| Campo          | Tipo     | Requerido | Descripción                            |
|----------------|----------|-----------|----------------------------------------|
| nombre         | string   | ✔️        | Nombre del producto.                   |
| sku            | string   | ✔️        | Código único de identificación.        |
| categoria      | string   | ✔️        | Categoría o tipo de producto.          |
| precio         | number   | ✔️        | Precio unitario.                       |
| stockInicial   | number   | ✔️        | Cantidad inicial en inventario.        |
| unidad         | string   | Opcional  | Unidad de medida (ej. "kg", "unid").   |

#### Respuesta (201)
```json
{
  "id": "prd_12345",
  "nombre": "Laptop HP",
  "sku": "HP-15-2025",
  "categoria": "Electrónica",
  "precio": 2500.00,
  "stock": 10,
  "unidad": "unid",
  "fechaCreacion": "2025-10-10T12:30:00Z"
}
```

---

### 2. Listar productos

**GET** `/productos`

Devuelve una lista de productos registrados.

#### Parámetros de consulta
| Campo     | Tipo   | Requerido | Descripción                            |
|------------|--------|-----------|----------------------------------------|
| categoria  | string | Opcional  | Filtra por categoría.                  |
| limite     | number | Opcional  | Límite de resultados.                  |
| pagina     | number | Opcional  | Página actual (paginación).            |

#### Respuesta (200)
```json
{
  "data": [
    {
      "id": "prd_12345",
      "nombre": "Laptop HP",
      "sku": "HP-15-2025",
      "stock": 10,
      "precio": 2500.00
    },
    {
      "id": "prd_98765",
      "nombre": "Mouse Logitech",
      "sku": "LOG-MSE-001",
      "stock": 45,
      "precio": 80.00
    }
  ]
}
```

---

### 3. Obtener detalle de producto

**GET** `/productos/{id}`

Obtiene la información completa de un producto.

#### Respuesta (200)
```json
{
  "id": "prd_12345",
  "nombre": "Laptop HP",
  "sku": "HP-15-2025",
  "categoria": "Electrónica",
  "precio": 2500.00,
  "stock": 10,
  "unidad": "unid",
  "almacen": "Principal",
  "fechaCreacion": "2025-10-10T12:30:00Z"
}
```

#### Errores comunes
| Código | Mensaje     | Descripción                    |
|---------|--------------|--------------------------------|
| 404     | NOT_FOUND    | Producto no encontrado.        |

---

### 4. Actualizar producto

**PUT** `/productos/{id}`

Actualiza los datos de un producto existente.

#### Body JSON
| Campo     | Tipo   | Requerido | Descripción                |
|------------|--------|-----------|----------------------------|
| nombre     | string | Opcional  | Nombre del producto.       |
| precio     | number | Opcional  | Precio actualizado.        |
| categoria  | string | Opcional  | Categoría del producto.    |

#### Respuesta (200)
```json
{
  "id": "prd_12345",
  "nombre": "Laptop HP 2025",
  "precio": 2600.00,
  "categoria": "Electrónica"
}
```

---

### 5. Registrar movimiento de stock

**POST** `/movimientos`

Registra una entrada o salida de stock.

#### Body JSON
| Campo        | Tipo     | Requerido | Descripción                           |
|---------------|----------|-----------|---------------------------------------|
| productoId    | string   | ✔️        | ID del producto.                      |
| tipo          | string   | ✔️        | "entrada" o "salida".                 |
| cantidad      | number   | ✔️        | Cantidad a registrar.                 |
| motivo        | string   | Opcional  | Descripción del movimiento.           |

#### Respuesta (201)
```json
{
  "id": "mov_001",
  "productoId": "prd_12345",
  "tipo": "entrada",
  "cantidad": 5,
  "motivo": "Reabastecimiento",
  "fecha": "2025-10-10T13:00:00Z"
}
```

#### Errores comunes
| Código | Mensaje            | Descripción                         |
|---------|--------------------|-------------------------------------|
| 400     | INVALID_OPERATION | Operación inválida (sin stock).     |
| 404     | PRODUCT_NOT_FOUND | Producto no existe.                 |

---

### 6. Consultar movimientos de producto

**GET** `/productos/{id}/movimientos`

Devuelve el historial de movimientos de stock de un producto.

#### Respuesta (200)
```json
{
  "data": [
    {
      "id": "mov_001",
      "tipo": "entrada",
      "cantidad": 5,
      "motivo": "Reabastecimiento",
      "fecha": "2025-10-10T13:00:00Z"
    },
    {
      "id": "mov_002",
      "tipo": "salida",
      "cantidad": 2,
      "motivo": "Venta de producto",
      "fecha": "2025-10-11T09:20:00Z"
    }
  ]
}
```

---

## Esquemas de datos

### Producto
```json
{
  "id": "string",
  "nombre": "string",
  "sku": "string",
  "categoria": "string",
  "precio": "number",
  "stock": "number",
  "unidad": "string"
}
```

### Movimiento
```json
{
  "id": "string",
  "productoId": "string",
  "tipo": "string",
  "cantidad": "number",
  "motivo": "string",
  "fecha": "ISODate"
}
```

---

## Códigos de error globales

| Código | Mensaje          | Descripción general               |
|---------|------------------|-----------------------------------|
| 400     | BAD_REQUEST      | Parámetros inválidos.             |
| 401     | UNAUTHORIZED     | Token inválido o ausente.         |
| 403     | FORBIDDEN        | Acceso denegado.                  |
| 404     | NOT_FOUND        | Recurso no encontrado.            |
| 409     | CONFLICT         | Conflicto en datos.               |
| 500     | SERVER_ERROR     | Error interno del servidor.       |

---

## Versionado

Versión actual: **v1.0.0**

Encabezado para futuras versiones:
```
X-API-Version: 1
```

---

## Ejemplo de flujo completo

```bash
# Crear producto
curl -X POST https://api.midominio.com/v1/inventario/productos   -H "Authorization: Bearer <token>"   -H "Content-Type: application/json"   -d '{"nombre": "Mouse Logitech", "sku": "LOG-MSE-001", "categoria": "Periféricos", "precio": 80.00, "stockInicial": 10}'

# Registrar movimiento
curl -X POST https://api.midominio.com/v1/inventario/movimientos   -H "Authorization: Bearer <token>"   -H "Content-Type: application/json"   -d '{"productoId": "prd_12345", "tipo": "salida", "cantidad": 2, "motivo": "Venta"}'
```

---

## Metadatos

| Campo       | Valor                         |
|--------------|-------------------------------|
| API Name     | API Inventario                |
| Versión      | 1.0.0                         |
| Formato      | REST + JSON                   |
| Autor        | Equipo Backend                |
| Fecha        | 2025-10-10                    |
| Licencia     | MIT                           |
| Documentación inspirada en | [Spotify Web API](https://developer.spotify.com/documentation/web-api) |
