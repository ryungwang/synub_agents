#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$ROOT/.run"
mkdir -p "$RUN_DIR"

stop_pid() {
  local name="$1"
  local pid="$2"

  if [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1; then
    pkill -TERM -P "$pid" >/dev/null 2>&1 || true
    kill "$pid" >/dev/null 2>&1 || true
    echo "Stopped $name. PID: $pid"
  else
    echo "$name was not running."
  fi
}

for pid_file in "$RUN_DIR"/*.pid; do
  [[ -e "$pid_file" ]] || continue
  name="$(basename "$pid_file" .pid)"
  pid="$(cat "$pid_file" 2>/dev/null || true)"
  stop_pid "$name" "$pid"
  rm -f "$pid_file"
done

for entry in "api:8080" "web:3002"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  pid="$(lsof -tiTCP:"$port" -sTCP:LISTEN -Pn 2>/dev/null | head -n 1 || true)"
  if [[ -n "$pid" ]]; then
    stop_pid "$name" "$pid"
  fi
done
