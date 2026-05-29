$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Write-Host "Starting PostgreSQL..."
docker compose -f (Join-Path $root "infra\docker\docker-compose.local.yml") up -d postgres

Write-Host "Start API in another terminal:"
Write-Host "  cd apps\api"
Write-Host "  .\gradlew bootRun"

Write-Host "Start web in another terminal:"
Write-Host "  cd apps\web"
Write-Host "  npm install"
Write-Host "  npm run dev -- --port 3000"

Write-Host "Start worker in another terminal:"
Write-Host "  cd workers\codex-worker"
Write-Host "  py -m pip install -r requirements.txt"
Write-Host "  py src\main.py"
