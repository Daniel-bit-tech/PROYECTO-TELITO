-- Script simplificado para agregar columna idUsuario a la tabla api
-- Ejecutar este script en MySQL Workbench o desde la línea de comandos

USE db_telito;

-- Paso 1: Verificar y agregar la columna idUsuario si no existe
-- Si ya existe, simplemente continúa con los siguientes pasos
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'db_telito' 
    AND TABLE_NAME = 'api' 
    AND COLUMN_NAME = 'idUsuario'
);

-- Solo agregar si no existe (esto se omitirá si la columna ya existe)
-- Si ya ejecutaste el primer paso, puedes saltarte hasta el Paso 2

-- Paso 2: Desactivar temporalmente el modo seguro
SET SQL_SAFE_UPDATES = 0;

-- Asignar un valor por defecto a las APIs existentes
-- IMPORTANTE: Esto asigna el primer desarrollador encontrado a todas las APIs
-- Si necesitas asignar desarrolladores específicos, modifica este UPDATE
UPDATE api 
SET idUsuario = (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1)
WHERE idUsuario IS NULL;

-- Reactivar el modo seguro
SET SQL_SAFE_UPDATES = 1;

-- Paso 3: Hacer la columna NOT NULL
ALTER TABLE api 
MODIFY COLUMN idUsuario VARCHAR(8) NOT NULL;

-- Paso 4: Agregar la foreign key (solo si no existe)
-- Verificar si la constraint ya existe
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'db_telito' 
    AND TABLE_NAME = 'api' 
    AND CONSTRAINT_NAME = 'fk_api_usuario'
);

-- Si la foreign key no existe, agregarla
-- Si ya existe este mensaje, significa que todo está configurado correctamente
ALTER TABLE api
ADD CONSTRAINT fk_api_usuario
FOREIGN KEY (idUsuario) REFERENCES usuario(dni)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- Verificación
SELECT 
    a.idAPI,
    a.nombre,
    a.idUsuario,
    u.nombre AS nombre_desarrollador,
    u.apellido_paterno
FROM api a
LEFT JOIN usuario u ON a.idUsuario = u.dni;
