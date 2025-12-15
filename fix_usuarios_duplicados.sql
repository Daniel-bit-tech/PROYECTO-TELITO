-- =====================================================
-- SCRIPT PARA IDENTIFICAR Y ELIMINAR USUARIOS DUPLICADOS
-- =====================================================
-- Problema: usuario.correo no tiene restricción UNIQUE
-- Esto causa NonUniqueResultException en findByCorreo()
-- =====================================================

USE db_telito;

-- DESACTIVAR SAFE UPDATE MODE TEMPORALMENTE
SET SQL_SAFE_UPDATES = 0;

-- 1. IDENTIFICAR TODOS LOS CORREOS DUPLICADOS
SELECT '=== CORREOS DUPLICADOS ===' AS info;
SELECT 
    correo,
    COUNT(*) as cantidad,
    GROUP_CONCAT(dni ORDER BY dni) as dnis,
    GROUP_CONCAT(nombre ORDER BY dni) as nombres,
    GROUP_CONCAT(fecha_registro ORDER BY dni) as fechas
FROM usuario 
GROUP BY correo 
HAVING COUNT(*) > 1;

-- 2. VER TODOS LOS DETALLES DE USUARIOS DUPLICADOS
SELECT '=== DETALLES DE USUARIOS DUPLICADOS ===' AS info;
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.apellido_materno,
    u.correo,
    u.fecha_registro,
    u.estado,
    u.idOrganizacion,
    (SELECT COUNT(*) FROM api WHERE idUsuario = u.dni) as apis_creadas,
    (SELECT COUNT(*) FROM proyecto WHERE dni_po_lider = u.dni) as proyectos_lider,
    (SELECT COUNT(*) FROM feedback WHERE idUsuario = u.dni) as feedbacks_dados
FROM usuario u
WHERE u.correo IN (
    SELECT correo 
    FROM usuario 
    GROUP BY correo 
    HAVING COUNT(*) > 1
)
ORDER BY u.correo, u.fecha_registro;

-- 3. ESTRATEGIA DE LIMPIEZA:
-- Mantendremos el usuario con fecha_registro MÁS ANTIGUA (el original)
-- Eliminaremos los duplicados más recientes

-- 3.1 Identificar qué usuarios se eliminarán
SELECT '=== USUARIOS QUE SE ELIMINARÁN ===' AS info;
SELECT 
    u1.dni,
    u1.nombre,
    u1.correo,
    u1.fecha_registro,
    'DUPLICADO - SE ELIMINARÁ' as accion
FROM usuario u1
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2
    WHERE u2.correo = u1.correo
    AND u2.fecha_registro < u1.fecha_registro
)
ORDER BY u1.correo, u1.fecha_registro;

-- 3.2 Identificar qué usuarios se mantendrán
SELECT '=== USUARIOS QUE SE MANTENDRÁN ===' AS info;
SELECT 
    u1.dni,
    u1.nombre,
    u1.correo,
    u1.fecha_registro,
    'ORIGINAL - SE MANTIENE' as accion
FROM usuario u1
WHERE u1.correo IN (
    SELECT correo 
    FROM usuario 
    GROUP BY correo 
    HAVING COUNT(*) > 1
)
AND NOT EXISTS (
    SELECT 1 
    FROM usuario u2
    WHERE u2.correo = u1.correo
    AND u2.fecha_registro < u1.fecha_registro
)
ORDER BY u1.correo;

-- 4. ACTUALIZAR REFERENCIAS ANTES DE ELIMINAR
-- Actualizar tablas que referencian los DNIs duplicados

-- 4.1 Actualizar api.idUsuario (apis creadas por usuario duplicado)
UPDATE api a
INNER JOIN usuario u_dup ON a.idUsuario = u_dup.dni
INNER JOIN usuario u_orig ON u_orig.correo = u_dup.correo 
    AND u_orig.fecha_registro < u_dup.fecha_registro
SET a.idUsuario = u_orig.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
);

-- 4.2 Actualizar proyecto.dni_po_lider
UPDATE proyecto p
INNER JOIN usuario u_dup ON p.dni_po_lider = u_dup.dni
INNER JOIN usuario u_orig ON u_orig.correo = u_dup.correo 
    AND u_orig.fecha_registro < u_dup.fecha_registro
SET p.dni_po_lider = u_orig.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
);

-- 4.3 Actualizar feedback.idUsuario
UPDATE feedback f
INNER JOIN usuario u_dup ON f.idUsuario = u_dup.dni
INNER JOIN usuario u_orig ON u_orig.correo = u_dup.correo 
    AND u_orig.fecha_registro < u_dup.fecha_registro
