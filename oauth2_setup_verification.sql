-- ==================================================
-- SCRIPT DE VERIFICACIÓN Y CONFIGURACIÓN OAUTH2
-- ==================================================
-- Este script verifica que la configuración OAuth2 esté lista
-- Ejecutar DESPUÉS de add_tipo_acceso_column.sql

USE db_telito;

-- 1. Verificar que la columna tipo_acceso existe
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND TABLE_NAME = 'usuario' 
  AND COLUMN_NAME IN ('tipo_acceso', 'oauth_provider_id', 'oauth_provider');

-- 2. Verificar roles existentes
SELECT idRol, nombre_rol, descripcion 
FROM rol 
ORDER BY idRol;

-- 3. Verificar si existe un rol por defecto para usuarios externos
-- Si no existe, crearlo
INSERT IGNORE INTO rol (nombre_rol, descripcion) 
VALUES ('DEVELOPER', 'Desarrollador externo con acceso limitado');

-- 4. Verificar la configuración
SELECT 
    'Configuración OAuth2' as verificacion,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'db_telito' 
              AND TABLE_NAME = 'usuario' 
              AND COLUMN_NAME = 'tipo_acceso'
        ) THEN '✅ Columna tipo_acceso existe'
        ELSE '❌ Columna tipo_acceso NO existe'
    END as estado_tipo_acceso,
    
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'db_telito' 
              AND TABLE_NAME = 'usuario' 
              AND COLUMN_NAME = 'oauth_provider_id'
        ) THEN '✅ Columna oauth_provider_id existe'
        ELSE '❌ Columna oauth_provider_id NO existe'
    END as estado_oauth_provider_id,
    
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'db_telito' 
              AND TABLE_NAME = 'usuario' 
              AND COLUMN_NAME = 'oauth_provider'
        ) THEN '✅ Columna oauth_provider existe'
        ELSE '❌ Columna oauth_provider NO existe'
    END as estado_oauth_provider,
    
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM rol WHERE nombre_rol = 'DEVELOPER'
        ) THEN '✅ Rol DEVELOPER existe'
        ELSE '❌ Rol DEVELOPER NO existe'
    END as estado_rol_developer;

-- 5. Mostrar ejemplo de consulta para usuarios externos
SELECT 'Ejemplo de consulta para usuarios externos' as info;

-- Consulta que el OAuth2UserService usará para verificar usuarios existentes
SELECT dni, nombre, apellido_paterno, correo, tipo_acceso, oauth_provider, oauth_provider_id
FROM usuario 
WHERE oauth_provider = 'google' 
  AND oauth_provider_id = 'ejemplo_google_id_123'
LIMIT 1;

-- 6. Estadísticas actuales
SELECT 
    COUNT(*) as total_usuarios,
    SUM(CASE WHEN tipo_acceso = 'interno' THEN 1 ELSE 0 END) as usuarios_internos,
    SUM(CASE WHEN tipo_acceso = 'externo' THEN 1 ELSE 0 END) as usuarios_externos,
    SUM(CASE WHEN oauth_provider IS NOT NULL THEN 1 ELSE 0 END) as usuarios_oauth2
FROM usuario;

-- 7. Verificar índices recomendados para OAuth2
SHOW INDEX FROM usuario WHERE Key_name LIKE '%oauth%' OR Column_name LIKE '%oauth%';

-- 8. Sugerir índices si no existen (opcional, para rendimiento)
-- NOTA: Estos índices mejorarán el rendimiento de las consultas OAuth2
-- Descomenta las siguientes líneas si quieres crearlos:

-- CREATE INDEX idx_usuario_oauth_provider_id ON usuario(oauth_provider_id, oauth_provider);
-- CREATE INDEX idx_usuario_tipo_acceso ON usuario(tipo_acceso);

SELECT '✅ Verificación completada. Revisa los resultados anteriores.' as resultado_final;