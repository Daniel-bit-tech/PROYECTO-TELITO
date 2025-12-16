-- =========================================================
-- SCRIPT: Eliminar Organizaciones sin Product Owner
-- =========================================================
-- Este script identifica y elimina organizaciones que no tienen
-- ningún usuario con rol de Product Owner activo
-- 
-- IMPORTANTE: Ejecuta primero las consultas de verificación
-- antes de ejecutar las eliminaciones
-- =========================================================

USE db_telito;

-- =========================================================
-- PASO 1: VERIFICACIÓN - Identificar organizaciones sin PO
-- =========================================================

SELECT 
    '=== ORGANIZACIONES SIN PRODUCT OWNER ACTIVO ===' AS '';

-- Consulta principal: Organizaciones sin PO
SELECT 
    o.idOrganizacion,
    o.nombre AS nombre_organizacion,
    o.dominio_correo,
    o.fecha_creacion,
    COUNT(u.dni) AS total_usuarios,
    SUM(CASE WHEN u.estado = TRUE THEN 1 ELSE 0 END) AS usuarios_activos,
    SUM(CASE WHEN r.nombre_rol = 'PO' AND u.estado = TRUE THEN 1 ELSE 0 END) AS pos_activos
FROM organizacion o
LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
GROUP BY o.idOrganizacion, o.nombre, o.dominio_correo, o.fecha_creacion
HAVING pos_activos = 0
ORDER BY o.fecha_creacion DESC;

-- =========================================================
-- PASO 2: DETALLES - Ver usuarios de organizaciones sin PO
-- =========================================================

SELECT 
    '=== USUARIOS EN ORGANIZACIONES SIN PO ===' AS '';

SELECT 
    o.nombre AS organizacion,
    u.dni,
    u.nombre,
    u.apellido_paterno,
    r.nombre_rol AS rol,
    u.estado,
    u.fecha_registro
FROM organizacion o
LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
WHERE o.idOrganizacion IN (
    SELECT o2.idOrganizacion
    FROM organizacion o2
    LEFT JOIN usuario u2 ON o2.idOrganizacion = u2.idOrganizacion
    LEFT JOIN rol r2 ON u2.idRol = r2.idRol
    GROUP BY o2.idOrganizacion
    HAVING SUM(CASE WHEN r2.nombre_rol = 'PO' AND u2.estado = TRUE THEN 1 ELSE 0 END) = 0
)
ORDER BY o.nombre, r.nombre_rol;

-- =========================================================
-- PASO 3: VERIFICAR TOKENS PENDIENTES
-- =========================================================

SELECT 
    '=== TOKENS PENDIENTES PARA ORGANIZACIONES SIN PO ===' AS '';

SELECT 
    o.nombre AS organizacion,
    t.dni_usuario,
    t.email,
    t.fecha_creacion,
    t.fecha_expiracion,
    t.usado,
    CASE 
        WHEN t.fecha_expiracion < NOW() THEN 'EXPIRADO'
        WHEN t.usado = TRUE THEN 'USADO'
        ELSE 'PENDIENTE'
    END AS estado_token
FROM tokens_confirmacion t
INNER JOIN organizacion o ON t.id_organizacion_temporal = o.idOrganizacion
WHERE o.idOrganizacion IN (
    SELECT o2.idOrganizacion
    FROM organizacion o2
    LEFT JOIN usuario u2 ON o2.idOrganizacion = u2.idOrganizacion
    LEFT JOIN rol r2 ON u2.idRol = r2.idRol
    GROUP BY o2.idOrganizacion
    HAVING SUM(CASE WHEN r2.nombre_rol = 'PO' AND u2.estado = TRUE THEN 1 ELSE 0 END) = 0
)
ORDER BY t.fecha_creacion DESC;

-- =========================================================
-- PASO 4: RESUMEN DE IMPACTO
-- =========================================================

SELECT 
    '=== RESUMEN DE IMPACTO ===' AS '';

SELECT 
    COUNT(DISTINCT o.idOrganizacion) AS organizaciones_a_eliminar,
    COUNT(DISTINCT u.dni) AS usuarios_afectados,
    COUNT(DISTINCT t.id) AS tokens_a_eliminar
FROM organizacion o
LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
LEFT JOIN tokens_confirmacion t ON o.idOrganizacion = t.id_organizacion_temporal
WHERE o.idOrganizacion IN (
    SELECT o2.idOrganizacion
    FROM organizacion o2
    LEFT JOIN usuario u2 ON o2.idOrganizacion = u2.idOrganizacion
    LEFT JOIN rol r2 ON u2.idRol = r2.idRol
    GROUP BY o2.idOrganizacion
    HAVING SUM(CASE WHEN r2.nombre_rol = 'PO' AND u2.estado = TRUE THEN 1 ELSE 0 END) = 0
);

-- =========================================================
-- ⚠️ PASO 5: ELIMINACIÓN (CUIDADO - NO REVERSIBLE)
-- =========================================================
-- DESCOMENTA LAS SIGUIENTES LÍNEAS SOLO DESPUÉS DE VERIFICAR
-- QUE LOS RESULTADOS ANTERIORES SON CORRECTOS
-- =========================================================

