-- Script para arreglar completamente el usuario 73415980
-- IMPORTANTE: Reemplaza [ID_ORGANIZACION] con el ID real que encontraste en la consulta anterior

-- 1. Asignar organización y correo corporativo
UPDATE usuario 
SET 
    idOrganizacion = [ID_ORGANIZACION],  -- REEMPLAZA ESTO con el ID real (ejemplo: 1, 2, 3, etc.)
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    alias = 'cesar.fabricio.tirado'  -- Sin espacios
WHERE dni = '73415980';

-- 2. Verificar que se actualizó correctamente
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    u.correo_corporativo,
    u.alias,
    u.idOrganizacion,
    o.nombre as organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = '73415980';
