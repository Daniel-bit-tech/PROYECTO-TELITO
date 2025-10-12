-- Script para arreglar problemas de base de datos

-- =========================================================
-- FASE 1: Diagnóstico completo de la base de datos
-- =========================================================

-- Verificar estructura de las tablas principales
DESCRIBE usuario;
DESCRIBE rol;
DESCRIBE organizacion;
DESCRIBE actividad_admin;

-- Verificar columnas de la tabla usuario
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'usuario' 
AND TABLE_SCHEMA = DATABASE()
ORDER BY COLUMN_NAME;

-- Verificar columnas de la tabla rol
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'rol' 
AND TABLE_SCHEMA = DATABASE()
ORDER BY COLUMN_NAME;

-- Verificar columnas de la tabla organizacion
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'organizacion' 
AND TABLE_SCHEMA = DATABASE()
ORDER BY COLUMN_NAME;

-- Verificar datos en tabla rol
SELECT * FROM rol;

-- Verificar datos en tabla organizacion  
SELECT * FROM organizacion;

-- Verificar columnas relacionadas con rol y organización
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'usuario' 
AND TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN ('idRol', 'id_rol', 'idOrganizacion', 'id_organizacion')
ORDER BY COLUMN_NAME;

-- =========================================================
-- FASE 2: Limpieza de columnas duplicadas (EJECUTAR DESPUÉS DEL DIAGNÓSTICO)
-- =========================================================

-- PASO 1: Migrar datos de columnas snake_case a camelCase si es necesario
UPDATE usuario SET idRol = id_rol WHERE idRol IS NULL AND id_rol IS NOT NULL;
UPDATE usuario SET idOrganizacion = id_organizacion WHERE idOrganizacion IS NULL AND id_organizacion IS NOT NULL;

-- PASO 2: Eliminar las columnas snake_case duplicadas
ALTER TABLE usuario DROP COLUMN id_rol;
ALTER TABLE usuario DROP COLUMN id_organizacion;

-- PASO 3: Asegurar que las columnas camelCase tengan las configuraciones correctas
ALTER TABLE usuario MODIFY COLUMN idRol INT NOT NULL;
ALTER TABLE usuario MODIFY COLUMN idOrganizacion INT NULL;

-- =========================================================
-- FASE 3: Verificación de integridad de datos
-- =========================================================

-- Verificar que todos los roles tienen IDs válidos
SELECT idRol, nombre_rol FROM rol ORDER BY idRol;

-- Verificar que todas las organizaciones tienen IDs válidos  
SELECT idOrganizacion, nombre FROM organizacion ORDER BY idOrganizacion;

-- Verificar registros de actividad problemáticos
SELECT COUNT(*) as total_registros FROM actividad_admin;
SELECT * FROM actividad_admin WHERE usuario_admin_dni IS NULL OR usuario_admin_dni = '' LIMIT 5;