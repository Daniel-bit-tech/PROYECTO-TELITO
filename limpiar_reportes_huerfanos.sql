-- Script para limpiar reportes que referencian APIs inexistentes
USE db_telito;

SET SQL_SAFE_UPDATES = 0;

-- 1. Ver cuántos reportes huérfanos hay
SELECT COUNT(*) AS reportes_huerfanos
FROM reporte
WHERE idApi NOT IN (SELECT idApi FROM api);

-- 2. Ver detalles de los reportes huérfanos
SELECT 
    r.idReporte,
    r.idApi,
    r.titulo,
    r.fecha_registro
FROM reporte r
WHERE r.idApi NOT IN (SELECT idApi FROM api);

-- 3. Eliminar los reportes huérfanos
DELETE FROM reporte
WHERE idApi NOT IN (SELECT idApi FROM api);

-- 4. Verificar que se eliminaron
SELECT COUNT(*) AS reportes_restantes_huerfanos
FROM reporte
WHERE idApi NOT IN (SELECT idApi FROM api);

SET SQL_SAFE_UPDATES = 1;

-- 5. Ver todos los reportes que quedaron
SELECT 
    r.idReporte,
    r.idApi,
    a.nombre AS nombre_api,
    r.titulo,
    r.fecha_registro
FROM reporte r
LEFT JOIN api a ON r.idApi = a.idApi
ORDER BY r.idApi;
