-- Solución para errores de Schema-validation en tabla [proyecto]

-- 1. Agregar columna idOrganizacion (ya ejecutado anteriormente)
-- ALTER TABLE proyecto ADD COLUMN idOrganizacion INT NULL;
-- ALTER TABLE proyecto ADD CONSTRAINT fk_proyecto_organizacion 
-- FOREIGN KEY (idOrganizacion) REFERENCES organizacion(idOrganizacion) 
-- ON DELETE SET NULL ON UPDATE CASCADE;

-- 2. Agregar columna dni_po_lider para el líder del proyecto
-- La entidad Proyecto tiene @JoinColumn(name="dni_po_lider",nullable = false)
ALTER TABLE proyecto 
ADD COLUMN dni_po_lider CHAR(8) NOT NULL DEFAULT '77778888';

-- Agregar la restricción de clave foránea a usuario(dni)
ALTER TABLE proyecto 
ADD CONSTRAINT fk_proyecto_lider 
FOREIGN KEY (dni_po_lider) REFERENCES usuario(dni) 
ON DELETE RESTRICT 
ON UPDATE CASCADE;
