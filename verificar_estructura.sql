-- Script para verificar la estructura real de la base de datos
USE db_telito;

-- Ver estructura de la tabla proyecto
DESCRIBE proyecto;

-- Ver todas las columnas de proyecto
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'proyecto'
ORDER BY ORDINAL_POSITION;

-- Ver estructura de la tabla api
DESCRIBE api;

-- Ver todas las columnas de api
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'api'
ORDER BY ORDINAL_POSITION;
