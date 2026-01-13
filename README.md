# FinGuard — Personal Finance Tracker (Spring Boot)

English | [Русский](README.ru.md)

[![CI](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml)
[![CodeQL](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml)

Portfolio-ready personal finance tracker: accounts, transactions, reports, FX/crypto rates, and a static dashboard UI.

## Screenshots
![Dashboard](docs/screenshots/dashboard-en.png)
![Swagger UI](docs/screenshots/swagger-ui.png)

<details>
  <summary>More screenshots</summary>

  ![Landing](docs/screenshots/landing-en.png)
  ![Login](docs/screenshots/login-en.png)
</details>

## Features (v1)
- Auth: registration + email verification, login, refresh, optional OTP; JWT in httpOnly cookies (or `Authorization: Bearer`).
- Accounts: CRUD, archive, balances recalculated from transactions.
- Categories: global defaults + user-defined categories.
- Transactions: income/expense, recalculates account balance.
- Reports: summary/by-category/cash-flow with conversion into user base currency.
- FX: store and serve latest FX snapshots (admin upsert API).
- Crypto: BTC/ETH rates + crypto wallet summary.
- Static UI: landing, auth pages, dashboard (EN/RU + base currency switch).

## Tech stack
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling, Actuator)
- PostgreSQL + Flyway
- Maven, Docker Compose (Postgres)
- Playwright (E2E)

## Quick start (local)
1) Prerequisites: Java 17+, Maven, Docker
2) Configure env
   - Copy `.env.example` → `.env` and set `JWT_SECRET` (Base64, 32+ bytes).
   - `./scripts/run-local.sh` loads `.env` automatically.
   - Generate a local secret:
     ```bash
     python -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(48)).decode())"
     ```
3) Start Postgres
   ```bash
   docker compose up -d postgres
   ```
4) Run backend
   ```bash
   ./scripts/run-local.sh
   # or
   mvn spring-boot:run
   ```
5) Open
   - UI: `http://localhost:8080/`
   - Login: `http://localhost:8080/app/login.html`
   - Dashboard: `http://localhost:8080/app/dashboard.html`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/actuator/health`

## API examples
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Examples (curl + Postman): `docs/API_EXAMPLES.md`

## Tests
- Backend: `mvn test`
- E2E:
  ```bash
  npm ci
  npx playwright install --with-deps chromium
  npm run e2e
  ```

## Roadmap
- Rules + notifications: monthly category spending limit + in-app alerts.
- Goals: progress + required monthly savings.
- More docs: ER diagram, release notes, API cookbook.
