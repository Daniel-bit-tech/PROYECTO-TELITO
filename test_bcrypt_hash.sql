-- ============================================
-- TEST BCRYPT HASH
-- ============================================

USE db_telito;

-- Ver el hash completo con caracteres visibles
SELECT 
    u.dni,
    u.correo,
    u.contrasena as 'Hash',
    CHAR_LENGTH(u.contrasena) as 'Longitud',
    HEX(u.contrasena) as 'Hash_HEX',
    -- Verificar cada caracter del final
    SUBSTRING(u.contrasena, 58, 3) as 'Ultimos_3_chars',
    ASCII(SUBSTRING(u.contrasena, 58, 1)) as 'Char_58',
    ASCII(SUBSTRING(u.contrasena, 59, 1)) as 'Char_59',
    ASCII(SUBSTRING(u.contrasena, 60, 1)) as 'Char_60'
FROM usuario u
WHERE u.correo = 'dev@telito.com';

-- Hash esperado para "password" con BCrypt strength 10:
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIJ5QQpaH6Z.M.mSJ1SfP5fy27Cu0u2.
-- Longitud: 60 caracteres
