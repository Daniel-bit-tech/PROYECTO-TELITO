-- Consultar usuarios DEV, QA y PO con sus credenciales
-- Las contraseñas están hasheadas con BCrypt, aquí están las originales:
-- CONTRASEÑA GENERAL PARA TODOS: "password123"

USE db_telito;

-- Ver la estructura de la tabla usuario
DESCRIBE usuario;

-- Consultar usuarios por rol
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    r.nombre_rol as 'Rol',
    u.estado,
    'password123' as 'Contraseña Original'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
ORDER BY r.nombre_rol, u.nombre;

-- Información adicional de usuarios
SELECT 
    r.nombre_rol as 'Rol',
    COUNT(*) as 'Cantidad de usuarios'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
GROUP BY r.nombre_rol;
