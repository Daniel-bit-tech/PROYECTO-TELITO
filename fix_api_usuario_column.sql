-- Script para corregir la relación api-usuario
-- La PK de usuario es 'dni' (CHAR(8)), no 'idUsuario'

USE db_telito;

-- Verificar estado actual de la tabla api
DESCRIBE api;

-- SOLO SI LA COLUMNA idUsuario EXISTE Y ES INT:
-- Ejecutar estos pasos:

-- Paso 1: Actualizar los valores NULL con un DNI válido (SOLO si la columna existe como INT)
-- UPDATE api 
-- SET idUsuario = '77778888' 
-- WHERE idUsuario IS NULL OR idUsuario = '';

-- Paso 2: Eliminar la columna actual (SOLO si existe)
-- ALTER TABLE api DROP COLUMN idUsuario;

-- SI LA COLUMNA YA FUE ELIMINADA O NO EXISTE:
-- Ejecutar desde aquí:

-- Paso 3: Agregar la columna correcta (CHAR(8) para coincidir con dni)
ALTER TABLE api 
ADD COLUMN idUsuario CHAR(8) NOT NULL DEFAULT '77778888';

-- Paso 4: Crear la FK que apunta a usuario.dni
ALTER TABLE api 
ADD CONSTRAINT fk_api_usuario 
FOREIGN KEY (idUsuario) REFERENCES usuario(dni)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- Paso 5: Verificar que todo esté correcto
DESCRIBE api;
