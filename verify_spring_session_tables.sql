-- =========================================================
-- VERIFICACIÓN Y CREACIÓN DE TABLAS SPRING SESSION
-- =========================================================
-- Este script verifica y crea las tablas necesarias para
-- la persistencia de sesiones con Spring Session JDBC
-- =========================================================

USE db_telito;

-- 1. Verificar si las tablas existen
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND TABLE_NAME LIKE 'SPRING_SESSION%';

-- 2. Crear las tablas si no existen
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

-- Índice para búsqueda por SESSION_ID
CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);

-- Índice para limpieza de sesiones expiradas
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);

-- Índice para búsqueda por usuario
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

-- 3. Verificar las tablas después de la creación
SELECT 'Tablas Spring Session verificadas:' AS Status;
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS 'Tamaño (MB)'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_telito' 
  AND TABLE_NAME LIKE 'SPRING_SESSION%';

-- 4. Ver sesiones activas (si las hay)
SELECT 'Sesiones activas:' AS Info;
SELECT 
    SESSION_ID,
    PRINCIPAL_NAME,
    FROM_UNIXTIME(CREATION_TIME/1000) AS created_at,
    FROM_UNIXTIME(LAST_ACCESS_TIME/1000) AS last_access,
    FROM_UNIXTIME(EXPIRY_TIME/1000) AS expires_at,
    MAX_INACTIVE_INTERVAL AS max_inactive_seconds
FROM SPRING_SESSION
WHERE EXPIRY_TIME > UNIX_TIMESTAMP() * 1000
ORDER BY LAST_ACCESS_TIME DESC;

-- 5. Limpiar sesiones expiradas manualmente (opcional)
-- DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME < UNIX_TIMESTAMP() * 1000;

SELECT '✅ Verificación completada' AS Status;
