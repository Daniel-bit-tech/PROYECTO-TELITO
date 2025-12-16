-- Verificar qué organización es la ID 1
SELECT 
    idOrganizacion,
    nombre,
    dominio_correo,
    fecha_creacion
FROM organizacion
WHERE idOrganizacion = 1;
