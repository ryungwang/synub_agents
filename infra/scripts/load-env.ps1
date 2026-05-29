$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$envPath = Join-Path $root ".env"

if (-not (Test-Path $envPath)) {
  throw ".env file not found: $envPath"
}

Get-Content -LiteralPath $envPath | ForEach-Object {
  $line = $_.Trim()
  if (-not $line -or $line.StartsWith("#")) {
    return
  }

  $name, $value = $line -split "=", 2
  if (-not $name) {
    return
  }

  [Environment]::SetEnvironmentVariable($name.Trim(), $value, "Process")
}
