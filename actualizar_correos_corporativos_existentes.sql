-- =========================================================
-- Script para actualizar correos corporativos de usuarios existentes
-- =========================================================

USE db_telito;

-- Paso 1: Ver usuarios sin correo corporativo pero CON organización
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.correo,
    u.correo_corporativo,
    u.idOrganizacion,
    o.nombre AS organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.correo_corporativo IS NULL
ORDER BY u.nombre;

-- Paso 2: Actualizar usuarios que tienen organización pero no correo corporativo
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
AND o.dominio_correo IS NOT NULL
AND u.idOrganizacion IS NOT NULL;

-- Paso 3: Verificar resultados
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.correo AS correo_personal,
    u.correo_corporativo,
    u.idOrganizacion,
    o.nombre AS organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
ORDER BY u.nombre;

-- Paso 4: Ver usuarios que SIGUEN sin correo corporativo (probablemente sin organización)
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.correo,
    u.idOrganizacion,
    'Sin organización asignada' AS motivo
FROM usuario u
WHERE u.correo_corporativo IS NULL
AND u.idOrganizacion IS NULL;
