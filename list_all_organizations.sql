-- Buscar todas las organizaciones
SELECT 
    idOrganizacion,
    nombre,
    dominio_correo,
    fecha_creacion
FROM organizacion
ORDER BY idOrganizacion;
