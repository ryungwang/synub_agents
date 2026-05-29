$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runDir = Join-Path $root ".run"

if (-not (Test-Path $runDir)) {
  New-Item -ItemType Directory -Force -Path $runDir | Out-Null
}

function Stop-LocalPid {
  param(
    [string]$Name,
    [int]$ProcessId
  )

  $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
  if ($process) {
    taskkill /PID $ProcessId /T /F | Out-Null
    Write-Host "Stopped $Name. PID: $ProcessId"
  } else {
    Write-Host "$Name was not running."
  }
}

Get-ChildItem -LiteralPath $runDir -Filter "*.pid" | ForEach-Object {
  $name = $_.BaseName
  $processId = Get-Content -LiteralPath $_.FullName -ErrorAction SilentlyContinue

  if ($processId) {
    Stop-LocalPid $name $processId
  }

  Remove-Item -LiteralPath $_.FullName -Force
}

@(
  @{ Name = "api"; Port = 8080 },
  @{ Name = "web"; Port = 3002 }
) | ForEach-Object {
  $connection = Get-NetTCPConnection -LocalPort $_.Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($connection) {
    Stop-LocalPid $_.Name $connection.OwningProcess
  }
}
