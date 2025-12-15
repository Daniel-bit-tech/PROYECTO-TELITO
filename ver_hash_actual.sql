-- ============================================
-- VERIFICAR SI EL UPDATE FUNCIONÓ
-- ============================================

USE db_telito;

-- Ver las contraseñas actuales completas
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    r.nombre_rol as 'Rol',
    u.contrasena as 'Hash Completo',
    CHAR_LENGTH(u.contrasena) as 'Longitud Hash'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
AND u.correo = 'dev@telito.com'
LIMIT 1;
