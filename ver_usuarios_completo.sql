-- ============================================
-- VER USUARIOS DEV, QA Y PO CON CREDENCIALES
-- ============================================
-- CONTRASEÑA PARA TODOS: password123
-- ============================================

USE db_telito;

-- Ver todos los usuarios con sus correos y roles
SELECT 
    u.dni as 'DNI',
    u.nombre as 'Nombre',
    u.correo as 'Correo (para login)',
    r.nombre_rol as 'Rol',
    CASE WHEN u.estado = 1 THEN 'Activo' ELSE 'Inactivo' END as 'Estado',
    'password123' as 'Contraseña'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
ORDER BY r.nombre_rol, u.nombre;
