-- Verificar que la contraseña se actualizó correctamente
SELECT 
    dni,
    nombre,
    correo,
    correo_corporativo,
    estado,
    LENGTH(contrasena) as longitud_hash,
    SUBSTRING(contrasena, 1, 30) as inicio_hash,
    contrasena
FROM usuario
WHERE dni = '73415980';
