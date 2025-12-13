-- ================================================
-- SCRIPT: Solucionar APIs con referencias huérfanas
-- FECHA: 2025-12-13
-- PROPÓSITO: Crear equipos faltantes o corregir referencias
-- ================================================

USE db_telito;

-- ================================================
-- 1. VERIFICAR QUÉ APIs ESTÁN AFECTADAS
-- ================================================

SELECT idApi, nombre, idEquipo, 'Equipo no existe' as problema
FROM api 
WHERE idEquipo NOT IN (SELECT idEquipo FROM equipo);

-- ================================================
-- 2. CREAR LOS EQUIPOS FALTANTES
-- ================================================

-- Insertar equipos que no existen pero son referenciados
INSERT IGNORE INTO equipo (idEquipo, nombre, fecha_creacion, idOrganizacion)
SELECT DISTINCT a.idEquipo, 
       CONCAT('Equipo ', a.idEquipo), 
       NOW(),
       (SELECT idOrganizacion FROM organizacion LIMIT 1)
FROM api a
WHERE a.idEquipo NOT IN (SELECT idEquipo FROM equipo);

-- ================================================
-- 3. VERIFICACIÓN FINAL
-- ================================================

SELECT COUNT(*) as 'APIs con equipos inexistentes después de la corrección' 
FROM api 
WHERE idEquipo NOT IN (SELECT idEquipo FROM equipo);

-- ================================================
-- FIN DEL SCRIPT
-- ================================================
