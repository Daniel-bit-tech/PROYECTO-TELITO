-- SOLUCIÓN TEMPORAL: Arreglar el usuario actual manualmente
-- Este script:
-- 1. Encuentra la organización Data Insights
-- 2. Asigna la organización al usuario
-- 3. Genera el correo corporativo
-- 4. Establece una contraseña conocida

-- Paso 1: Verificar que existe la organización
SELECT @orgId := idOrganizacion, @dominio := dominio_correo
FROM organizacion 
WHERE nombre LIKE '%Data%Insights%' OR dominio_correo = 'datainsights.com'
LIMIT 1;

-- Paso 2: Actualizar el usuario
UPDATE usuario 
SET 
    idOrganizacion = @orgId,
    correo_corporativo = 'cesar.fabricio.tirado@datainsights.com',
    alias = 'cesar.fabricio.tirado',
    contrasena = '$2a$10$N9qo8uLOickgx2ZGZkpjCe3RQLiP3L7WvgKKfH4q4L8GE1QWZ8yGi'  -- TempPass123!
WHERE dni = '73415980';

-- Paso 3: Verificar
SELECT 
    u.dni,
    u.nombre,
    u.correo,
    u.correo_corporativo,
    u.alias,
    u.idOrganizacion,
    o.nombre as organizacion,
    o.dominio_correo
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = '73415980';
