-- ==================================================
-- SCRIPT DE LIMPIEZA DE ROLES DUPLICADOS
-- ==================================================
-- Este script limpia los roles DEVELOPER duplicados

USE db_telito;

-- 1. Verificar el estado actual
SELECT 'ANTES DE LA LIMPIEZA' as estado;
SELECT idRol, nombre_rol, descripcion FROM rol WHERE nombre_rol = 'DEVELOPER';

-- 2. Verificar si hay usuarios asignados a estos roles duplicados
SELECT 
    r.idRol, 
    r.nombre_rol, 
    COUNT(u.dni) as usuarios_asignados
FROM rol r 
LEFT JOIN usuario u ON r.idRol = u.idRol 
WHERE r.nombre_rol = 'DEVELOPER' 
GROUP BY r.idRol, r.nombre_rol;

-- 3. Reasignar usuarios de roles duplicados al rol principal (ID 5)
UPDATE usuario 
SET idRol = 5 
WHERE idRol IN (6, 7, 8) 
  AND idRol IN (SELECT idRol FROM rol WHERE nombre_rol = 'DEVELOPER');

-- 4. Eliminar los roles duplicados (mantener solo el ID 5)
DELETE FROM rol 
WHERE nombre_rol = 'DEVELOPER' 
  AND idRol > 5;

-- 5. Verificar el resultado final
SELECT 'DESPUÉS DE LA LIMPIEZA' as estado;
SELECT idRol, nombre_rol, descripcion FROM rol WHERE nombre_rol = 'DEVELOPER';

-- 6. Verificar que todos los usuarios estén correctamente asignados
SELECT 
    r.idRol, 
    r.nombre_rol, 
    COUNT(u.dni) as usuarios_asignados
FROM rol r 
LEFT JOIN usuario u ON r.idRol = u.idRol 
WHERE r.nombre_rol = 'DEVELOPER' 
GROUP BY r.idRol, r.nombre_rol;

-- 7. Mostrar todos los roles para verificar integridad
SELECT idRol, nombre_rol, descripcion FROM rol ORDER BY idRol;

SELECT '✅ Limpieza completada. Ahora solo debe haber 1 rol DEVELOPER.' as resultado_final;