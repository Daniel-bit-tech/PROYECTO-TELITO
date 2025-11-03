-- ============================================
-- FIX: Agregar AUTO_INCREMENT a idTicket
-- ============================================
-- Problema: Field 'idTicket' doesn't have a default value
-- Solución: Configurar idTicket como AUTO_INCREMENT
-- ============================================

USE db_telito;

-- PASO 1: Eliminar la FK que bloquea el cambio
ALTER TABLE chatbot 
DROP FOREIGN KEY fk_ChatBOT_Ticket1;

-- PASO 2: Modificar la columna idTicket para que sea AUTO_INCREMENT
ALTER TABLE ticket 
MODIFY COLUMN idTicket INT NOT NULL AUTO_INCREMENT;

-- PASO 3: Recrear la FK
ALTER TABLE chatbot 
ADD CONSTRAINT fk_ChatBOT_Ticket1 
FOREIGN KEY (idTicket) 
REFERENCES ticket(idTicket) 
ON DELETE NO ACTION 
ON UPDATE NO ACTION;

-- Verificar que se aplicó correctamente
SHOW CREATE TABLE ticket;

-- Mensaje de confirmación
SELECT 'AUTO_INCREMENT configurado correctamente en ticket.idTicket' AS resultado;
