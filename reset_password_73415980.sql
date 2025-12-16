-- Script para resetear la contraseña del usuario con DNI 73415980
-- Nueva contraseña temporal: TempPass123!
-- Hash BCrypt generado con: $2a$10$

-- IMPORTANTE: Después de ejecutar este script, podrás iniciar sesión con:
-- Email: cesar.fabricio.tirado@datainsights.com
-- Password: TempPass123!

UPDATE usuario 
SET contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'
WHERE dni = '73415980';

-- Verificar que se actualizó correctamente
SELECT 
    dni,
    nombre,
    correo,
    correo_corporativo,
    LENGTH(contrasena) as longitud_hash,
    SUBSTRING(contrasena, 1, 20) as inicio_hash
FROM usuario
WHERE dni = '73415980';
