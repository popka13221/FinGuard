#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> FinGuard local runner"

# Load .env if present
if [ -f ".env" ]; then
  echo "-> Loading .env"
  set -a
  source ".env"
  set +a
fi

# Start Postgres via Docker if available
if command -v docker >/dev/null 2>&1 && command -v docker compose >/dev/null 2>&1; then
  echo "-> Starting Postgres with docker compose..."
  docker compose up -d postgres
else
  echo "-> Docker not found. Make sure Postgres is running locally (see README)."
fi

# Default DB env if not provided
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-finguard}"
export DB_USER="${DB_USER:-finguard}"
export DB_PASSWORD="${DB_PASSWORD:-finguard}"

echo "-> DB: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "-> Running Spring Boot..."

mvn spring-boot:run
