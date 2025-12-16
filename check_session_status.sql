-- ============================================
-- VERIFICACIÓN RÁPIDA DE PERSISTENCIA
-- ============================================

USE db_telito;

-- 1. Verificar tablas existen
SELECT '🔍 VERIFICACIÓN DE TABLAS' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) = 3 THEN '✅ TODAS LAS TABLAS EXISTEN'
        ELSE '❌ FALTAN TABLAS'
    END AS Status,
    COUNT(*) AS 'Tablas encontradas'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND (TABLE_NAME IN ('SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES', 'persistent_logins'));

-- 2. Listar tablas encontradas
SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Filas',
    ROUND(DATA_LENGTH / 1024, 2) AS 'Tamaño (KB)'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND (TABLE_NAME LIKE 'SPRING_SESSION%' OR TABLE_NAME = 'persistent_logins')
ORDER BY TABLE_NAME;

-- 3. Ver sesiones activas
SELECT '📊 SESIONES ACTIVAS' AS '';
SELECT 
    COUNT(*) AS 'Total Sesiones Activas',
    COUNT(DISTINCT PRINCIPAL_NAME) AS 'Usuarios Únicos'
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000;

-- Detalle de sesiones
SELECT 
    PRINCIPAL_NAME AS Usuario,
    FROM_UNIXTIME(LAST_ACCESS_TIME/1000) AS 'Último Acceso',
    ROUND((EXPIRY_TIME - (UNIX_TIMESTAMP() * 1000)) / 1000 / 3600, 1) AS 'Horas Restantes'
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000
ORDER BY LAST_ACCESS_TIME DESC
LIMIT 10;

-- 4. Ver tokens Remember-Me
SELECT '🔐 TOKENS REMEMBER-ME' AS '';
SELECT 
    COUNT(*) AS 'Total Tokens',
    COUNT(DISTINCT username) AS 'Usuarios con Token'
FROM persistent_logins;

-- Detalle de tokens
SELECT 
    username AS Usuario,
    last_used AS 'Último Uso',
    TIMESTAMPDIFF(HOUR, last_used, NOW()) AS 'Horas desde uso'
FROM persistent_logins
ORDER BY last_used DESC
LIMIT 10;

-- 5. Resumen final
SELECT '✅ RESUMEN FINAL' AS '';
SELECT 
    'Configuración' AS Item,
    CASE 
        WHEN (SELECT COUNT(*) FROM information_schema.TABLES 
              WHERE TABLE_SCHEMA = 'db_telito' 
              AND TABLE_NAME IN ('SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES', 'persistent_logins')) = 3
        THEN '✅ LISTO'
        ELSE '❌ INCOMPLETO'
    END AS Estado
UNION ALL
SELECT 
    'Sesiones Activas',
    CONCAT(COUNT(*), ' activas') 
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000
UNION ALL
SELECT 
    'Tokens Remember-Me',
    CONCAT(COUNT(*), ' tokens') 
FROM persistent_logins;

-- Recomendación
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM information_schema.TABLES 
              WHERE TABLE_SCHEMA = 'db_telito' 
              AND TABLE_NAME IN ('SPRING_SESSION', 'SPRING_SESSION_ATTRIBUTES', 'persistent_logins')) = 3
        THEN '✅ SISTEMA LISTO - Puedes reiniciar el servidor, las sesiones persistirán'
        ELSE '❌ EJECUTA setup_session_persistence.sql PRIMERO'
    END AS 'Recomendación';
