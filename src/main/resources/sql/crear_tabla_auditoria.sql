-- Script para crear la tabla de auditoría de actividades administrativas
-- Ejecutar este script en MySQL para habilitar el sistema de auditoría

-- Crear la tabla actividad_admin
CREATE TABLE IF NOT EXISTS actividad_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_dni VARCHAR(8) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_actividad TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    detalles JSON,
    
    -- Índices para mejorar rendimiento
    INDEX idx_usuario_dni (usuario_dni),
    INDEX idx_accion (accion),
    INDEX idx_fecha_actividad (fecha_actividad),
    
    -- Clave foránea hacia la tabla usuario
    CONSTRAINT fk_actividad_admin_usuario 
        FOREIGN KEY (usuario_dni) 
        REFERENCES usuario(dni) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Comentarios de la tabla y columnas
ALTER TABLE actividad_admin 
    COMMENT = 'Registro de auditoría para todas las actividades administrativas';

ALTER TABLE actividad_admin 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'ID único de la actividad',
    MODIFY COLUMN usuario_dni VARCHAR(8) NOT NULL COMMENT 'DNI del administrador que realizó la acción',
    MODIFY COLUMN accion VARCHAR(50) NOT NULL COMMENT 'Tipo de acción realizada (CREAR_USUARIO, EDITAR_USUARIO, etc.)',
    MODIFY COLUMN descripcion TEXT NOT NULL COMMENT 'Descripción detallada de la actividad',
    MODIFY COLUMN fecha_actividad TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora cuando se realizó la actividad',
    MODIFY COLUMN ip_address VARCHAR(45) COMMENT 'Dirección IP desde donde se realizó la acción',
    MODIFY COLUMN detalles JSON COMMENT 'Información adicional en formato JSON';

-- Insertar algunas actividades de ejemplo (opcional)
-- Reemplazar 'TU_DNI_ADMIN' con el DNI de un administrador real
/*
INSERT INTO actividad_admin (usuario_dni, accion, descripcion, detalles) VALUES
('TU_DNI_ADMIN', 'VIEW_DASHBOARD', 'Accedió al panel de administración', '{"seccion": "dashboard"}'),
('TU_DNI_ADMIN', 'CREAR_USUARIO', 'Creó nuevo usuario de prueba', '{"usuario_creado": "12345678", "rol": "USER"}'),
('TU_DNI_ADMIN', 'EDITAR_USUARIO', 'Editó información de usuario', '{"usuario_editado": "12345678", "cambios": "datos personales"}');
*/

-- Verificar la creación de la tabla
SELECT 'Tabla actividad_admin creada exitosamente' as resultado;

-- Mostrar la estructura de la tabla
DESCRIBE actividad_admin;