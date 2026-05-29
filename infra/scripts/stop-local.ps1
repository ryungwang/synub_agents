$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runDir = Join-Path $root ".run"

if (-not (Test-Path $runDir)) {
  Write-Host "No local service state found."
  return
}

Get-ChildItem -LiteralPath $runDir -Filter "*.pid" | ForEach-Object {
  $name = $_.BaseName
  $processId = Get-Content -LiteralPath $_.FullName -ErrorAction SilentlyContinue

  if ($processId) {
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
      Stop-Process -Id $processId -Force
      Write-Host "Stopped $name. PID: $processId"
    } else {
      Write-Host "$name was not running. PID file removed."
    }
  }

  Remove-Item -LiteralPath $_.FullName -Force
}
