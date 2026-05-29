#!/usr/bin/env bash
set -euo pipefail

POSTGRES=false
WITH_WORKER=false

for arg in "$@"; do
  case "$arg" in
    --postgres|-Postgres)
      POSTGRES=true
      ;;
    --with-worker|-WithWorker)
      WITH_WORKER=true
      ;;
    *)
      echo "Unknown option: $arg" >&2
      echo "Usage: $0 [--postgres] [--with-worker]" >&2
      exit 1
      ;;
  esac
done

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$ROOT/.run"
LOG_DIR="$RUN_DIR/logs"
mkdir -p "$LOG_DIR"

is_running_pid() {
  local pid_file="$1"
  [[ -f "$pid_file" ]] || return 1
  local pid
  pid="$(cat "$pid_file" 2>/dev/null || true)"
  [[ -n "$pid" ]] || return 1
  kill -0 "$pid" >/dev/null 2>&1
}

port_pid() {
  local port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN -Pn 2>/dev/null | head -n 1
}

start_process() {
  local name="$1"
  local script="$2"
  local port="${3:-}"
  local pid_file="$RUN_DIR/$name.pid"

  if is_running_pid "$pid_file"; then
    echo "$name is already running. PID: $(cat "$pid_file")"
    return
  fi

  if [[ -n "$port" ]]; then
    local existing_pid
    existing_pid="$(port_pid "$port" || true)"
    if [[ -n "$existing_pid" ]]; then
      echo "$name port $port is already in use. Existing PID: $existing_pid"
      echo "$existing_pid" > "$pid_file"
      return
    fi
  fi

  nohup bash "$script" > "$LOG_DIR/$name.out.log" 2> "$LOG_DIR/$name.err.log" &
  echo "$!" > "$pid_file"
  echo "Started $name. PID: $!"
}

echo "Starting Synub Agents local services..."
if [[ "$POSTGRES" == true ]]; then
  echo "Local mode uses PostgreSQL."
  API_SCRIPT="$ROOT/infra/scripts/start-api-local-postgres.sh"
else
  echo "Local mode uses the H2 file database. No PostgreSQL or Docker is required."
  API_SCRIPT="$ROOT/infra/scripts/start-api-local.sh"
fi

start_process "api" "$API_SCRIPT" 8080
start_process "web" "$ROOT/infra/scripts/start-web-local.sh" 3002

if [[ "$WITH_WORKER" == true ]]; then
  start_process "worker" "$ROOT/infra/scripts/start-worker-local.sh"
else
  echo "Worker was not started. Use './infra/scripts/start-local.sh --with-worker' when agent execution is needed."
fi

echo "Dashboard URL: http://127.0.0.1:3002"
echo "Logs: $LOG_DIR"
echo "Stop command: ./infra/scripts/stop-local.sh"
