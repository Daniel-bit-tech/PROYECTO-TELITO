-- Script para agregar la columna idUsuario a la tabla api
-- Esta columna es necesaria para la relación ManyToOne entre Api y Usuario

-- =========================================================
-- PASO 1: Verificar estructura actual de la tabla api
-- =========================================================
DESCRIBE api;

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'api' 
AND TABLE_SCHEMA = DATABASE()
ORDER BY ORDINAL_POSITION;

-- =========================================================
-- PASO 2: Agregar la columna idUsuario si no existe
-- =========================================================

-- Verificar si la columna ya existe
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api' 
    AND COLUMN_NAME = 'idUsuario'
);

-- Agregar columna idUsuario (desarrollador responsable de la API)
ALTER TABLE api 
ADD COLUMN IF NOT EXISTS idUsuario VARCHAR(8) NOT NULL
AFTER idEstado;

-- =========================================================
-- PASO 3: Agregar la clave foránea a la tabla usuario
-- =========================================================

-- Primero verificar si la constraint ya existe
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'api'
AND CONSTRAINT_NAME LIKE '%usuario%';

-- Agregar la foreign key constraint
ALTER TABLE api
ADD CONSTRAINT fk_api_usuario
FOREIGN KEY (idUsuario) REFERENCES usuario(dni)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- =========================================================
-- PASO 4: Verificar la estructura actualizada
-- =========================================================
DESCRIBE api;

-- Verificar las foreign keys de la tabla api
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'api'
AND REFERENCED_TABLE_NAME IS NOT NULL;
