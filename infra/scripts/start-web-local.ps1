$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$webRoot = Join-Path $root "apps\web"
$nodeRoot = "C:\Program Files\nodejs"

if (-not (Get-Command npm -ErrorAction SilentlyContinue) -and (Test-Path (Join-Path $nodeRoot "npm.cmd"))) {
  $env:Path = "$nodeRoot;$env:Path"
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
  throw "npm was not found. Install Node.js with npm, or add C:\Program Files\nodejs to PATH."
}

Push-Location $webRoot
try {
  if (-not (Test-Path "node_modules")) {
    npm install
  }
  .\node_modules\.bin\vite.cmd --host 127.0.0.1 --port 3002 --strictPort
} finally {
  Pop-Location
}
