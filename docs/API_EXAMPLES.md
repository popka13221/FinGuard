# FinGuard — API examples (Swagger / curl)

Base URL (local): `http://localhost:8080`

## Public endpoints
```bash
curl -s http://localhost:8080/api/currencies
curl -s "http://localhost:8080/api/fx/rates?base=USD&quote=EUR&quote=RUB"
curl -s "http://localhost:8080/api/crypto/rates?base=USD"
```

## Authenticated flow (curl, easiest local setup)
CSRF is enabled by default (the static UI handles it automatically). For simple curl examples, run locally with CSRF disabled:

```bash
export APP_SECURITY_CSRF_ENABLED=false
./scripts/run-local.sh
```

### CSRF-enabled curl (without disabling CSRF)
If you want to keep CSRF enabled, you need both the `XSRF-TOKEN` cookie and the `X-XSRF-TOKEN` header. One simple approach:

```bash
BASE="http://localhost:8080"
COOKIE_JAR=".cookies.txt"

csrf() {
  curl -sS -c "$COOKIE_JAR" -b "$COOKIE_JAR" "$BASE/api/auth/csrf" \
    | python -c "import sys,json; print(json.load(sys.stdin)['token'])"
}
CSRF="$(csrf)"

# Example: register (CSRF-enabled)
EMAIL="demo-$(date +%s)@example.com"
PASS="StrongPass1!"
curl -sS -X POST "$BASE/api/auth/register" \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" -H "X-XSRF-TOKEN: $CSRF" -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"fullName\":\"Demo User\",\"baseCurrency\":\"USD\"}"
```

Register → verify → get a JWT:

```bash
BASE="http://localhost:8080"
EMAIL="demo-$(date +%s)@example.com"
PASS="StrongPass1!"

# Optional (recommended for local dev): make the verification/reset code predictable.
export APP_SECURITY_TOKENS_FIXED_CODE=654321

curl -sS -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"fullName\":\"Demo User\",\"baseCurrency\":\"USD\"}"

# Verify email (uses the fixed code above)
TOKEN="$(curl -sS -X POST "$BASE/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"token\":\"654321\"}" | python -c "import sys,json; print(json.load(sys.stdin)['token'])")"
echo "$TOKEN" | head -c 24 && echo "…"
```

Create data:

```bash
# Create account
ACCOUNT_ID="$(curl -sS -X POST "$BASE/api/accounts" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Cash","currency":"USD","initialBalance":1200}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['id'])")"

# Create category
CATEGORY_ID="$(curl -sS -X POST "$BASE/api/categories" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Food","type":"EXPENSE"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['id'])")"

# Create transaction
curl -sS -X POST "$BASE/api/transactions" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"accountId\":$ACCOUNT_ID,\"categoryId\":$CATEGORY_ID,\"type\":\"EXPENSE\",\"amount\":35,\"transactionDate\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"description\":\"Coffee\"}" \
  >/dev/null
```

Fetch balances & reports:

```bash
curl -sS -H "Authorization: Bearer $TOKEN" "$BASE/api/accounts/balance"
curl -sS -H "Authorization: Bearer $TOKEN" "$BASE/api/reports/summary?period=MONTH"
curl -sS -H "Authorization: Bearer $TOKEN" "$BASE/api/reports/by-category?period=MONTH&limit=10"
curl -sS -H "Authorization: Bearer $TOKEN" "$BASE/api/reports/cash-flow"
```

## Swagger UI tips
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Swagger can generate requests, but it does not automatically send CSRF headers. For quick local exploration either disable CSRF or use the curl flow above.
- Postman: you can import OpenAPI schema from `http://localhost:8080/v3/api-docs`.
