$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
. (Join-Path $PSScriptRoot "load-env.ps1")

$env:SPRING_DATASOURCE_URL = "jdbc:h2:file:./data/local-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
$env:SPRING_DATASOURCE_USERNAME = "sa"
$env:SPRING_DATASOURCE_PASSWORD = ""
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = "org.h2.Driver"

$jdk = "C:\Users\User\.jdks\ms-21.0.11"
if (Test-Path $jdk) {
  $env:JAVA_HOME = $jdk
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

$jar = Join-Path $root "apps\api\build\libs\ai-dev-staff-api-0.1.0.jar"
Push-Location $root
try {
  .\gradlew.bat -p apps\api bootJar
} finally {
  Pop-Location
}

Push-Location $root
try {
  java -jar $jar --spring.profiles.active=local
} finally {
  Pop-Location
}
