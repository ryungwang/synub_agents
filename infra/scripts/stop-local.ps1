$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
docker compose -f (Join-Path $root "infra\docker\docker-compose.local.yml") down
