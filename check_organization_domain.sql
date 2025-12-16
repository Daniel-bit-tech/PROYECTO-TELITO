-- Verificar si el usuario tiene organización y si la organización tiene dominio
SELECT 
    u.dni,
    u.nombre,
    u.correo_corporativo,
    u.idOrganizacion,
    o.nombre as nombre_organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = '73415980';
