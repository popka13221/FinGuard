#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> FinGuard local runner"

# Options
CHECK_ONLY=0
BUILD_SPA=0

for arg in "$@"; do
  case "$arg" in
    --spa)
      BUILD_SPA=1
      ;;
    --check)
      CHECK_ONLY=1
      ;;
    --no-spa)
      BUILD_SPA=0
      ;;
    --help|-h)
      cat <<'EOF'
Usage: ./scripts/run-local.sh [options]

Starts FinGuard backend (Spring Boot). By default serves the legacy static UI from `/app/*.html`.

Options:
  --check                         Start, wait until ready, then stop
  --spa                           Build and serve the React SPA from the backend
  --no-spa                        Do not build the SPA (default)
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

hash_file_sha256() {
  local file="$1"
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file" | awk '{print $1}'
    return 0
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
    return 0
  fi
  return 1
}

echo "-> DB: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

SPA_BUILT=0
if [ "$BUILD_SPA" -eq 1 ] && [ -f "frontend/package.json" ]; then
  if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
    NODE_MAJOR="$(node -p "Number(process.versions.node.split('.')[0])" 2>/dev/null || echo "")"
    if [ -n "$NODE_MAJOR" ] && [ "$NODE_MAJOR" -lt 18 ]; then
      echo "-> Node.js ${NODE_MAJOR} detected; skipping SPA build. Install Node 18+ to build the SPA."
    else
      LOCK_HASH=""
      if [ -f "frontend/package-lock.json" ]; then
        LOCK_HASH="$(hash_file_sha256 frontend/package-lock.json 2>/dev/null || true)"
      fi
      LOCK_MARKER="frontend/node_modules/.finguard_lock_hash"
      NEED_INSTALL=0
      if [ ! -d "frontend/node_modules" ]; then
        NEED_INSTALL=1
      elif [ -n "$LOCK_HASH" ] && [ "$(cat "$LOCK_MARKER" 2>/dev/null || true)" != "$LOCK_HASH" ]; then
        NEED_INSTALL=1
      fi
      if [ "$NEED_INSTALL" -eq 1 ]; then
        echo "-> Installing frontend dependencies (npm ci)..."
        (cd frontend && npm ci)
        if [ -n "$LOCK_HASH" ]; then
          echo "$LOCK_HASH" > "$LOCK_MARKER"
        fi
      fi

      echo "-> Building SPA (Vite)..."
      (cd frontend && npm run build)
      if [ -f "frontend/dist/index.html" ]; then
        SPA_BUILT=1
        export SPRING_WEB_RESOURCES_STATIC_LOCATIONS="file:${ROOT_DIR}/frontend/dist/,classpath:/static/"
      else
        echo "-> SPA build finished, but frontend/dist/index.html not found. Skipping SPA serve."
      fi
    fi
  else
    echo "-> Node.js/npm not found; skipping SPA build."
  fi
fi

echo "-> Starting backend (Spring Boot) on ${BACKEND_URL} ..."
mvn spring-boot:run &
BACKEND_PID="$!"

echo
echo "Backend:"
echo "  - API:        ${BACKEND_URL}/api"
echo "  - Static UI:  ${BACKEND_URL}/app/dashboard.html"
echo "  - Swagger:    ${BACKEND_URL}/swagger-ui/index.html"
if [ "$SPA_BUILT" -eq 1 ]; then
  echo "  - SPA UI:     ${BACKEND_URL}/dashboard"
fi
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
