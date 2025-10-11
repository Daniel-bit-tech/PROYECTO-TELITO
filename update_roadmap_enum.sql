-- Script para actualizar los valores del enum en la tabla roadmap
-- Cambiar espacios por guiones bajos para que coincida con el enum Java

USE db_telito;

-- Desactivar safe mode temporalmente
SET SQL_SAFE_UPDATES = 0;

-- PRIMERO: Alterar la tabla para permitir tanto valores antiguos como nuevos
ALTER TABLE roadmap MODIFY COLUMN estado ENUM('Nueva','En desarrollo','En_desarrollo','Próxima') NOT NULL;

-- SEGUNDO: Actualizar los valores existentes
UPDATE roadmap SET estado = 'En_desarrollo' WHERE estado = 'En desarrollo';

-- TERCERO: Limpiar la definición del enum para quitar los valores antiguos
ALTER TABLE roadmap MODIFY COLUMN estado ENUM('Nueva','En_desarrollo','Próxima') NOT NULL;

-- Reactivar safe mode
SET SQL_SAFE_UPDATES = 1;

-- Verificar los cambios
SELECT DISTINCT estado FROM roadmap;