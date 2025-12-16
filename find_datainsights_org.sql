-- 1. Buscar la organización Data Insights
SELECT 
    idOrganizacion,
    nombre,
    dominio_correo,
    fecha_creacion
FROM organizacion
WHERE nombre LIKE '%Data%' OR nombre LIKE '%Insight%' OR dominio_correo LIKE '%datainsights%';
