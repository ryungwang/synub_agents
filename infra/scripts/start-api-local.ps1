$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$jdk = "C:\Users\User\.jdks\ms-21.0.11"
if (Test-Path $jdk) {
  $env:JAVA_HOME = $jdk
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

$jar = Join-Path $root "apps\api\build\libs\ai-dev-staff-api-0.1.0.jar"
if (-not (Test-Path $jar)) {
  Push-Location $root
  try {
    .\gradlew.bat -p apps\api bootJar
  } finally {
    Pop-Location
  }
}

Push-Location $root
try {
  java -jar $jar --spring.profiles.active=local
} finally {
  Pop-Location
}
