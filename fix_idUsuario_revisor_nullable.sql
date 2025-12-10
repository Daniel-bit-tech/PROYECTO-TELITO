-- Fix: Hacer nullable el campo idUsuario_revisor en sol_acceso_equipo
-- El revisor no existe cuando se crea la solicitud, solo cuando se aprueba/rechaza
-- IMPORTANTE: La tabla usuario usa 'dni' (CHAR(8)) como PRIMARY KEY, no 'idUsuario'

USE db_telito;

-- Paso 1: Eliminar las foreign keys existentes (si existen)
-- Ejecuta primero esta consulta para ver qué constraints existen:
-- SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE 
-- WHERE TABLE_SCHEMA = 'db_telito' AND TABLE_NAME = 'sol_acceso_equipo' AND CONSTRAINT_NAME LIKE 'fk_%';

-- Descomenta y ejecuta solo las que realmente existen:
-- ALTER TABLE sol_acceso_equipo DROP FOREIGN KEY fk_sol_acceso_equipo_usuario1;
-- ALTER TABLE sol_acceso_equipo DROP FOREIGN KEY fk_sol_acceso_equipo_usuario2;

-- Paso 2: Eliminar foreign keys existentes primero (ejecuta solo las que existan)
-- Si alguna da error de "no existe", simplemente continúa con la siguiente

ALTER TABLE sol_acceso_equipo 
DROP FOREIGN KEY fk_sol_acceso_equipo_usuario_solicitante;

ALTER TABLE sol_acceso_equipo 
DROP FOREIGN KEY fk_sol_acceso_equipo_usuario_revisor;

-- Paso 3: Modificar las columnas al tipo correcto CHAR(8) para que coincidan con usuario.dni
ALTER TABLE sol_acceso_equipo 
MODIFY COLUMN idUsuario_solicitante CHAR(8) NOT NULL;

ALTER TABLE sol_acceso_equipo 
MODIFY COLUMN idUsuario_revisor CHAR(8) NULL;

-- Paso 4: Recrear las foreign keys correctamente apuntando a usuario.dni
ALTER TABLE sol_acceso_equipo 
ADD CONSTRAINT fk_sol_acceso_equipo_usuario_solicitante
FOREIGN KEY (idUsuario_solicitante) 
REFERENCES usuario(dni) 
ON DELETE RESTRICT 
ON UPDATE CASCADE;

ALTER TABLE sol_acceso_equipo 
ADD CONSTRAINT fk_sol_acceso_equipo_usuario_revisor 
FOREIGN KEY (idUsuario_revisor) 
REFERENCES usuario(dni) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- Verificar el cambio
DESCRIBE sol_acceso_equipo;
SHOW CREATE TABLE sol_acceso_equipo\G
