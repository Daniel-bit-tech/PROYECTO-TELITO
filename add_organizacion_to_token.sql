-- =========================================================
-- Script para agregar campo id_organizacion_temporal a tokens_confirmacion
-- =========================================================
-- Este campo almacena temporalmente la organización del usuario
-- hasta que confirme su cuenta

USE db_telito;

-- Paso 1: Agregar la columna id_organizacion_temporal
ALTER TABLE tokens_confirmacion
ADD COLUMN id_organizacion_temporal INT NULL
COMMENT 'ID de la organización temporal del usuario'
AFTER id_rol_temporal;

-- Paso 2: Agregar foreign key (opcional, por si quieres validar integridad)
-- ALTER TABLE tokens_confirmacion
-- ADD CONSTRAINT fk_token_organizacion
-- FOREIGN KEY (id_organizacion_temporal) REFERENCES organizacion(idOrganizacion)
-- ON DELETE SET NULL
-- ON UPDATE CASCADE;

-- Paso 3: Verificar la estructura actualizada
DESCRIBE tokens_confirmacion;

-- Paso 4: Ver algunos registros
SELECT 
    id,
    email,
    dni_usuario,
    nombre_temporal,
    id_rol_temporal,
    id_organizacion_temporal,
    usado,
    fecha_expiracion
FROM tokens_confirmacion 
ORDER BY fecha_creacion DESC
LIMIT 10;
