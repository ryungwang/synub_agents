$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$env:SPRING_DATASOURCE_URL = if ($env:SPRING_DATASOURCE_URL) { $env:SPRING_DATASOURCE_URL } else { "jdbc:postgresql://localhost:5432/synub_agents" }
$env:SPRING_DATASOURCE_USERNAME = if ($env:SPRING_DATASOURCE_USERNAME) { $env:SPRING_DATASOURCE_USERNAME } else { "synub_agents" }
$env:SPRING_DATASOURCE_PASSWORD = if ($env:SPRING_DATASOURCE_PASSWORD) { $env:SPRING_DATASOURCE_PASSWORD } else { "synub_agents" }
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = "org.postgresql.Driver"

$postgresConnection = Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($postgresConnection) {
  Write-Host "PostgreSQL port 5432 is already in use. Existing PID: $($postgresConnection.OwningProcess)"
} else {
  docker compose -f (Join-Path $root "infra\docker\docker-compose.local.yml") up -d postgres
}

$jdk = "C:\Users\User\.jdks\ms-21.0.11"
if (Test-Path $jdk) {
  $env:JAVA_HOME = $jdk
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

$jar = Join-Path $root "apps\api\build\libs\synub-agents-api-0.1.0.jar"
Push-Location $root
try {
  .\gradlew.bat -p apps\api bootJar
} finally {
  Pop-Location
}

Push-Location $root
try {
  java -jar $jar --spring.profiles.active=local-postgres
} finally {
  Pop-Location
}
