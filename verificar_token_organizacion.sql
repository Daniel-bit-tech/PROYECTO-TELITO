-- Script para verificar tokens y su información de organización
-- Ejecutar DESPUÉS de crear un nuevo usuario para ver si la organización se guardó

-- 1. Ver todos los tokens recientes (últimas 24 horas)
SELECT 
    id,
    token,
    email,
    dni_usuario,
    id_rol_temporal,
    id_organizacion_temporal,
    usado,
    fecha_creacion,
    fecha_expiracion,
    CASE 
        WHEN id_organizacion_temporal IS NULL THEN '❌ SIN ORGANIZACIÓN'
        ELSE '✅ CON ORGANIZACIÓN'
    END as estado_org
FROM tokens_confirmacion
WHERE fecha_creacion >= NOW() - INTERVAL 24 HOUR
ORDER BY fecha_creacion DESC;

-- 2. Ver el token más reciente con detalles completos
SELECT 
    t.id,
    t.token,
    t.email,
    t.dni_usuario,
    t.nombre_temporal,
    t.apellido_paterno_temporal,
    t.id_rol_temporal,
    r.nombreRol as nombre_rol,
    t.id_organizacion_temporal,
    o.nombre as nombre_organizacion,
    t.usado,
    t.fecha_creacion
FROM tokens_confirmacion t
LEFT JOIN rol r ON t.id_rol_temporal = r.idRol
LEFT JOIN organizacion o ON t.id_organizacion_temporal = o.idOrganizacion
ORDER BY t.fecha_creacion DESC
LIMIT 1;

-- 3. Ver usuarios creados recientemente (últimas 24 horas)
SELECT 
    u.dni,
    u.nombre,
    u.apellidoPaterno,
    u.correo,
    u.idRol,
    r.nombreRol,
    u.idOrganizacion,
    o.nombre as nombre_organizacion,
    u.correo_corporativo,
    u.estado,
    u.fechaRegistro,
    CASE 
        WHEN u.idOrganizacion IS NULL THEN '❌ SIN ORGANIZACIÓN'
        ELSE '✅ CON ORGANIZACIÓN'
    END as estado_org
FROM usuario u
LEFT JOIN rol r ON u.idRol = r.idRol
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.fechaRegistro >= NOW() - INTERVAL 24 HOUR
ORDER BY u.fechaRegistro DESC;

-- 4. Comparar token con usuario creado (para el token más reciente)
SELECT 
    'TOKEN' as tipo,
    t.email,
    t.dni_usuario as dni,
    t.id_organizacion_temporal as org_id,
    NULL as org_nombre,
    t.usado
FROM tokens_confirmacion t
WHERE t.id = (SELECT MAX(id) FROM tokens_confirmacion)

UNION ALL

SELECT 
    'USUARIO' as tipo,
    u.correo as email,
    u.dni,
    u.idOrganizacion as org_id,
    o.nombre as org_nombre,
    u.estado as usado
FROM usuario u
LEFT JOIN organizacion o ON u.idOrganizacion = o.idOrganizacion
WHERE u.dni = (
    SELECT dni_usuario 
    FROM tokens_confirmacion 
    WHERE id = (SELECT MAX(id) FROM tokens_confirmacion)
);
