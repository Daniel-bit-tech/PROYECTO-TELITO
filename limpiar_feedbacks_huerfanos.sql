-- Script para limpiar feedbacks huérfanos (que referencian APIs eliminadas)
-- Ejecutar en db_telito

USE db_telito;

-- 1. Verificar feedbacks huérfanos (que referencian APIs que no existen)
SELECT 
    f.idFeedback,
    f.idApi,
    f.comentario,
    f.calificacion,
    f.fechaCreacion,
    u.nombre as usuario_nombre,
    u.correo as usuario_correo
FROM feedback f
LEFT JOIN api a ON f.idApi = a.idApi
INNER JOIN usuario u ON f.idUsuario = u.idUsuario
WHERE a.idApi IS NULL
ORDER BY f.fechaCreacion DESC;

-- 2. Contar feedbacks huérfanos
SELECT COUNT(*) as total_feedbacks_huerfanos
FROM feedback f
LEFT JOIN api a ON f.idApi = a.idApi
WHERE a.idApi IS NULL;

-- 3. OPCIÓN A: Marcar feedbacks huérfanos como registrados en backlog
--    (Para mantener el historial pero que no aparezcan activos)
UPDATE feedback f
LEFT JOIN api a ON f.idApi = a.idApi
SET f.registradoBacklog = TRUE
WHERE a.idApi IS NULL;

-- 4. OPCIÓN B: Eliminar feedbacks huérfanos completamente
--    (DESCOMENTAR SOLO SI QUIERES ELIMINARLOS PERMANENTEMENTE)
/*
DELETE f FROM feedback f
LEFT JOIN api a ON f.idApi = a.idApi
WHERE a.idApi IS NULL;
*/

-- 5. Verificar el resultado
SELECT 
    COUNT(*) as feedbacks_activos,
    (SELECT COUNT(*) FROM feedback WHERE registradoBacklog = TRUE) as feedbacks_en_backlog
FROM feedback
WHERE registradoBacklog = FALSE;

-- 6. Mostrar estadísticas finales
SELECT 
    'Total de feedbacks' as categoria,
    COUNT(*) as cantidad
FROM feedback
UNION ALL
SELECT 
    'Feedbacks activos' as categoria,
    COUNT(*) as cantidad
FROM feedback
WHERE registradoBacklog = FALSE
UNION ALL
SELECT 
    'Feedbacks en backlog' as categoria,
    COUNT(*) as cantidad
FROM feedback
WHERE registradoBacklog = TRUE
UNION ALL
SELECT 
    'Feedbacks con API válida' as categoria,
    COUNT(*) as cantidad
FROM feedback f
INNER JOIN api a ON f.idApi = a.idApi
WHERE f.registradoBacklog = FALSE;

-- 7. PREVENCIÓN: Agregar constraint de foreign key si no existe
--    (Para prevenir futuros feedbacks huérfanos)
-- Nota: Esto puede fallar si ya hay datos huérfanos. Primero ejecuta los pasos anteriores.
/*
ALTER TABLE feedback
ADD CONSTRAINT fk_feedback_api
FOREIGN KEY (idApi) REFERENCES api(idApi)
ON DELETE CASCADE;
*/

SELECT '✅ Script ejecutado. Revisa los resultados arriba.' as resultado;
