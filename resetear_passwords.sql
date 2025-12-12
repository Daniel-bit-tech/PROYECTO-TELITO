-- ============================================
-- RESETEAR CONTRASEÑAS A "password123"
-- ============================================
-- Hash BCrypt de "password123": $2a$10$N9qo8uLOickgx2ZMRZoMye IJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2
-- ============================================

USE db_telito;

-- Actualizar contraseñas de todos los usuarios DEV, QA y PO
UPDATE usuario u
JOIN rol r ON u.idRol = r.idRol
SET u.contrasena = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2'
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO');

-- Verificar que se actualizaron
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    r.nombre_rol as 'Rol',
    LEFT(u.contrasena, 30) as 'Hash (primeros 30 chars)',
    'password123' as 'Nueva Contraseña'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
ORDER BY r.nombre_rol, u.nombre;
