-- ============================================
-- VERIFICAR CONEXIÓN Y CONTRASEÑAS
-- ============================================

USE db_telito;

-- 1. Ver si las contraseñas están hasheadas (deben empezar con $2a$ o $2b$)
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    r.nombre_rol,
    LEFT(u.contrasena, 30) as 'Primeros 30 chars de password',
    CASE 
        WHEN u.contrasena LIKE '$2a$%' OR u.contrasena LIKE '$2b$%' THEN 'BCrypt OK ✓'
        ELSE 'NO HASHEADA ✗'
    END as 'Estado Hash',
    u.estado as 'Activo'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
ORDER BY r.nombre_rol, u.nombre;

-- 2. Contar usuarios activos por rol
SELECT 
    r.nombre_rol as 'Rol',
    COUNT(*) as 'Total',
    SUM(CASE WHEN u.estado = 1 THEN 1 ELSE 0 END) as 'Activos'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
GROUP BY r.nombre_rol;
