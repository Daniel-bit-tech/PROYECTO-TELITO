# API Pagos

## Descripción general

La **API de Pagos** gestiona el procesamiento de transacciones, facturación y control de métodos de pago.  
Permite crear pagos, consultar su estado, emitir facturas y gestionar reembolsos de forma segura.  
Sigue los principios REST y utiliza JSON como formato de intercambio de datos.

---

## Base URL

```
https://api.midominio.com/v1/pagos
```

---

## Autenticación

### Tipo
**Bearer Token (JWT)**

Todas las operaciones requieren autenticación mediante token.  
El encabezado debe incluirse en cada solicitud:

```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. Crear pago

**POST** `/crear`

Crea una nueva transacción de pago.

#### Body JSON
| Campo        | Tipo     | Requerido | Descripción                          |
|---------------|----------|-----------|--------------------------------------|
| monto         | number   | ✔️        | Monto total a pagar.                 |
| moneda        | string   | ✔️        | Código ISO de la moneda (ej. "USD"). |
| metodoPagoId  | string   | ✔️        | ID del método de pago del usuario.   |
| descripcion   | string   | Opcional  | Descripción del pago.                |

#### Respuesta (201)
```json
{
  "id": "pay_93a12f",
  "estado": "pendiente",
  "monto": 49.99,
  "moneda": "USD",
  "descripcion": "Suscripción mensual",
  "fechaCreacion": "2025-10-10T12:00:00Z"
}
```

#### Errores comunes
| Código | Mensaje             | Descripción                          |
|---------|---------------------|--------------------------------------|
| 400     | INVALID_METHOD     | Método de pago inválido.             |
| 402     | PAYMENT_FAILED     | Error al procesar el pago.           |

---

### 2. Consultar pago

**GET** `/{id}`

Obtiene los detalles de un pago específico.

#### Respuesta (200)
```json
{
  "id": "pay_93a12f",
  "estado": "exitoso",
  "monto": 49.99,
  "moneda": "USD",
  "descripcion": "Suscripción mensual",
  "fechaCreacion": "2025-10-10T12:00:00Z",
  "fechaConfirmacion": "2025-10-10T12:05:34Z"
}
```

#### Errores comunes
| Código | Mensaje      | Descripción                   |
|---------|--------------|-------------------------------|
| 404     | NOT_FOUND    | Pago no encontrado.           |

---

### 3. Listar pagos del usuario

**GET** `/mis-pagos`

Devuelve todos los pagos asociados al usuario autenticado.

#### Parámetros de consulta
| Campo     | Tipo   | Requerido | Descripción                    |
|------------|--------|-----------|--------------------------------|
| estado     | string | Opcional  | Filtra por estado (ej. "exitoso"). |
| limite     | number | Opcional  | Número máximo de resultados.   |

#### Respuesta (200)
```json
{
  "data": [
    {
      "id": "pay_001",
      "estado": "exitoso",
      "monto": 49.99,
      "moneda": "USD",
      "descripcion": "Suscripción mensual"
    },
    {
      "id": "pay_002",
      "estado": "pendiente",
      "monto": 10.00,
      "moneda": "USD",
      "descripcion": "Recarga de crédito"
    }
  ]
}
```

---

### 4. Reembolsar pago

**POST** `/{id}/reembolso`

Solicita el reembolso de un pago exitoso.

#### Body JSON
| Campo        | Tipo     | Requerido | Descripción                      |
|---------------|----------|-----------|----------------------------------|
| motivo        | string   | Opcional  | Razón del reembolso.             |
| monto         | number   | Opcional  | Monto a reembolsar parcial.      |

#### Respuesta (200)
```json
{
  "id": "refund_12x91a",
  "pagoId": "pay_93a12f",
  "estado": "procesando",
  "monto": 49.99,
  "fecha": "2025-10-10T12:30:00Z"
}
```

#### Errores comunes
| Código | Mensaje             | Descripción                           |
|---------|---------------------|---------------------------------------|
| 400     | REFUND_NOT_ALLOWED | El pago no puede ser reembolsado.     |
| 409     | ALREADY_REFUNDED   | El pago ya fue reembolsado.           |

---

### 5. Emitir factura

**POST** `/factura`

Genera una factura digital a partir de un pago.

#### Body JSON
| Campo    | Tipo   | Requerido | Descripción              |
|-----------|--------|-----------|--------------------------|
| pagoId    | string | ✔️        | ID del pago confirmado.  |
| ruc       | string | ✔️        | RUC o número fiscal.     |
| razonSocial | string | ✔️      | Nombre o razón social.   |
| direccion | string | Opcional  | Dirección fiscal.        |

#### Respuesta (201)
```json
{
  "id": "inv_451xz",
  "pagoId": "pay_93a12f",
  "pdfUrl": "https://cdn.midominio.com/facturas/inv_451xz.pdf",
  "fechaEmision": "2025-10-10T12:40:00Z"
}
```

---

## Esquemas de datos

### Pago
```json
{
  "id": "string",
  "estado": "string",
  "monto": "number",
  "moneda": "string",
  "descripcion": "string",
  "fechaCreacion": "ISODate"
}
```

### Factura
```json
{
  "id": "string",
  "pagoId": "string",
  "pdfUrl": "string",
  "fechaEmision": "ISODate"
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
| 409     | CONFLICT         | Operación no permitida.           |
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
# Crear pago
curl -X POST https://api.midominio.com/v1/pagos/crear   -H "Authorization: Bearer <token>"   -H "Content-Type: application/json"   -d '{"monto": 49.99, "moneda": "USD", "metodoPagoId": "card_1234", "descripcion": "Suscripción mensual"}'

# Consultar estado
curl -X GET https://api.midominio.com/v1/pagos/pay_93a12f   -H "Authorization: Bearer <token>"

# Emitir factura
curl -X POST https://api.midominio.com/v1/pagos/factura   -H "Authorization: Bearer <token>"   -H "Content-Type: application/json"   -d '{"pagoId": "pay_93a12f", "ruc": "12345678901", "razonSocial": "Mi Empresa SAC"}'
```

---

## Metadatos

| Campo       | Valor                         |
|--------------|-------------------------------|
| API Name     | API Pagos                     |
| Versión      | 1.0.0                         |
| Formato      | REST + JSON                   |
| Autor        | Equipo Backend                |
| Fecha        | 2025-10-10                    |
| Licencia     | MIT                           |
| Documentación inspirada en | [Spotify Web API](https://developer.spotify.com/documentation/web-api) |
