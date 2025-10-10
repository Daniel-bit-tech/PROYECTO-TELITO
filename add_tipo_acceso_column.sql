-- Script para agregar campo tipo_acceso a la tabla usuario
-- Este campo distingue entre usuarios internos (corporativos) y externos (OAuth2)

USE db_telito;

-- Agregar la columna tipo_acceso con valor por defecto 'interno' para usuarios existentes
ALTER TABLE usuario 
ADD COLUMN tipo_acceso ENUM('interno', 'externo') NOT NULL DEFAULT 'interno'
COMMENT 'Tipo de acceso: interno (credenciales corporativas) o externo (OAuth2)';

-- Crear índice para mejorar consultas por tipo de acceso
CREATE INDEX idx_usuario_tipo_acceso ON usuario(tipo_acceso);

-- Agregar campo oauth_provider_id para almacenar el ID único del proveedor OAuth2
ALTER TABLE usuario 
ADD COLUMN oauth_provider_id VARCHAR(255) NULL
COMMENT 'ID único del proveedor OAuth2 (Google, Azure, etc.)';

-- Agregar campo oauth_provider para identificar el proveedor utilizado
ALTER TABLE usuario 
ADD COLUMN oauth_provider VARCHAR(50) NULL
COMMENT 'Proveedor OAuth2 utilizado (google, azure, etc.)';

-- Crear índice compuesto para búsquedas eficientes de usuarios OAuth2
CREATE INDEX idx_usuario_oauth ON usuario(oauth_provider, oauth_provider_id);

-- Hacer que la contraseña sea opcional para usuarios externos (OAuth2)
ALTER TABLE usuario 
MODIFY COLUMN contrasena VARCHAR(256) NULL
COMMENT 'Contraseña hasheada - NULL para usuarios OAuth2';

-- Verificar los cambios
DESCRIBE usuario;

-- Mostrar usuarios existentes (todos deberían tener tipo_acceso = 'interno')
SELECT dni, nombre, correo, tipo_acceso, oauth_provider 
FROM usuario 
LIMIT 10;