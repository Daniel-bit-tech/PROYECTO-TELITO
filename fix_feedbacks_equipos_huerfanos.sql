-- ============================================
-- FIX: Limpiar feedbacks con referencias huérfanas a equipos
-- ============================================
-- Problema: Feedbacks referencian APIs que tienen equipos inexistentes (ID: 87654321)
-- Solución: Identificar y corregir/eliminar referencias huérfanas
-- ============================================

USE db_telito;

-- PASO 1: Identificar feedbacks problemáticos
SELECT 
    f.idFeedback,
    f.comentario,
    f.calificacion,
    f.idAPI,
    a.nombre AS api_nombre,
    a.idEquipo AS equipo_id,
    CASE 
        WHEN e.idEquipo IS NULL THEN '❌ EQUIPO NO EXISTE'
        ELSE '✅ OK'
    END AS estado_equipo
FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idAPI
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
WHERE a.idEquipo IS NOT NULL AND e.idEquipo IS NULL;

-- PASO 2: Contar feedbacks afectados
SELECT 
    COUNT(*) AS feedbacks_con_equipos_huerfanos
FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idAPI
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
WHERE a.idEquipo IS NOT NULL AND e.idEquipo IS NULL;

-- PASO 3: Ver las APIs con equipos huérfanos
SELECT 
    a.idAPI,
    a.nombre,
    a.idEquipo AS equipo_inexistente,
    COUNT(f.idFeedback) AS cantidad_feedbacks
FROM api a
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
LEFT JOIN feedback f ON a.idAPI = f.idAPI
WHERE a.idEquipo IS NOT NULL AND e.idEquipo IS NULL
GROUP BY a.idAPI, a.nombre, a.idEquipo;

-- PASO 4: SOLUCIÓN - Actualizar APIs para asignarlas a un equipo válido
-- Obtener el primer equipo válido
SELECT @equipoValido := MIN(idEquipo) FROM equipo LIMIT 1;

-- Mostrar el equipo al que se asignarán las APIs huérfanas
SELECT 
    @equipoValido AS id_equipo_destino,
    nombre AS nombre_equipo,
    descripcion
FROM equipo 
WHERE idEquipo = @equipoValido;

-- Actualizar las APIs con equipos inexistentes
UPDATE api 
SET idEquipo = @equipoValido
WHERE idEquipo NOT IN (SELECT idEquipo FROM equipo);

-- PASO 5: Verificar que se corrigió el problema
SELECT 
    'APIs corregidas' AS resultado,
    COUNT(*) AS cantidad
FROM api a
WHERE a.idEquipo = @equipoValido;

-- PASO 6: Verificar que ya no hay feedbacks con equipos huérfanos
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ No hay feedbacks con equipos huérfanos'
        ELSE CONCAT('⚠️ Aún hay ', COUNT(*), ' feedbacks con problemas')
    END AS estado_final
FROM feedback f
LEFT JOIN api a ON f.idAPI = a.idAPI
LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
WHERE a.idEquipo IS NOT NULL AND e.idEquipo IS NULL;

-- ALTERNATIVA: Si prefieres eliminar los feedbacks huérfanos (DESCOMENTAR SI DESEAS USAR)
-- DELETE f FROM feedback f
-- LEFT JOIN api a ON f.idAPI = a.idAPI
-- LEFT JOIN equipo e ON a.idEquipo = e.idEquipo
-- WHERE a.idEquipo IS NOT NULL AND e.idEquipo IS NULL;

SELECT '✅ Script completado. Revisa los resultados arriba.' AS mensaje;
