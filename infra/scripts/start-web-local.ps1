$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$webRoot = Join-Path $root "apps\web"

Push-Location $webRoot
try {
  if (-not (Test-Path "node_modules")) {
    npm install
  }
  npm run dev -- --host 127.0.0.1 --port 3002
} finally {
  Pop-Location
}
