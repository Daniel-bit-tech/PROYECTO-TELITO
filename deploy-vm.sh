#!/bin/bash
# ============================================
# SCRIPT DE DESPLIEGUE AUTOMÁTICO - VM
# ============================================
# Uso: ./deploy-vm.sh
# ============================================

set -e  # Salir si hay algún error

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   DESPLIEGUE TELITO - PRODUCCIÓN${NC}"
echo -e "${BLUE}========================================${NC}"

# ============================================
# 1. COMPILAR APLICACIÓN
# ============================================
echo -e "\n${GREEN}[1/5] Compilando aplicación...${NC}"
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Compilación exitosa${NC}"
else
    echo -e "${RED}❌ Error en compilación${NC}"
    exit 1
fi

# ============================================
# 2. VERIFICAR JAR
# ============================================
echo -e "\n${GREEN}[2/5] Verificando JAR...${NC}"
JAR_FILE="target/TELITODEV-0.0.1-SNAPSHOT.jar"

if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    echo -e "${GREEN}✅ JAR encontrado: $JAR_SIZE${NC}"
else
    echo -e "${RED}❌ JAR no encontrado${NC}"
    exit 1
fi

# ============================================
# 3. CONFIGURAR VARIABLES DE ENTORNO
# ============================================
echo -e "\n${GREEN}[3/5] Configurando variables de entorno...${NC}"
echo -e "${YELLOW}Asegúrate de configurar estas variables:${NC}"
echo "  - DB_PASSWORD"
echo "  - SPRING_MAIL_USERNAME"
echo "  - SPRING_MAIL_PASSWORD"
echo "  - GOOGLE_OAUTH2_CLIENT_ID"
echo "  - GOOGLE_OAUTH2_CLIENT_SECRET"

# ============================================
# 4. COPIAR A VM (Opcional - descomentar)
# ============================================
# echo -e "\n${GREEN}[4/5] Copiando a VM...${NC}"
# VM_USER="usuario"
# VM_IP="TU_IP_VM"
# VM_PATH="/home/usuario/telito"
# 
# echo "Copiando JAR..."
# scp $JAR_FILE $VM_USER@$VM_IP:$VM_PATH/
# 
# echo "Copiando script SQL..."
# scp fix_ticket_auto_increment.sql $VM_USER@$VM_IP:$VM_PATH/
# 
# echo -e "${GREEN}✅ Archivos copiados a VM${NC}"

# ============================================
# 5. EJECUTAR EN VM (Opcional - descomentar)
# ============================================
# echo -e "\n${GREEN}[5/5] Ejecutando en VM...${NC}"
# 
# ssh $VM_USER@$VM_IP << 'ENDSSH'
# cd /home/usuario/telito
# 
# # Detener aplicación anterior
# if [ -f telito.pid ]; then
#     echo "Deteniendo aplicación anterior..."
#     kill $(cat telito.pid) || true
#     rm telito.pid
# fi
# 
# # Iniciar nueva versión
# echo "Iniciando nueva versión..."
# nohup java -jar -Dspring.profiles.active=production TELITODEV-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
# echo $! > telito.pid
# 
# echo "✅ Aplicación iniciada"
# ENDSSH

# ============================================
# FIN
# ============================================
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Proceso completado${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${YELLOW}Siguientes pasos:${NC}"
echo "1. Copiar JAR a la VM: scp $JAR_FILE usuario@VM:/path/"
echo "2. Configurar variables de entorno en la VM"
echo "3. Ejecutar: java -jar -Dspring.profiles.active=production TELITODEV-0.0.1-SNAPSHOT.jar"
echo ""
echo -e "${YELLOW}O usar systemd service (recomendado) - Ver DEPLOY_VM_GUIDE.md${NC}"
echo ""
