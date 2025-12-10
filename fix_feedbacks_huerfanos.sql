-- Script para limpiar feedbacks huérfanos (actualizado)
-- Ejecutar en db_telito

USE db_telito;

-- 1. Verificar si la columna registrado_backlog existe, si no, crearla
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'feedback' 
AND COLUMN_NAME = 'registrado_backlog';

-- Si no existe, ejecutar esto:
ALTER TABLE feedback 
ADD COLUMN IF NOT EXISTS registrado_backlog BOOLEAN DEFAULT FALSE;

-- 2. Verificar feedbacks huérfanos (que referencian APIs que no existen)
SELECT 
    f.idFeedback,
    f.idAPI,
    f.comentario,
    f.calificacion,
    f.fecha_creacion,
    u.nombre as usuario_nombre,
    u.correo as usuario_correo
FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idApi
INNER JOIN usuario u ON f.idUsuario = u.dni
WHERE a.idApi IS NULL
ORDER BY f.fecha_creacion DESC;

-- 3. Contar feedbacks huérfanos
SELECT COUNT(*) as total_feedbacks_huerfanos
FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idApi
WHERE a.idApi IS NULL;

-- 4. OPCIÓN A: Eliminar feedbacks huérfanos directamente
--    (RECOMENDADO: Ya que no hay forma de recuperar la referencia a la API)
DELETE f FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idApi
WHERE a.idApi IS NULL;

-- 5. Verificar el resultado
SELECT 
    COUNT(*) as total_feedbacks,
    (SELECT COUNT(*) FROM feedback f LEFT JOIN api a ON f.idAPI = a.idApi WHERE a.idApi IS NULL) as feedbacks_huerfanos
FROM feedback;

-- 6. PREVENCIÓN: Verificar constraint de foreign key
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'db_telito'
AND TABLE_NAME = 'feedback'
AND REFERENCED_TABLE_NAME = 'api';

-- 7. Si no existe el constraint, agregarlo para prevenir futuros problemas
-- (OPCIONAL - puede fallar si ya existe)
/*
ALTER TABLE feedback
ADD CONSTRAINT fk_feedback_api
FOREIGN KEY (idAPI) REFERENCES api(idApi)
ON DELETE CASCADE;
*/

SELECT '✅ Script ejecutado. Feedbacks huérfanos eliminados.' as resultado;
