#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck disable=SC1091
source "$ROOT/infra/scripts/load-env.sh"

cd "$ROOT/workers/codex-worker"

PYTHON_BIN="python"
if command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN="python3"
fi

if [[ ! -x ".venv/bin/python" ]]; then
  "$PYTHON_BIN" -m venv .venv
fi

if [[ -f "requirements.txt" ]]; then
  .venv/bin/python -m pip install -q -r requirements.txt
fi

if [[ -f "$ROOT/.env" ]]; then
  export WORKER_API_BASE_URL="${WORKER_API_BASE_URL:-http://127.0.0.1:8080}"
fi

if command -v codex >/dev/null 2>&1; then
  export CODEX_COMMAND="${CODEX_COMMAND:-$(command -v codex)}"
else
  export CODEX_COMMAND="${CODEX_COMMAND:-codex}"
fi

.venv/bin/python src/main.py
