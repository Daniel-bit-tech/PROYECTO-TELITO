-- ARREGLAR USUARIO ACTUAL 73415980

-- 1. Actualizar usuario con organización correcta
UPDATE usuario 
SET 
    idOrganizacion = 2,  -- Data Insights
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    alias = 'cesar.fabricio.tirado'
WHERE dni = '73415980';

-- 2. Verificar
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    u.correo_corporativo,
    u.alias,
    u.idOrganizacion,
    o.nombre as organizacion
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = '73415980';
