-- Script para limpiar TODAS las credenciales huérfanas
-- Elimina credenciales que referencian APIs que no existen

USE db_telito;

SET SQL_SAFE_UPDATES = 0;

-- Ver cuántas credenciales huérfanas hay antes de eliminar
SELECT COUNT(*) AS total_credenciales_huerfanas
FROM credencialapi
WHERE idAPI NOT IN (SELECT idApi FROM api);

-- Eliminar todas las credenciales huérfanas
DELETE FROM credencialapi
WHERE idAPI NOT IN (SELECT idApi FROM api);

-- Verificar que se eliminaron correctamente
SELECT COUNT(*) AS credenciales_restantes_huerfanas
FROM credencialapi
WHERE idAPI NOT IN (SELECT idApi FROM api);

SET SQL_SAFE_UPDATES = 1;

-- Verificar las credenciales que quedaron
SELECT 
    c.idCredencialAPI,
    c.idUsuario,
    c.idAPI,
    a.nombre AS nombre_api
FROM credencialapi c
LEFT JOIN api a ON c.idAPI = a.idApi
ORDER BY c.idAPI;
