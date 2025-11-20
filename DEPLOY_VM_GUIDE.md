# ============================================
# GUÍA DE DESPLIEGUE EN VM
# ============================================

## 📋 PASOS PREVIOS

### 1. Requisitos en la VM
- ✅ Java 17+ instalado
- ✅ MySQL 8.0 instalado y configurado
- ✅ Puerto 8080 abierto en el firewall
- ✅ Base de datos `db_telito` creada

---

## 🔧 CONFIGURACIÓN

### 2. Variables de Entorno en la VM

Crea un archivo `.env` o configura las variables de sistema:

```bash
# Base de datos
export DB_PASSWORD="tu_password_mysql"

# Email (opcional)
export SPRING_MAIL_USERNAME="tu_email@gmail.com"
export SPRING_MAIL_PASSWORD="tu_app_password_gmail"

# OAuth2 Google (si usas login con Google)
export GOOGLE_OAUTH2_CLIENT_ID="tu_client_id"
export GOOGLE_OAUTH2_CLIENT_SECRET="tu_client_secret"

# AWS S3 (si usas S3)
export AWS_ACCESS_KEY_ID="tu_access_key"
export AWS_SECRET_ACCESS_KEY="tu_secret_key"
```

### 3. Editar application-production.properties

Abre el archivo `src/main/resources/application-production.properties` y cambia:

```properties
# Línea 13: Password de MySQL
spring.datasource.password=TU_PASSWORD_MYSQL_VM

# Línea 34: IP pública de tu VM
app.url=http://TU_IP_VM:8080

# Línea 63: Redirect URI de OAuth2
spring.security.oauth2.client.registration.google.redirect-uri=http://TU_IP_VM:8080/login/oauth2/code/google
```

**Ejemplo con IP 34.212.200.2:**
```properties
app.url=http://34.212.200.2:8080
spring.security.oauth2.client.registration.google.redirect-uri=http://34.212.200.2:8080/login/oauth2/code/google
```

---

## 🏗️ COMPILAR Y EMPAQUETAR

### 4. Compilar el proyecto

En tu máquina local (Windows):

```powershell
# Compilar sin ejecutar tests
mvnw.cmd clean package -DskipTests

# El JAR se generará en: target/TELITODEV-0.0.1-SNAPSHOT.jar
```

---

## 🚀 DESPLEGAR EN LA VM

### 5. Copiar archivos a la VM

```bash
# Copiar el JAR
scp target/TELITODEV-0.0.1-SNAPSHOT.jar usuario@TU_IP_VM:/home/usuario/telito/

# Copiar el script SQL de AUTO_INCREMENT (si no lo ejecutaste)
scp fix_ticket_auto_increment.sql usuario@TU_IP_VM:/home/usuario/telito/
```

### 6. Configurar la base de datos en la VM

Conecta por SSH a la VM y ejecuta:

```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar el script
USE db_telito;
source /home/usuario/telito/fix_ticket_auto_increment.sql;
exit;
```

### 7. Ejecutar la aplicación

**Opción A: Ejecución directa (para pruebas)**
```bash
cd /home/usuario/telito
java -jar -Dspring.profiles.active=production TELITODEV-0.0.1-SNAPSHOT.jar
```

**Opción B: Como servicio systemd (recomendado)**

Crea el archivo `/etc/systemd/system/telito.service`:

```ini
[Unit]
Description=Telito Dev Application
After=mysql.service

[Service]
Type=simple
User=usuario
WorkingDirectory=/home/usuario/telito
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=production /home/usuario/telito/TELITODEV-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

# Variables de entorno
Environment="DB_PASSWORD=tu_password"
Environment="SPRING_MAIL_USERNAME=tu_email@gmail.com"
Environment="SPRING_MAIL_PASSWORD=tu_app_password"
Environment="GOOGLE_OAUTH2_CLIENT_ID=tu_client_id"
Environment="GOOGLE_OAUTH2_CLIENT_SECRET=tu_client_secret"

[Install]
WantedBy=multi-user.target
```

Luego:
```bash
# Recargar systemd
sudo systemctl daemon-reload

# Iniciar el servicio
sudo systemctl start telito

# Ver el estado
sudo systemctl status telito

# Ver logs
sudo journalctl -u telito -f

# Habilitar inicio automático
sudo systemctl enable telito
```

---

## 🔍 VERIFICACIÓN

### 8. Comprobar que funciona

```bash
# Ver logs de la aplicación
tail -f /var/log/telitodev/application.log

# Comprobar que el puerto 8080 está escuchando
netstat -tulpn | grep 8080

# Probar desde un navegador
# http://TU_IP_VM:8080
```

---

## 🚨 TROUBLESHOOTING

### Error: "Cannot connect to MySQL"
- Verifica que MySQL esté corriendo: `sudo systemctl status mysql`
- Comprueba el password en `application-production.properties`
- Verifica que MySQL permita conexiones locales

### Error: "Port 8080 already in use"
- Encuentra el proceso: `sudo lsof -i :8080`
- Mata el proceso: `sudo kill -9 PID`

### Chatbot no funciona
- El chatbot funciona localmente en la misma aplicación
- No necesitas desplegar nada adicional
- URL: `http://localhost:8080/dev/soporte/chat`

### OAuth2 no funciona
- Actualiza la redirect URI en Google Cloud Console
- Debe ser: `http://TU_IP_VM:8080/login/oauth2/code/google`
- Asegúrate de que las variables de entorno estén configuradas

---

## 📝 NOTAS IMPORTANTES

### Chatbot Local
El chatbot **NO necesita AWS** porque:
- Usa lógica local con regex patterns
- Está integrado en la misma aplicación Spring Boot
- La URL `chatbot.aws.url` apunta a `localhost:8080`

### Base de datos
- Asegúrate de ejecutar `fix_ticket_auto_increment.sql`
- Sin este script, la creación de tickets fallará

### Firewall
Si usas AWS EC2 o similar:
- Abre el puerto 8080 en el Security Group
- Regla: TCP 8080 desde 0.0.0.0/0

---

## ✅ CHECKLIST DE DESPLIEGUE

- [ ] Java 17+ instalado en VM
- [ ] MySQL 8.0 corriendo
- [ ] Base de datos `db_telito` creada
- [ ] Script `fix_ticket_auto_increment.sql` ejecutado
- [ ] Variables de entorno configuradas
- [ ] IP de VM actualizada en `application-production.properties`
- [ ] JAR compilado con `mvnw clean package`
- [ ] JAR copiado a la VM
- [ ] Aplicación iniciada con perfil `production`
- [ ] Puerto 8080 abierto en firewall
- [ ] Aplicación accesible desde navegador
