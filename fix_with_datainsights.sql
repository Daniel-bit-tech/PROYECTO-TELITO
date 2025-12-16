-- SOLUCIÓN CORRECTA: Asignar Data Insights (ID 2) al usuario

UPDATE usuario 
SET 
    idOrganizacion = 2,
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    alias = 'cesar.fabricio.tirado',
    contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'  -- TempPass123!
WHERE dni = '73415980';

-- Verificar que todo está correcto
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
    r.nombre_rol as rol,
    LENGTH(u.contrasena) as longitud_password
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
WHERE u.dni = '73415980';
