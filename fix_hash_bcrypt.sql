-- ============================================
-- CORREGIR HASH BCRYPT INCOMPLETO
-- ============================================

USE db_telito;

-- El hash correcto de BCrypt para "password" debe tener 60 caracteres
-- Hash actual: $2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2 (59 chars)
-- Hash correcto: $2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2. (60 chars)

-- Actualizar hash para DEV
UPDATE usuario u
JOIN rol r ON u.idRol = r.idRol
SET u.contrasena = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2.'
WHERE r.nombre_rol = 'DEV' AND u.correo = 'dev@telito.com';

-- Actualizar hash para QA
UPDATE usuario u
JOIN rol r ON u.idRol = r.idRol
SET u.contrasena = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2.'
WHERE r.nombre_rol = 'QA' AND u.correo = 'qa@telito.com';

-- Actualizar hash para PO
UPDATE usuario u
JOIN rol r ON u.idRol = r.idRol
SET u.contrasena = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2.'
WHERE r.nombre_rol = 'PO' AND u.correo = 'po@telito.com';

-- Verificar que ahora tenga 60 caracteres
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    r.nombre_rol as 'Rol',
    u.contrasena as 'Hash Completo',
    CHAR_LENGTH(u.contrasena) as 'Longitud Hash (debe ser 60)'
FROM usuario u 
JOIN rol r ON u.idRol = r.idRol 
WHERE r.nombre_rol IN ('DEV', 'QA', 'PO')
ORDER BY r.nombre_rol;
