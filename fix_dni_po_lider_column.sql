-- Fix para columna dni_po_lider en tabla proyecto
-- Cambia CHAR a VARCHAR(8) para que coincida con la entidad JPA

USE db_telito;

ALTER TABLE proyecto MODIFY COLUMN dni_po_lider VARCHAR(8) NOT NULL;

-- Verificar el cambio
DESCRIBE proyecto;
