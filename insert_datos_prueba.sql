-- ================================================
-- SCRIPT: Insertar datos de prueba
-- FECHA: 2025-12-11
-- PROPÓSITO: Crear APIs, credenciales y reportes de ejemplo
-- ================================================

USE db_telito;

-- ================================================
-- 1. INSERTAR APIs DE PRUEBA
-- ================================================

-- API 1: Servicio de Autenticación
INSERT INTO api (nombre, descripcion, endpointUrl, idEquipo, idDominio, idTag, idEstado, idUsuario, fecha_creacion)
VALUES 
('Auth Service API', 
 'API REST para autenticación y autorización de usuarios con JWT',
 'https://api.telito.com/auth/v1',
 (SELECT idEquipo FROM equipo LIMIT 1),
 (SELECT idDominio FROM dominio LIMIT 1),
 (SELECT idTag FROM tag LIMIT 1),
 (SELECT idEstado FROM estadoapi LIMIT 1),
 (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1),
 NOW());

-- API 2: Gestión de Usuarios
INSERT INTO api (nombre, descripcion, endpointUrl, idEquipo, idDominio, idTag, idEstado, idUsuario, fecha_creacion)
VALUES 
('User Management API', 
 'API para CRUD de usuarios y gestión de perfiles',
 'https://api.telito.com/users/v1',
 (SELECT idEquipo FROM equipo LIMIT 1),
 (SELECT idDominio FROM dominio LIMIT 1),
 (SELECT idTag FROM tag LIMIT 1),
 (SELECT idEstado FROM estadoapi LIMIT 1),
 (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1),
 NOW());

-- API 3: Servicio de Notificaciones
INSERT INTO api (nombre, descripcion, endpointUrl, idEquipo, idDominio, idTag, idEstado, idUsuario, fecha_creacion)
VALUES 
('Notification Service', 
 'Servicio de mensajería en tiempo real con WebSockets',
 'wss://api.telito.com/notifications/v1',
 (SELECT idEquipo FROM equipo LIMIT 1),
 (SELECT idDominio FROM dominio LIMIT 1),
 (SELECT idTag FROM tag LIMIT 1),
 (SELECT idEstado FROM estadoapi LIMIT 1),
 (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1),
 NOW());

-- API 4: Servicio de Pagos
INSERT INTO api (nombre, descripcion, endpointUrl, idEquipo, idDominio, idTag, idEstado, idUsuario, fecha_creacion)
VALUES 
('Payment Gateway API', 
 'API para procesamiento de pagos y transacciones',
 'https://api.telito.com/payments/v2',
 (SELECT idEquipo FROM equipo LIMIT 1),
 (SELECT idDominio FROM dominio LIMIT 1),
 (SELECT idTag FROM tag LIMIT 1),
 (SELECT idEstado FROM estadoapi LIMIT 1),
 (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1),
 NOW());

-- API 5: Servicio de Analytics
INSERT INTO api (nombre, descripcion, endpointUrl, idEquipo, idDominio, idTag, idEstado, idUsuario, fecha_creacion)
VALUES 
('Analytics API', 
 'API GraphQL para consultas de métricas y estadísticas',
 'https://api.telito.com/analytics/graphql',
 (SELECT idEquipo FROM equipo LIMIT 1),
 (SELECT idDominio FROM dominio LIMIT 1),
 (SELECT idTag FROM tag LIMIT 1),
 (SELECT idEstado FROM estadoapi LIMIT 1),
 (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1),
 NOW());

-- ================================================
-- 2. ASIGNAR APIs A ENTORNOS
-- ================================================

-- Obtener IDs de las APIs recién creadas
SET @authApi = (SELECT idApi FROM api WHERE nombre = 'Auth Service API' LIMIT 1);
SET @userApi = (SELECT idApi FROM api WHERE nombre = 'User Management API' LIMIT 1);
SET @notifApi = (SELECT idApi FROM api WHERE nombre = 'Notification Service' LIMIT 1);
SET @paymentApi = (SELECT idApi FROM api WHERE nombre = 'Payment Gateway API' LIMIT 1);
SET @analyticsApi = (SELECT idApi FROM api WHERE nombre = 'Analytics API' LIMIT 1);

