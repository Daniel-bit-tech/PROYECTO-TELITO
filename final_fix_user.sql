-- SOLUCIÓN DEFINITIVA: Arreglar el usuario 73415980
-- Asignar organización ID 1 (Data Insights) y generar correo corporativo

UPDATE usuario 
SET 
    idOrganizacion = 1,
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    alias = 'cesar.fabricio.tirado',
    contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'  -- TempPass123!
WHERE dni = '73415980';

-- Verificar que se actualizó correctamente
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
    r.nombre_rol as rol
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
WHERE u.dni = '73415980';