/*
-- Desactivar modo seguro temporalmente
SET SQL_SAFE_UPDATES = 0;

-- Iniciar transacción para poder revertir si algo sale mal
START TRANSACTION;

-- 1. Primero eliminar tokens asociados a estas organizaciones
DELETE FROM tokens_confirmacion 
WHERE id_organizacion_temporal IN (
    SELECT o.idOrganizacion
    FROM organizacion o
    LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
    LEFT JOIN rol r ON u.idRol = r.idRol
    GROUP BY o.idOrganizacion
    HAVING SUM(CASE WHEN r.nombre_rol = 'PO' AND u.estado = TRUE THEN 1 ELSE 0 END) = 0
);

SELECT CONCAT('✅ Tokens eliminados: ', ROW_COUNT()) AS resultado;

-- 2. Luego eliminar usuarios de estas organizaciones
-- NOTA: Esto puede fallar si hay foreign keys. En ese caso, 
-- considera primero desactivar usuarios o eliminar datos relacionados
DELETE FROM usuario 
WHERE idOrganizacion IN (
    -- Subconsulta envuelta para evitar "Error 1093: can't specify target table"
    SELECT idOrganizacion FROM (
        SELECT o.idOrganizacion
        FROM organizacion o
        LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
        LEFT JOIN rol r ON u.idRol = r.idRol
        GROUP BY o.idOrganizacion
        HAVING SUM(CASE WHEN r.nombre_rol = 'PO' AND u.estado = TRUE THEN 1 ELSE 0 END) = 0
    ) AS orgs_sin_po
);

SELECT CONCAT('✅ Usuarios eliminados: ', ROW_COUNT()) AS resultado;

-- 3. Finalmente eliminar las organizaciones
DELETE FROM organizacion 
WHERE idOrganizacion IN (
    SELECT o2.idOrganizacion
    FROM (
        SELECT o.idOrganizacion
        FROM organizacion o
        LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
        LEFT JOIN rol r ON u.idRol = r.idRol
        GROUP BY o.idOrganizacion
        HAVING SUM(CASE WHEN r.nombre_rol = 'PO' AND u.estado = TRUE THEN 1 ELSE 0 END) = 0
    ) AS o2
);

SELECT CONCAT('✅ Organizaciones eliminadas: ', ROW_COUNT()) AS resultado;

-- Si todo está bien, confirmar los cambios
COMMIT;

-- Reactivar modo seguro
SET SQL_SAFE_UPDATES = 1;

SELECT '✅ LIMPIEZA COMPLETADA EXITOSAMENTE' AS resultado_final;
*/

-- =========================================================
-- ALTERNATIVA: Eliminación Manual Segura
-- =========================================================
-- Si prefieres eliminar organizaciones específicas manualmente,
-- usa esta plantilla:
-- =========================================================

/*
-- Ejemplo para eliminar una organización específica (reemplaza el ID)
SET @org_id = 999; -- REEMPLAZA CON EL ID REAL

START TRANSACTION;

-- Eliminar tokens
DELETE FROM tokens_confirmacion WHERE id_organizacion_temporal = @org_id;

-- Eliminar usuarios (cuidado con foreign keys)
DELETE FROM usuario WHERE idOrganizacion = @org_id;

-- Eliminar organización
DELETE FROM organizacion WHERE idOrganizacion = @org_id;

-- Verificar antes de confirmar
SELECT 'Verificar que todo esté correcto, luego ejecuta COMMIT;' AS aviso;

-- Si todo está bien:
-- COMMIT;

-- Si algo salió mal:
-- ROLLBACK;
*/

-- =========================================================
-- VERIFICACIÓN POST-ELIMINACIÓN
-- =========================================================
-- Ejecuta esto después de la eliminación para confirmar
-- =========================================================

/*
SELECT 
    '=== VERIFICACIÓN POST-ELIMINACIÓN ===' AS '';

-- Ver organizaciones restantes
SELECT 
    o.idOrganizacion,
    o.nombre,
    COUNT(u.dni) AS total_usuarios,
    SUM(CASE WHEN r.nombre_rol = 'PO' THEN 1 ELSE 0 END) AS pos
FROM organizacion o
LEFT JOIN usuario u ON o.idOrganizacion = u.idOrganizacion
LEFT JOIN rol r ON u.idRol = r.idRol
GROUP BY o.idOrganizacion, o.nombre
ORDER BY o.nombre;
*/

-- =========================================================
-- NOTAS IMPORTANTES
-- =========================================================
/*
1. SIEMPRE ejecuta primero los pasos 1-4 para verificar qué se eliminará

2. Las eliminaciones pueden FALLAR si hay foreign keys en otras tablas
   que referencian a usuarios u organizaciones. Tablas que pueden tener FKs:
   - equipo (idOrganizacion)
   - proyecto (idOrganizacion)
   - api (puede tener relación indirecta)
   - sol_acceso_org
   - sol_acceso_equipo
   - actividad_admin
   - feedback
   - notificacion
   - etc.

3. ALTERNATIVAS si falla la eliminación:
   a) Desactivar usuarios en lugar de eliminarlos:
      UPDATE usuario SET estado = FALSE WHERE idOrganizacion = X;
   
   b) Eliminar primero datos relacionados de otras tablas:
      - Equipos
      - Proyectos
      - APIs
      - Solicitudes de acceso
      - Feedbacks, notificaciones, etc.
   
   c) Usar CASCADE en foreign keys (requiere modificar esquema DB)

4. RESPALDO: Antes de ejecutar eliminaciones, haz un backup:
   mysqldump -u usuario -p db_telito > backup_antes_limpieza.sql

5. La función automática de limpieza en `eliminarToken()` ya maneja
   organizaciones nuevas sin usuarios. Este script es para limpiar
   organizaciones más antiguas o casos especiales.
*/
