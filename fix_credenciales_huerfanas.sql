-- ================================================
-- SCRIPT: Limpiar credenciales huérfanas que referencian APIs inexistentes
-- FECHA: 2025-12-11
-- PROPÓSITO: Eliminar registros de credencialapi que apuntan a APIs que no existen
-- ================================================

-- 1. Verificar cuántas credenciales huérfanas existen
SELECT 
    c.idCredencialAPI,
    c.idUsuario,
    c.idAPI,
    'API NO EXISTE' as estado
FROM credencialapi c
LEFT JOIN api a ON c.idAPI = a.idApi
WHERE a.idApi IS NULL;

-- 2. Si hay resultados arriba, ejecutar la limpieza:
-- ADVERTENCIA: Esto eliminará permanentemente las credenciales huérfanas

-- OPCIÓN A: Desactivar temporalmente el safe mode (recomendado)
SET SQL_SAFE_UPDATES = 0;

DELETE FROM credencialapi
WHERE idAPI NOT IN (SELECT idApi FROM api);

-- Reactivar el safe mode
SET SQL_SAFE_UPDATES = 1;

-- OPCIÓN B: Si prefieres mantener safe mode, usa este DELETE con la clave primaria:
-- DELETE FROM credencialapi
-- WHERE idCredencialAPI IN (
--     SELECT idCredencialAPI FROM (
--         SELECT c.idCredencialAPI
--         FROM credencialapi c
--         LEFT JOIN api a ON c.idAPI = a.idApi
--         WHERE a.idApi IS NULL
--     ) AS temp
-- );

-- 3. Verificar que ya no hay credenciales huérfanas
SELECT COUNT(*) as credenciales_huerfanas_restantes
FROM credencialapi c
LEFT JOIN api a ON c.idAPI = a.idApi
WHERE a.idApi IS NULL;

-- 4. Verificar la integridad de los datos
SELECT 
    'Total APIs' as tipo,
    COUNT(*) as cantidad
FROM api
UNION ALL
SELECT 
    'Total Credenciales' as tipo,
    COUNT(*) as cantidad
FROM credencialapi
UNION ALL
SELECT 
    'Credenciales Válidas' as tipo,
    COUNT(*) as cantidad
FROM credencialapi c
INNER JOIN api a ON c.idAPI = a.idApi;

-- ================================================
-- FIN DEL SCRIPT
-- ================================================
