# Carga las variables de .env en la sesion actual de PowerShell y levanta el backend.
# Uso: .\run-dev.ps1

$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Error "No se encontro .env en $envFile. Copia .env.example a .env y completa los valores."
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
        $key, $value = $line.Split("=", 2)
        $value = $value.Trim()
        # Una variable vacia no se define: asi Spring usa el default (ej. ${JWT_SECRET:default})
        # en vez de recibir un string vacio, que rompe la validacion de la clave HMAC.
        if ($value) {
            [System.Environment]::SetEnvironmentVariable($key.Trim(), $value, "Process")
        }
    }
}

& "$PSScriptRoot\mvnw.cmd" spring-boot:run
