-- OPCIÓN: Usar Telito Solutions (organización ID 1)
-- Correo corporativo será: cesar.fabricio.tirado@telito.com

UPDATE usuario 
SET 
    idOrganizacion = 1,
    correo_corporativo = 'cesar.fabricio.tirado@telito.com',
    alias = 'cesar.fabricio.tirado',
    contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'  -- TempPass123!
WHERE dni = '73415980';

-- Verificar
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
