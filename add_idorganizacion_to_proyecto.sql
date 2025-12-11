-- Script para agregar columna idOrganizacion a la tabla proyecto
-- Ejecutar este script en MySQL Workbench o desde la línea de comandos

USE db_telito;

-- Paso 1: Verificar estructura actual de la tabla proyecto
DESCRIBE proyecto;

-- Paso 2: Desactivar temporalmente el modo seguro
SET SQL_SAFE_UPDATES = 0;

-- Paso 3: La columna idOrganizacion ya existe, continuamos con la asignación de valores

-- Paso 4: Asignar una organización por defecto a los proyectos sin organización
-- Opción A: Asignar la primera organización disponible
UPDATE proyecto 
SET idOrganizacion = (SELECT idOrganizacion FROM organizacion LIMIT 1)
WHERE idOrganizacion IS NULL;

-- Opción B (comentada): Si prefieres asignar manualmente por proyecto
-- UPDATE proyecto SET idOrganizacion = 1 WHERE idProyecto = X;

-- Paso 5: Hacer la columna NOT NULL (después de asignar valores)
ALTER TABLE proyecto 
MODIFY COLUMN idOrganizacion INT NOT NULL;

-- Paso 6: Agregar la foreign key
ALTER TABLE proyecto
ADD CONSTRAINT fk_proyecto_organizacion
FOREIGN KEY (idOrganizacion) REFERENCES organizacion(idOrganizacion)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- Paso 7: Reactivar el modo seguro
SET SQL_SAFE_UPDATES = 1;

-- Verificación final
SELECT 
    p.idProyecto,
    p.nombre AS nombre_proyecto,
    p.idOrganizacion,
    o.nombre AS nombre_organizacion,
    p.idEquipo
FROM proyecto p
LEFT JOIN organizacion o ON p.idOrganizacion = o.idOrganizacion;
