-- Verificar datos del usuario con DNI 73415980
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.apellido_materno,
    u.correo,
    u.correo_corporativo,
    u.estado,
    u.contrasena IS NOT NULL as tiene_password,
    LENGTH(u.contrasena) as longitud_hash,
    SUBSTRING(u.contrasena, 1, 10) as inicio_hash,
    o.nombre as organizacion,
    r.nombre_rol as rol
FROM usuario u 
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
WHERE u.dni = '73415980';

-- Verificar si hay un token de confirmación pendiente
SELECT 
    id,
    email,
    dni_usuario,
    nombre_temporal,
    usado,
    fecha_expiracion,
    fecha_expiracion > NOW() as token_valido
FROM tokens_confirmacion
WHERE dni_usuario = '73415980'
ORDER BY fecha_creacion DESC
LIMIT 1;
