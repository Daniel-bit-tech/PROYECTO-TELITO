-- =========================================================
-- Script para agregar campo correo_corporativo a usuario
-- =========================================================
-- Este campo almacena el email corporativo generado automáticamente
-- basado en el dominio de la organización del usuario

USE db_telito;

-- Paso 1: Agregar la columna correo_corporativo
ALTER TABLE usuario 
ADD COLUMN correo_corporativo VARCHAR(100) COMMENT 'Email corporativo del usuario (ej: nombre.apellido@telito.com)' NULL
AFTER correo;

-- Paso 2: Crear índice único para evitar duplicados
-- (comentado por si quieres permitir que varios usuarios compartan email corporativo)
-- CREATE UNIQUE INDEX idx_usuario_correo_corporativo ON usuario(correo_corporativo);

-- Paso 3: Verificar la estructura actualizada
DESCRIBE usuario;

-- Paso 4: Verificar los cambios
SELECT 
    dni,
    nombre,
    apellido_paterno,
    correo AS correo_personal,
    correo_corporativo,
    idOrganizacion
FROM usuario 
LIMIT 10;

-- =========================================================
-- OPCIONAL: Generar correos corporativos para usuarios existentes
-- =========================================================
-- Este UPDATE genera automáticamente el email corporativo
-- basado en el patrón: nombre.apellido@dominioOrganizacion

UPDATE usuario u
INNER JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
SET u.correo_corporativo = CONCAT(
    LOWER(REPLACE(u.nombre, ' ', '.')),
    '.',
    LOWER(u.apellido_paterno),
    '@',
    o.dominio_correo
)
WHERE u.correo_corporativo IS NULL
AND o.dominio_correo IS NOT NULL;

-- Verificar usuarios con email corporativo generado
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.correo AS correo_personal,
    u.correo_corporativo,
    o.nombre AS organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.correo_corporativo IS NOT NULL
LIMIT 10;

-- =========================================================
-- NOTAS IMPORTANTES
-- =========================================================
-- 1. correo_personal (columna 'correo'): 
--    - Usado para enviar notificaciones y códigos de confirmación
--    - Ingresado manualmente por el admin
--
-- 2. correo_corporativo (nueva columna):
--    - Generado automáticamente: nombre.apellido@dominioOrganizacion
--    - Será el USERNAME para hacer login
--    - No necesita ser un buzón real de correo
--
-- 3. Para hacer login, el usuario usará:
--    - Usuario: correo_corporativo (ej: cesar.torres@telito.com)
--    - Contraseña: la que el admin configuró