-- Obtener IDs de entornos (usar los primeros disponibles)
SET @env1 = (SELECT MIN(idEntorno) FROM entorno);
SET @env2 = (SELECT MIN(idEntorno) FROM entorno WHERE idEntorno > @env1);
SET @env3 = (SELECT MIN(idEntorno) FROM entorno WHERE idEntorno > @env2);

-- Asignar todas las APIs a los entornos disponibles
INSERT IGNORE INTO api_has_entorno (idAPI, idEntorno) 
SELECT idApi, idEntorno 
FROM (SELECT @authApi as idApi UNION SELECT @userApi UNION SELECT @notifApi UNION SELECT @paymentApi UNION SELECT @analyticsApi) apis
CROSS JOIN (SELECT idEntorno FROM entorno LIMIT 3) entornos;

-- ================================================
-- 3. ASIGNAR APIs A PROYECTOS
-- ================================================

-- Obtener un proyecto existente
SET @proyecto = (SELECT idProyecto FROM proyecto LIMIT 1);

-- Asignar APIs al proyecto (usando versión 1 y primer entorno por defecto)
INSERT IGNORE INTO proyecto_has_api (idProyecto, idAPI, idVersion, idEntorno, fecha_asociacion) VALUES
(@proyecto, @authApi, 1, (SELECT idEntorno FROM entorno LIMIT 1), NOW()),
(@proyecto, @userApi, 1, (SELECT idEntorno FROM entorno LIMIT 1), NOW()),
(@proyecto, @notifApi, 1, (SELECT idEntorno FROM entorno LIMIT 1), NOW()),
(@proyecto, @paymentApi, 1, (SELECT idEntorno FROM entorno LIMIT 1), NOW()),
(@proyecto, @analyticsApi, 1, (SELECT idEntorno FROM entorno LIMIT 1), NOW());

-- ================================================
-- 4. CREAR CREDENCIALES PARA USUARIOS
-- ================================================

-- Obtener usuario Developer
SET @devUser = (SELECT dni FROM usuario WHERE idRol = 3 LIMIT 1);

-- Obtener usuario QA
SET @qaUser = (SELECT dni FROM usuario WHERE idRol = 4 LIMIT 1);

-- Crear credenciales para el Developer
INSERT INTO credencialapi (idUsuario, idAPI, api_key, fecha_creacion, estado) VALUES
(@devUser, @authApi, UUID(), NOW(), 1),
(@devUser, @userApi, UUID(), NOW(), 1),
(@devUser, @notifApi, UUID(), NOW(), 1);

-- Crear credenciales para el QA
INSERT INTO credencialapi (idUsuario, idAPI, api_key, fecha_creacion, estado) VALUES
(@qaUser, @authApi, UUID(), NOW(), 1),
(@qaUser, @userApi, UUID(), NOW(), 1),
(@qaUser, @paymentApi, UUID(), NOW(), 1);

-- ================================================
-- 5. CREAR REPORTES DE QA
-- ================================================

-- Reporte 1: Auth API
INSERT INTO reporte (idApi, titulo, descripcion, idUsuarioQA, dniPoLider, fecha_registro, idEstadoReporte)
VALUES 
(@authApi,
 'Validación de Tokens JWT',
 'Se probaron los endpoints de generación y validación de tokens. Todos los casos de prueba pasaron correctamente.',
 @qaUser,
 (SELECT dni FROM usuario WHERE idRol = 2 LIMIT 1),
 NOW(),
 (SELECT idEstadoReporte FROM estadoreporte LIMIT 1));

