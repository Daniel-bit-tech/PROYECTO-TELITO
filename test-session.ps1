# Script para diagnosticar problemas de sesión
# Ejecutar: .\test-session.ps1

$baseUrl = "http://localhost:8080"

Write-Host "🔍 DIAGNÓSTICO DE SESIÓN - TELITO DEV" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Verificar que el servidor está corriendo
Write-Host "1️⃣  Verificando que el servidor está activo..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/login" -Method GET -UseBasicParsing
    Write-Host "   ✅ Servidor activo (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "   ❌ El servidor no está respondiendo" -ForegroundColor Red
    Write-Host "   💡 Asegúrate de iniciar la aplicación con: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test 2: Verificar tablas de base de datos (requiere estar autenticado)
Write-Host "2️⃣  Para verificar las tablas de Spring Session:" -ForegroundColor Yellow
Write-Host "   1. Inicia sesión en la aplicación" -ForegroundColor White
Write-Host "   2. Luego ejecuta en PowerShell:" -ForegroundColor White
Write-Host "      Invoke-WebRequest -Uri '$baseUrl/api/session-debug/verify-tables' -UseDefaultCredentials" -ForegroundColor Cyan
Write-Host ""

# Test 3: Instrucciones para test de persistencia
Write-Host "3️⃣  Para probar la persistencia de sesión:" -ForegroundColor Yellow
Write-Host "   1. Inicia sesión marcando el checkbox 'Recordar sesión'" -ForegroundColor White
Write-Host "   2. Ejecuta varias veces:" -ForegroundColor White
Write-Host "      Invoke-WebRequest -Uri '$baseUrl/api/session-debug/test-persistence' -UseDefaultCredentials" -ForegroundColor Cyan
Write-Host "   3. El contador debe aumentar cada vez" -ForegroundColor White
Write-Host ""

# Test 4: Información de sesión
Write-Host "4️⃣  Para ver información completa de tu sesión:" -ForegroundColor Yellow
Write-Host "   Invoke-WebRequest -Uri '$baseUrl/api/session-debug/info' -UseDefaultCredentials | ConvertFrom-Json | ConvertTo-Json -Depth 10" -ForegroundColor Cyan
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 PASOS MANUALES RECOMENDADOS:" -ForegroundColor Green
Write-Host ""
Write-Host "1. 🔐 LOGIN CON REMEMBER ME:" -ForegroundColor Yellow
Write-Host "   - Ve a: $baseUrl/login" -ForegroundColor White
Write-Host "   - Ingresa tus credenciales" -ForegroundColor White
Write-Host "   - ⚠️  MARCA el checkbox 'Recordar sesión' o 'Remember me'" -ForegroundColor Red
Write-Host "   - Haz clic en 'Iniciar sesión'" -ForegroundColor White
Write-Host ""

Write-Host "2. 🧪 PROBAR PERSISTENCIA:" -ForegroundColor Yellow
Write-Host "   - Cierra completamente el navegador" -ForegroundColor White
Write-Host "   - Abre el navegador nuevamente" -ForegroundColor White
Write-Host "   - Ve a: $baseUrl" -ForegroundColor White
Write-Host "   - Deberías seguir conectado (sin pedirte login)" -ForegroundColor White
Write-Host ""

Write-Host "3. 🔍 DIAGNÓSTICO AVANZADO:" -ForegroundColor Yellow
Write-Host "   - En el navegador, ve a Developer Tools (F12)" -ForegroundColor White
Write-Host "   - Application → Cookies → http://localhost:8080" -ForegroundColor White
Write-Host "   - Busca la cookie 'JSESSIONID'" -ForegroundColor White
Write-Host "   - Verifica que 'Max-Age' sea 604800 (7 días)" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  PROBLEMAS COMUNES:" -ForegroundColor Red
Write-Host ""
Write-Host "❌ 'La sesión se pierde al cerrar el navegador'" -ForegroundColor Yellow
Write-Host "   Soluciones:" -ForegroundColor White
Write-Host "   1. Asegúrate de MARCAR el checkbox 'Remember me' al hacer login" -ForegroundColor White
Write-Host "   2. Verifica que tu navegador no esté configurado para borrar cookies al cerrarse" -ForegroundColor White
Write-Host "   3. Chrome: Settings → Privacy → NO marcar 'Clear cookies when you close all windows'" -ForegroundColor White
Write-Host ""

Write-Host "❌ 'Error: Table SPRING_SESSION doesn't exist'" -ForegroundColor Yellow
Write-Host "   Solución: Ejecutar el script SQL:" -ForegroundColor White
Write-Host "   mysql -u root -proot db_telito < verificar_spring_session.sql" -ForegroundColor Cyan
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Si todos los tests pasan, tu sesión debería persistir correctamente" -ForegroundColor Green
Write-Host ""
