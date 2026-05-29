param(
  [switch]$WithWorker
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$runDir = Join-Path $root ".run"
$logDir = Join-Path $runDir "logs"

New-Item -ItemType Directory -Force -Path $runDir, $logDir | Out-Null

function Test-RunningPid {
  param([string]$PidFile)

  if (-not (Test-Path $PidFile)) {
    return $false
  }

  $processId = Get-Content -LiteralPath $PidFile -ErrorAction SilentlyContinue
  if (-not $processId) {
    return $false
  }

  return [bool](Get-Process -Id $processId -ErrorAction SilentlyContinue)
}

function Start-LocalProcess {
  param(
    [string]$Name,
    [string]$Script,
    [int]$Port = 0
  )

  $pidFile = Join-Path $runDir "$Name.pid"
  if (Test-RunningPid $pidFile) {
    Write-Host "$Name is already running. PID: $(Get-Content -LiteralPath $pidFile)"
    return
  }

  if ($Port -gt 0) {
    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection) {
      Write-Host "$Name port $Port is already in use. Existing PID: $($connection.OwningProcess)"
      return
    }
  }

  $stdout = Join-Path $logDir "$Name.out.log"
  $stderr = Join-Path $logDir "$Name.err.log"
  $process = Start-Process powershell `
    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $Script) `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

  Set-Content -LiteralPath $pidFile -Value $process.Id
  Write-Host "Started $Name. PID: $($process.Id)"
}

Write-Host "Starting Synub Agents local services..."
Write-Host "Local mode uses the H2 file database. No PostgreSQL or Docker is required."

Start-LocalProcess "api" (Join-Path $PSScriptRoot "start-api-local.ps1") 8080
Start-LocalProcess "web" (Join-Path $PSScriptRoot "start-web-local.ps1") 3002

if ($WithWorker) {
  Start-LocalProcess "worker" (Join-Path $PSScriptRoot "start-worker-local.ps1")
} else {
  Write-Host "Worker was not started. Use '.\infra\scripts\start-local.ps1 -WithWorker' when agent execution is needed."
}

Write-Host "Dashboard URL: http://127.0.0.1:3002"
Write-Host "Logs: $logDir"
Write-Host "Stop command: .\infra\scripts\stop-local.ps1"