-- Reporte 2: User API
INSERT INTO reporte (idApi, titulo, descripcion, idUsuarioQA, dniPoLider, fecha_registro, idEstadoReporte)
VALUES 
(@userApi,
 'Pruebas CRUD de Usuarios',
 'Se validaron operaciones de creación, lectura, actualización y eliminación de usuarios. Encontrados 2 issues menores.',
 @qaUser,
 (SELECT dni FROM usuario WHERE idRol = 2 LIMIT 1),
 NOW(),
 (SELECT idEstadoReporte FROM estadoreporte LIMIT 1));

-- Reporte 3: Payment API
INSERT INTO reporte (idApi, titulo, descripcion, idUsuarioQA, dniPoLider, fecha_registro, idEstadoReporte)
VALUES 
(@paymentApi,
 'Testing de Transacciones',
 'Pruebas de procesamiento de pagos con diferentes métodos. Requiere ajustes en manejo de errores.',
 @qaUser,
 (SELECT dni FROM usuario WHERE idRol = 2 LIMIT 1),
 NOW(),
 (SELECT idEstadoReporte FROM estadoreporte LIMIT 1));

-- ================================================
-- 6. CREAR ISSUES PARA LOS REPORTES
-- ================================================

-- Issues para el reporte de User API
SET @userReporte = (SELECT idReporte FROM reporte WHERE titulo = 'Pruebas CRUD de Usuarios' LIMIT 1);

INSERT INTO issue (idReporte, titulo, descripcion, fecha_registro, idPrioridad, idEstadoIssue)
VALUES 
(@userReporte,
 'Validación de email incorrecta',
 'El endpoint de creación de usuario no valida correctamente el formato del email',
 NOW(),
 (SELECT idPrioridad FROM prioridad LIMIT 1),
 (SELECT idEstadoIssue FROM estadoissue LIMIT 1));

INSERT INTO issue (idReporte, titulo, descripcion, fecha_registro, idPrioridad, idEstadoIssue)
VALUES 
(@userReporte,
 'Paginación no funciona correctamente',
 'Los parámetros de paginación page y size no se aplican en el endpoint GET /users',
 NOW(),
 (SELECT idPrioridad FROM prioridad LIMIT 1),
 (SELECT idEstadoIssue FROM estadoissue LIMIT 1));

-- Issues para el reporte de Payment API
SET @paymentReporte = (SELECT idReporte FROM reporte WHERE titulo = 'Testing de Transacciones' LIMIT 1);

INSERT INTO issue (idReporte, titulo, descripcion, fecha_registro, idPrioridad, idEstadoIssue)
VALUES 
(@paymentReporte,
 'Timeout en transacciones grandes',
 'Las transacciones mayores a $10000 generan timeout en el servidor',
 NOW(),
 (SELECT idPrioridad FROM prioridad LIMIT 1),
 (SELECT idEstadoIssue FROM estadoissue LIMIT 1));

-- ================================================
-- 7. VERIFICACIÓN FINAL
-- ================================================

-- Verificar APIs creadas
SELECT 'APIs Creadas' as tipo, COUNT(*) as cantidad FROM api 
WHERE nombre IN ('Auth Service API', 'User Management API', 'Notification Service', 'Payment Gateway API', 'Analytics API');

-- Verificar credenciales
SELECT 'Credenciales Creadas' as tipo, COUNT(*) as cantidad FROM credencialapi 
WHERE idAPI IN (@authApi, @userApi, @notifApi, @paymentApi, @analyticsApi);

-- Verificar reportes
SELECT 'Reportes Creados' as tipo, COUNT(*) as cantidad FROM reporte 
WHERE idApi IN (@authApi, @userApi, @paymentApi);

-- Verificar issues
SELECT 'Issues Creados' as tipo, COUNT(*) as cantidad FROM issue 
WHERE idReporte IN (@userReporte, @paymentReporte);

-- ================================================
-- FIN DEL SCRIPT
-- ================================================
