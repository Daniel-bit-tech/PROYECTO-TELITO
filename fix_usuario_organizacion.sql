-- Script para asegurar que todos los usuarios tengan una organización asignada
-- y crear una organización por defecto para desarrolladores externos

-- Verificar organizaciones existentes
SELECT * FROM organizacion;

-- Crear organización por defecto para desarrolladores externos si no existe
INSERT INTO organizacion (nombre, descripcion, estado)
SELECT 'Desarrolladores Externos', 'Organización por defecto para desarrolladores externos', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM organizacion WHERE nombre = 'Desarrolladores Externos'
);

-- Obtener el ID de la organización por defecto
SET @org_default_id = (SELECT idOrganizacion FROM organizacion WHERE nombre = 'Desarrolladores Externos' LIMIT 1);

-- Verificar usuarios sin organización
SELECT dni, nombre, correo, idOrganizacion
FROM usuario 
WHERE idOrganizacion IS NULL;

-- Asignar organización por defecto a usuarios sin organización que sean desarrolladores (rol 2)
UPDATE usuario 
SET idOrganizacion = @org_default_id 
WHERE idOrganizacion IS NULL 
AND idRol = 2;

-- Verificar el resultado
SELECT u.dni, u.nombre, u.correo, u.idOrganizacion, o.nombre as organizacion_nombre
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.idRol = 2;

-- Mostrar estadísticas finales
SELECT 
    'Total usuarios desarrolladores' as tipo,
    COUNT(*) as cantidad
FROM usuario 
WHERE idRol = 2
UNION ALL
SELECT 
    'Desarrolladores con organización' as tipo,
    COUNT(*) as cantidad
FROM usuario 
WHERE idRol = 2 AND idOrganizacion IS NOT NULL
UNION ALL
SELECT 
    'Desarrolladores sin organización' as tipo,
    COUNT(*) as cantidad
FROM usuario 
WHERE idRol = 2 AND idOrganizacion IS NULL;
