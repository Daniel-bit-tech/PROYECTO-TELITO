-- Verificar estado actual del usuario 73415980
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.correo,
    u.correo_corporativo,
    u.alias,
    u.estado,
    u.idOrganizacion,
    o.nombre as organizacion,
    o.dominio_correo,
    LENGTH(u.contrasena) as longitud_password,
    SUBSTRING(u.contrasena, 1, 30) as inicio_password
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = '73415980';
