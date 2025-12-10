-- Verificar datos actuales en sol_acceso_equipo
USE db_telito;

-- Ver todas las solicitudes
SELECT * FROM sol_acceso_equipo;

-- Ver estructura de la tabla
DESCRIBE sol_acceso_equipo;

-- Verificar tipos de datos de las columnas de usuario
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND TABLE_NAME = 'sol_acceso_equipo'
  AND COLUMN_NAME IN ('idUsuario_solicitante', 'idUsuario_revisor');

-- Ver las foreign keys
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'db_telito'
  AND TABLE_NAME = 'sol_acceso_equipo'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
