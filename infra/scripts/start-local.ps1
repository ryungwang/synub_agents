$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Write-Host "Local mode uses the H2 file database."
Write-Host "No PostgreSQL or Docker startup is required."

Write-Host "Start API in another terminal:"
Write-Host "  .\infra\scripts\start-api-local.ps1"

Write-Host "Start web in another terminal:"
Write-Host "  .\infra\scripts\start-web-local.ps1"

Write-Host "Start worker in another terminal:"
Write-Host "  .\infra\scripts\start-worker-local.ps1"
