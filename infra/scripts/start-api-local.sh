#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck disable=SC1091
source "$ROOT/infra/scripts/load-env.sh"

export SPRING_DATASOURCE_URL="jdbc:h2:file:./data/local-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
export SPRING_DATASOURCE_USERNAME="sa"
export SPRING_DATASOURCE_PASSWORD=""
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver"

JAR="$ROOT/apps/api/build/libs/synub-agents-api-0.1.0.jar"

cd "$ROOT"
sh ./gradlew -p apps/api bootJar
java -jar "$JAR" --spring.profiles.active=local
