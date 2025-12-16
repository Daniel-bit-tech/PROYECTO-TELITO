-- Verificar el token del usuario 73415980
SELECT 
    id,
    token,
    email,
    dni_usuario,
    nombre_temporal,
    id_rol_temporal,
    id_organizacion_temporal,
    usado,
    fecha_creacion,
    fecha_expiracion
FROM tokens_confirmacion
WHERE dni_usuario = '73415980'
ORDER BY fecha_creacion DESC
LIMIT 1;
