# Script para limpiar el proyecto cuando Maven clean falla
# Ejecutar desde PowerShell como administrador

Write-Host "Limpiando proyecto TELITODEV..." -ForegroundColor Green

# Detener todos los procesos de Java
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Esperar un momento
Start-Sleep -Seconds 2

# Eliminar carpeta target
if (Test-Path ".\target") {
    Write-Host "Eliminando carpeta target..." -ForegroundColor Yellow
    
    # Intentar eliminar normalmente
    try {
        Remove-Item -Path ".\target" -Recurse -Force
        Write-Host "Carpeta target eliminada exitosamente." -ForegroundColor Green
    } catch {
        Write-Host "Error eliminando target normalmente. Intentando método alternativo..." -ForegroundColor Red
        
        # Método alternativo usando robocopy para "limpiar" la carpeta
        $emptyDir = New-TemporaryFile
        Remove-Item $emptyDir
        New-Item -ItemType Directory -Path $emptyDir
        
        robocopy $emptyDir ".\target" /MIR /NP /NJH /NJS
        Remove-Item ".\target" -Force -ErrorAction SilentlyContinue
        Remove-Item $emptyDir -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Limpieza completada. Ahora puedes ejecutar 'mvn clean compile' sin problemas." -ForegroundColor Green
