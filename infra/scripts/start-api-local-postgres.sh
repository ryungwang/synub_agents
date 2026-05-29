#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck disable=SC1091
source "$ROOT/infra/scripts/load-env.sh"

export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/synub_agents}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-synub_agents}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-synub_agents}"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"

if lsof -iTCP:5432 -sTCP:LISTEN -Pn >/dev/null 2>&1; then
  echo "PostgreSQL port 5432 is already in use. Using the existing server."
else
  docker compose -f "$ROOT/infra/docker/docker-compose.local.yml" up -d postgres
fi

JAR="$ROOT/apps/api/build/libs/synub-agents-api-0.1.0.jar"

cd "$ROOT"
sh ./gradlew -p apps/api bootJar
java -jar "$JAR" --spring.profiles.active=local-postgres
