-- Verificar las APIs que el chatbot debería mostrar
USE db_telito;

-- Ver todas las APIs con su estado
SELECT 
    idAPI,
    nombre,
    idEstado,
    CASE 
        WHEN idEstado = 2 THEN '❌ Excluida (estado 2)'
        ELSE '✅ Incluida'
    END as 'Disponible para Chatbot'
FROM api
ORDER BY idAPI;

-- Ver cuántas APIs deberían aparecer
SELECT 
    COUNT(*) as 'Total APIs disponibles para chatbot'
FROM api 
WHERE idEstado != 2;
