-- Script para agregar columna dni_po_lider a la tabla proyecto
-- Ejecutar este script en MySQL Workbench

USE db_telito;

-- Paso 1: Verificar estructura actual
DESCRIBE proyecto;

-- Paso 2: Desactivar modo seguro
SET SQL_SAFE_UPDATES = 0;

-- Paso 3: Agregar la columna dni_po_lider
ALTER TABLE proyecto 
ADD COLUMN dni_po_lider VARCHAR(8) NULL
AFTER idOrganizacion;

-- Paso 4: Asignar un PO líder por defecto a cada proyecto
-- Opción A: Asignar el primer PO (rol 2) encontrado
UPDATE proyecto 
SET dni_po_lider = (SELECT dni FROM usuario WHERE idRol = 2 LIMIT 1)
WHERE dni_po_lider IS NULL;

-- Opción B (comentada): Asignar manualmente por proyecto
-- UPDATE proyecto SET dni_po_lider = '12345678' WHERE idProyecto = 1;
-- UPDATE proyecto SET dni_po_lider = '87654321' WHERE idProyecto = 2;

-- Paso 5: Hacer la columna NOT NULL
ALTER TABLE proyecto 
MODIFY COLUMN dni_po_lider VARCHAR(8) NOT NULL;

-- Paso 6: Agregar la foreign key
ALTER TABLE proyecto
ADD CONSTRAINT fk_proyecto_po_lider
FOREIGN KEY (dni_po_lider) REFERENCES usuario(dni)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- Paso 7: Reactivar modo seguro
SET SQL_SAFE_UPDATES = 1;

-- Verificación final
SELECT 
    p.idProyecto,
    p.nombre AS nombre_proyecto,
    p.dni_po_lider,
    u.nombre AS nombre_po,
    u.apellido_paterno AS apellido_po,
    o.nombre AS organizacion
FROM proyecto p
LEFT JOIN usuario u ON p.dni_po_lider = u.dni
LEFT JOIN organizacion o ON p.idOrganizacion = o.idOrganizacion;
