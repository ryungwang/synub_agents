#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo ".env file not found: $ENV_FILE" >&2
  exit 1
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line%$'\r'}"
  line="${line#$'\ufeff'}"

  [[ -n "$line" ]] || continue
  [[ "$line" == \#* ]] && continue
  [[ "$line" == *=* ]] || continue

  name="${line%%=*}"
  value="${line#*=}"
  name="${name#"${name%%[![:space:]]*}"}"
  name="${name%"${name##*[![:space:]]}"}"

  [[ -n "$name" ]] || continue
  export "$name=$value"
done < "$ENV_FILE"
