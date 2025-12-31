#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> FinGuard local runner"

# Options
CHECK_ONLY=0

for arg in "$@"; do
  case "$arg" in
    --check)
      CHECK_ONLY=1
      ;;
    --help|-h)
      cat <<'EOF'
Usage: ./scripts/run-local.sh [options]

Starts FinGuard backend (Spring Boot) and serves the static UI from `/app/*.html`.

Options:
  --check                         Start, wait until ready, then stop
EOF
      exit 0
      ;;
  esac
done

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

BACKEND_PORT="${BACKEND_PORT:-8080}"
BACKEND_URL="${BACKEND_URL:-http://localhost:${BACKEND_PORT}}"
export ALLOWED_ORIGINS="${ALLOWED_ORIGINS:-${BACKEND_URL}}"

BACKEND_PID=""

kill_tree() {
  local pid="$1"
  if [ -z "$pid" ]; then
    return 0
  fi
  local children=""
  children="$(pgrep -P "$pid" 2>/dev/null || true)"
  for child in $children; do
    kill_tree "$child"
  done
  kill "$pid" 2>/dev/null || true
}

cleanup() {
  if [ -n "$BACKEND_PID" ]; then
    echo "-> Stopping backend (pid $BACKEND_PID)"
    kill_tree "$BACKEND_PID" || true
  fi
}
trap cleanup EXIT INT TERM

wait_for_http() {
  local name="$1"
  local url="$2"
  local seconds="${3:-90}"
  local pid="${4:-}"
  local start
  start="$(date +%s)"
  echo "-> Waiting for ${name} (${url})"
  while true; do
    if [ -n "$pid" ] && ! kill -0 "$pid" >/dev/null 2>&1; then
      echo "-> ${name} process exited before becoming ready"
      return 1
    fi
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "-> ${name} is ready"
      return 0
    fi
    local now
    now="$(date +%s)"
    if [ $((now - start)) -ge "$seconds" ]; then
      echo "-> ${name} did not become ready in ${seconds}s"
      return 1
    fi
    sleep 1
  done
}

echo "-> DB: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

echo "-> Starting backend (Spring Boot) on ${BACKEND_URL} ..."
mvn spring-boot:run &
BACKEND_PID="$!"

echo
echo "Backend:"
echo "  - API:        ${BACKEND_URL}/api"
echo "  - Static UI:  ${BACKEND_URL}/app/dashboard.html"
echo "  - Swagger:    ${BACKEND_URL}/swagger-ui/index.html"
echo

READY_OK=0
wait_for_http "backend" "${BACKEND_URL}/health" 120 "$BACKEND_PID" || READY_OK=1

if [ "$CHECK_ONLY" -eq 1 ]; then
  if [ "$READY_OK" -eq 0 ]; then
    wait_for_http "static-ui" "${BACKEND_URL}/app/dashboard.html" 30 "$BACKEND_PID" || READY_OK=1
  fi
  exit "$READY_OK"
fi

wait "$BACKEND_PID"
