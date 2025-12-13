-- =========================================================
-- Script para hacer idEquipo opcional (NULL) y limpiar referencias
-- =========================================================

USE db_telito;

-- Paso 1: Ver APIs con equipos que no existen
SELECT 
    a.idApi,
    a.nombre,
    a.idEquipo,
    'Equipo no existe' AS problema
FROM api a
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
WHERE a.idEquipo IS NOT NULL
AND e.idEquipo IS NULL;

-- Paso 2: Ver usuarios con equipos que no existen
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.idEquipo,
    'Equipo no existe' AS problema
FROM usuario u
LEFT JOIN equipo e ON u.idEquipo = e.idEquipo
WHERE u.idEquipo IS NOT NULL
AND e.idEquipo IS NULL;

-- Paso 3: Hacer que idEquipo sea opcional en API (permitir NULL)
ALTER TABLE api MODIFY COLUMN idEquipo INT NULL;

-- Paso 4: Hacer que idEquipo sea opcional en USUARIO (permitir NULL)
ALTER TABLE usuario MODIFY COLUMN idEquipo INT NULL;

-- Paso 5: Limpiar referencias huérfanas en API
UPDATE api a
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
SET a.idEquipo = NULL
WHERE a.idEquipo IS NOT NULL
AND e.idEquipo IS NULL;

-- Paso 6: Limpiar referencias huérfanas en USUARIO
UPDATE usuario u
LEFT JOIN equipo e ON u.idEquipo = e.idEquipo
SET u.idEquipo = NULL
WHERE u.idEquipo IS NOT NULL
AND e.idEquipo IS NULL;

-- Paso 7: Verificar APIs (ahora con equipo NULL está OK)
SELECT 
    a.idApi,
    a.nombre,
    a.idEquipo,
    e.nombre AS nombre_equipo
FROM api a
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
LIMIT 20;

-- Paso 8: Verificar usuarios (ahora con equipo NULL está OK)
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.idEquipo,
    e.nombre AS nombre_equipo,
    o.nombre AS organizacion
FROM usuario u
LEFT JOIN equipo e ON u.idEquipo = e.idEquipo
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
LIMIT 20;

-- RESUMEN:
-- ✅ Organización: OBLIGATORIA (idOrganizacion debe tener valor)
-- ✅ Equipo: OPCIONAL (idEquipo puede ser NULL)
-- ✅ Esto permite que usuarios y APIs pertenezcan a una organización sin necesidad de estar en un equipo específico
