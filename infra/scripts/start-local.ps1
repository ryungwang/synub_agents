$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Write-Host "Starting PostgreSQL..."
docker compose -f (Join-Path $root "infra\docker\docker-compose.local.yml") up -d postgres

Write-Host "Start API in another terminal:"
Write-Host "  .\infra\scripts\start-api-local.ps1"

Write-Host "Start web in another terminal:"
Write-Host "  .\infra\scripts\start-web-local.ps1"

Write-Host "Start worker in another terminal:"
Write-Host "  .\infra\scripts\start-worker-local.ps1"
