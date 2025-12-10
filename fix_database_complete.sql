-- Script completo para sincronizar la base de datos con las entidades Java
-- Ejecutar en MySQL Workbench

USE db_telito;

SET SQL_SAFE_UPDATES = 0;

-- ============================================================
-- 1. AGREGAR COLUMNA dni_po_lider A LA TABLA proyecto
-- ============================================================

-- Verificar si la columna ya existe
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'proyecto' 
AND COLUMN_NAME = 'dni_po_lider';

-- Agregar columna si no existe
SET @sql_add_lider = IF(@col_exists = 0,
    'ALTER TABLE proyecto ADD COLUMN dni_po_lider VARCHAR(8) NULL',
    'SELECT "La columna dni_po_lider ya existe" AS mensaje');
PREPARE stmt FROM @sql_add_lider;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Asignar el primer PO (rol 2) a los proyectos sin líder
UPDATE proyecto 
SET dni_po_lider = (SELECT dni FROM usuario WHERE idRol = 2 LIMIT 1)
WHERE dni_po_lider IS NULL;

-- Hacer la columna NOT NULL
ALTER TABLE proyecto 
MODIFY COLUMN dni_po_lider VARCHAR(8) NOT NULL;

-- Agregar foreign key si no existe
SET @fk_exists_lider = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'db_telito' 
    AND TABLE_NAME = 'proyecto' 
    AND CONSTRAINT_NAME = 'fk_proyecto_po_lider'
);

SET @sql_fk_lider = IF(@fk_exists_lider = 0,
    'ALTER TABLE proyecto ADD CONSTRAINT fk_proyecto_po_lider FOREIGN KEY (dni_po_lider) REFERENCES usuario(dni) ON DELETE RESTRICT ON UPDATE CASCADE',
    'SELECT "La FK fk_proyecto_po_lider ya existe" AS mensaje');
PREPARE stmt FROM @sql_fk_lider;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. AGREGAR COLUMNA idUsuario A LA TABLA api
-- ============================================================

-- Verificar si la columna ya existe
SELECT COUNT(*) INTO @col_exists_api 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'api' 
AND COLUMN_NAME = 'idUsuario';

-- Agregar columna si no existe
SET @sql_add_usuario = IF(@col_exists_api = 0,
    'ALTER TABLE api ADD COLUMN idUsuario VARCHAR(8) NULL AFTER idEstado',
    'SELECT "La columna idUsuario ya existe" AS mensaje');
PREPARE stmt FROM @sql_add_usuario;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Asignar el primer desarrollador (rol 3) a las APIs sin desarrollador
UPDATE api 
SET idUsuario = (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1)
WHERE idUsuario IS NULL;

-- Hacer la columna NOT NULL
ALTER TABLE api 
MODIFY COLUMN idUsuario VARCHAR(8) NOT NULL;

-- Agregar foreign key si no existe
SET @fk_exists_api = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'db_telito' 
    AND TABLE_NAME = 'api' 
    AND CONSTRAINT_NAME = 'fk_api_usuario'
);

SET @sql_fk_api = IF(@fk_exists_api = 0,
    'ALTER TABLE api ADD CONSTRAINT fk_api_usuario FOREIGN KEY (idUsuario) REFERENCES usuario(dni) ON DELETE RESTRICT ON UPDATE CASCADE',
    'SELECT "La FK fk_api_usuario ya existe" AS mensaje');
PREPARE stmt FROM @sql_fk_api;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET SQL_SAFE_UPDATES = 1;

-- ============================================================
-- VERIFICACIÓN FINAL
-- ============================================================

SELECT '=== VERIFICACIÓN DE PROYECTOS ===' AS '';
SELECT 
    p.idProyecto,
    p.nombre AS nombre_proyecto,
    p.dni_po_lider,
    u.nombre AS nombre_po,
    u.apellido_paterno AS apellido_po
FROM proyecto p
LEFT JOIN usuario u ON p.dni_po_lider = u.dni
LIMIT 10;

SELECT '=== VERIFICACIÓN DE APIS ===' AS '';
SELECT 
    a.idAPI,
    a.nombre AS nombre_api,
    a.idUsuario,
    u.nombre AS nombre_dev,
    u.apellido_paterno AS apellido_dev
FROM api a
LEFT JOIN usuario u ON a.idUsuario = u.dni
LIMIT 10;

SELECT 'Script ejecutado correctamente' AS resultado;
