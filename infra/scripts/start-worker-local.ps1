$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$workerRoot = Join-Path $root "workers\codex-worker"

Push-Location $workerRoot
try {
  py src\main.py
} finally {
  Pop-Location
}