SET f.idUsuario = u_orig.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
);

-- 4.4 Actualizar usuario_has_equipo
UPDATE usuario_has_equipo ue
INNER JOIN usuario u_dup ON ue.Usuario_dni = u_dup.dni
INNER JOIN usuario u_orig ON u_orig.correo = u_dup.correo 
    AND u_orig.fecha_registro < u_dup.fecha_registro
SET ue.Usuario_dni = u_orig.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
)
AND NOT EXISTS (
    SELECT 1 FROM usuario_has_equipo ue2 
    WHERE ue2.Usuario_dni = u_orig.dni 
    AND ue2.Equipo_idEquipo = ue.Equipo_idEquipo
);

-- 4.5 Eliminar duplicados en usuario_has_equipo que no se pueden migrar
DELETE ue FROM usuario_has_equipo ue
INNER JOIN usuario u_dup ON ue.Usuario_dni = u_dup.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
);

-- 4.6 Actualizar usuario_has_rol
UPDATE usuario_has_rol ur
INNER JOIN usuario u_dup ON ur.usuario_dni = u_dup.dni
INNER JOIN usuario u_orig ON u_orig.correo = u_dup.correo 
    AND u_orig.fecha_registro < u_dup.fecha_registro
SET ur.usuario_dni = u_orig.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
)
AND NOT EXISTS (
    SELECT 1 FROM usuario_has_rol ur2 
    WHERE ur2.usuario_dni = u_orig.dni 
    AND ur2.rol_idRol = ur.rol_idRol
);

-- 4.7 Eliminar duplicados en usuario_has_rol que no se pueden migrar
DELETE ur FROM usuario_has_rol ur
INNER JOIN usuario u_dup ON ur.usuario_dni = u_dup.dni
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2 
    WHERE u2.correo = u_dup.correo 
    AND u2.fecha_registro < u_dup.fecha_registro
);

-- 5. ELIMINAR USUARIOS DUPLICADOS (manteniendo el más antiguo)
SELECT '=== ELIMINANDO USUARIOS DUPLICADOS ===' AS info;

DELETE u1 FROM usuario u1
WHERE EXISTS (
    SELECT 1 
    FROM usuario u2
    WHERE u2.correo = u1.correo
    AND u2.fecha_registro < u1.fecha_registro
);

-- 6. VERIFICAR QUE NO QUEDEN DUPLICADOS
SELECT '=== VERIFICACIÓN POST-LIMPIEZA ===' AS info;
SELECT 
    correo,
    COUNT(*) as cantidad,
    'PROBLEMA: Aún hay duplicados' as estado
FROM usuario 
GROUP BY correo 
HAVING COUNT(*) > 1
UNION ALL
SELECT 
    'TOTAL USUARIOS ÚNICOS' as correo,
    COUNT(DISTINCT correo) as cantidad,
    'OK' as estado
FROM usuario;

-- 7. AÑADIR RESTRICCIÓN UNIQUE PARA PREVENIR FUTUROS DUPLICADOS
SELECT '=== AÑADIENDO RESTRICCIÓN UNIQUE ===' AS info;

-- Primero verificar si ya existe el índice/constraint
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'db_telito' 
AND TABLE_NAME = 'usuario' 
AND COLUMN_NAME = 'correo';

-- Añadir restricción UNIQUE si no existe
ALTER TABLE usuario 
ADD UNIQUE INDEX idx_usuario_correo_unique (correo);

-- 8. VERIFICACIÓN FINAL
SELECT '=== VERIFICACIÓN FINAL ===' AS info;
SELECT 
    u.dni,
    u.nombre,
    u.apellido_paterno,
    u.apellido_materno,
    u.correo,
    u.fecha_registro,
    u.estado,
    (SELECT GROUP_CONCAT(r.nombre) FROM usuario_has_rol ur 
     JOIN rol r ON ur.rol_idRol = r.idRol 
     WHERE ur.usuario_dni = u.dni) as roles
FROM usuario u
WHERE u.correo = 'qa@telito.com';

SELECT '=== SCRIPT COMPLETADO ===' AS info;
SELECT 
    'Los usuarios duplicados han sido eliminados' as mensaje,
    'Se ha añadido restricción UNIQUE en usuario.correo' as restriccion,
    'Reinicia la aplicación para que los cambios surtan efecto' as accion_siguiente;

-- REACTIVAR SAFE UPDATE MODE
SET SQL_SAFE_UPDATES = 1;
