-- Script para actualizar correo corporativo y contraseña
-- Contraseña temporal: TempPass123!

-- 1. Actualizar correo corporativo y contraseña
UPDATE usuario 
SET 
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'
WHERE dni = '73415980';

-- 2. Verificar que se actualizó correctamente
SELECT 
    dni,
    nombre,
    correo,
    correo_corporativo,
    estado,
    LENGTH(contrasena) as longitud_hash,
    SUBSTRING(contrasena, 1, 30) as inicio_hash
FROM usuario
WHERE dni = '73415980';
