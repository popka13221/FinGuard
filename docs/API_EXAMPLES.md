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

Register → verify → get a JWT:

```bash
BASE="http://localhost:8080"
EMAIL="demo-$(date +%s)@example.com"
PASS="StrongPass1!"

curl -sS -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\",\"fullName\":\"Demo User\",\"baseCurrency\":\"USD\"}"

# Default verification code is fixed to 654321 (config: app.security.tokens.fixed-code)
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
- If CSRF is enabled, use the UI for auth and CSRF, or disable CSRF in local env (see above) for quick API exploration.

