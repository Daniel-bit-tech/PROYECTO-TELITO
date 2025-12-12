-- =========================================================
-- CONFIGURACIÓN COMPLETA DE PERSISTENCIA DE SESIONES
-- =========================================================

USE db_telito;

-- =========================================================
-- 1. TABLAS SPRING SESSION (Sesiones persistentes)
-- =========================================================

-- Tabla principal de sesiones
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para optimización
CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

-- Tabla de atributos de sesión
CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) 
        REFERENCES SPRING_SESSION(PRIMARY_ID) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2. TABLA PERSISTENT_LOGINS (Remember-Me Token)
-- =========================================================

CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice para búsqueda por usuario
CREATE INDEX IF NOT EXISTS idx_persistent_logins_username ON persistent_logins(username);

-- =========================================================
-- 3. VERIFICACIÓN DE TABLAS
-- =========================================================

SELECT '✅ TABLAS CREADAS EXITOSAMENTE' AS Status;

SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Filas',
    ROUND(DATA_LENGTH / 1024, 2) AS 'Tamaño (KB)',
    CREATE_TIME AS 'Creada',
    UPDATE_TIME AS 'Actualizada'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND (TABLE_NAME LIKE 'SPRING_SESSION%' OR TABLE_NAME = 'persistent_logins')
ORDER BY TABLE_NAME;

-- =========================================================
-- 4. QUERIES DE MONITOREO
-- =========================================================

-- Ver sesiones activas
SELECT '📊 SESIONES ACTIVAS' AS Info;
SELECT 
    SESSION_ID,
    PRINCIPAL_NAME AS Usuario,
    FROM_UNIXTIME(CREATION_TIME/1000) AS Creada,
    FROM_UNIXTIME(LAST_ACCESS_TIME/1000) AS 'Último Acceso',
    FROM_UNIXTIME(EXPIRY_TIME/1000) AS Expira,
    ROUND((EXPIRY_TIME - (UNIX_TIMESTAMP() * 1000)) / 1000 / 3600, 1) AS 'Horas Restantes',
    MAX_INACTIVE_INTERVAL AS 'Max Inactivo (seg)'
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000
ORDER BY LAST_ACCESS_TIME DESC;

-- Ver tokens Remember-Me activos
SELECT '🔐 TOKENS REMEMBER-ME' AS Info;
SELECT 
    username AS Usuario,
    series AS 'Serie Token',
    LEFT(token, 20) AS 'Token (parcial)',
    last_used AS 'Último Uso',
    TIMESTAMPDIFF(HOUR, last_used, NOW()) AS 'Horas desde uso',
    TIMESTAMPDIFF(DAY, last_used, NOW()) AS 'Días desde uso'
FROM persistent_logins
ORDER BY last_used DESC;

-- Estadísticas generales
SELECT '📈 ESTADÍSTICAS GENERALES' AS Info;
SELECT 
    (SELECT COUNT(*) FROM SPRING_SESSION WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000) AS 'Sesiones Activas',
    (SELECT COUNT(*) FROM SPRING_SESSION WHERE EXPIRY_TIME <= UNIX_TIMESTAMP() * 1000) AS 'Sesiones Expiradas',
    (SELECT COUNT(DISTINCT PRINCIPAL_NAME) FROM SPRING_SESSION WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000) AS 'Usuarios con Sesión',
    (SELECT COUNT(*) FROM persistent_logins) AS 'Tokens Remember-Me',
    (SELECT COUNT(DISTINCT username) FROM persistent_logins) AS 'Usuarios con Remember-Me';

-- =========================================================
-- 5. LIMPIEZA (Descomentar si necesitas limpiar)
-- =========================================================

-- Eliminar sesiones expiradas (ejecutar periódicamente)
-- DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME < UNIX_TIMESTAMP() * 1000;

-- Eliminar tokens remember-me viejos (más de 7 días sin uso)
-- DELETE FROM persistent_logins WHERE last_used < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- EMERGENCIA: Eliminar TODAS las sesiones y tokens (logout forzado)
-- DELETE FROM SPRING_SESSION_ATTRIBUTES;
-- DELETE FROM SPRING_SESSION;
-- DELETE FROM persistent_logins;

SELECT '✅ VERIFICACIÓN COMPLETADA - TODO LISTO' AS Status;
