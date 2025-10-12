-- Verificar duplicados en la tabla proyecto por dni_po_lider
SELECT dni_po_lider, COUNT(*) as cantidad
FROM proyecto 
WHERE dni_po_lider IS NOT NULL
GROUP BY dni_po_lider 
HAVING COUNT(*) > 1;

-- Ver todos los proyectos que tienen dni_po_lider = 55554444
SELECT * FROM proyecto WHERE dni_po_lider = '55554444';

-- Ver el usuario que está causando el problema
SELECT * FROM usuario WHERE dni = '55554444';