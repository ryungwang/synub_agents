$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Write-Host "Starting PostgreSQL..."
docker compose -f (Join-Path $root "infra\docker\docker-compose.local.yml") up -d postgres

Write-Host "Start API in another terminal:"
Write-Host "  `$env:JAVA_HOME='C:\Users\User\.jdks\ms-21.0.11'"
Write-Host "  `$env:Path=`"`$env:JAVA_HOME\bin;`$env:Path`""
Write-Host "  .\gradlew.bat -p apps\api bootRun --args='--spring.profiles.active=local'"

Write-Host "Start web in another terminal:"
Write-Host "  cd apps\web"
Write-Host "  npm install"
Write-Host "  npm run dev -- --host 127.0.0.1 --port 3002"

Write-Host "Start worker in another terminal:"
Write-Host "  cd workers\codex-worker"
Write-Host "  py -m pip install -r requirements.txt"
Write-Host "  py src\main.py"
