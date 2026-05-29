#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck disable=SC1091
source "$ROOT/infra/scripts/load-env.sh"

cd "$ROOT/apps/web"
if [[ ! -d node_modules ]]; then
  npm install
fi

./node_modules/.bin/vite --host 127.0.0.1 --port 3002 --strictPort
