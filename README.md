# FinGuard — Personal Finance & Alerts Platform

English | [Русский](README.ru.md)

[![CI](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml)
[![CodeQL](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml)

Personal finance tracker with goals and alerts for spending/FX/assets. The main goal is to showcase a clean domain model, money/date/currency handling, background jobs, and integrations.

## What’s inside
- **MVP**: users + JWT, accounts, categories, transactions with balance recalculation, period reports, FX rates, simple rules with notifications.
- **Growth**: savings goals, FX/crypto integrations with caching, advanced rules, Email/Telegram, extracting a rule-engine and a message queue.
- **Architecture**: layered monolith (Controller → Service → Repository → Domain/Model + Config/Security/Scheduler).

## Tech stack
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling, Actuator)
- PostgreSQL + Flyway
- Maven, Docker Compose (Postgres)
- Planned: OpenFeign/WebClient, Kafka/RabbitMQ

## Quick start
1) Prerequisites
   - Java 17+, Maven
   - Docker + Docker Compose (for Postgres)
2) Database (defaults to `finguard`/`finguard`)
   ```bash
   docker compose up -d
   ```
   Or use your own DB and export env vars:
   ```bash
   export DB_HOST=localhost DB_PORT=5432 DB_NAME=finguard DB_USER=finguard DB_PASSWORD=finguard
   ```
3) Run backend
   ```bash
   ./scripts/run-local.sh      # starts backend (and Postgres via docker compose if available)
   # or
   mvn spring-boot:run
   ```
4) UI and API
   - Static UI: `http://localhost:8080/app/login.html` (login/registration/password recovery, dashboard)
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/health` or `/actuator/health`

## Auth flow
- Registration is 2-step: `POST /api/auth/register` creates a record in `pending_registrations` and sends a code by email; `users` record and tokens are only created after `POST /api/auth/verify` (registration is considered complete only after verify).
- Before verify there is no user in `users`, so access to protected `/api/**` is impossible by design.
- `FG_AUTH` and `FG_REFRESH` are set as httpOnly cookies after successful `POST /api/auth/login` (or `POST /api/auth/login/otp`) and after `POST /api/auth/verify`; SameSite is configured via `app.security.jwt.cookie-samesite`.
- Login before email verification returns 403 with code `100006` (if the password matches pending registration).
- Refresh: `POST /api/auth/refresh`
- Email verification: `POST /api/auth/verify/request`, `POST /api/auth/verify` (currently the default code is fixed to `654321`; you can override it via `app.security.tokens.fixed-code`)
- Password reset (2-step, no direct “change by code”):
  1) `POST /api/auth/forgot` — email/code.
  2) `POST /api/auth/reset/confirm` — takes `email+code`, returns a short-lived `resetSessionToken` (1 per user, TTL ~10–15 min, IP/UA binding, separate rate limits).
  3) `POST /api/auth/reset` — takes `resetSessionToken` + new password, invalidates all refresh sessions.
- OTP (optional): after a successful password check returns a 202 challenge; issuance rate limits by email+IP, and a repeated login within the TTL returns 202 without resending a code.
- CORS: set `ALLOWED_ORIGINS` for frontend on another domain; `credentials: true` is enabled.
- Rate limit: IP-based filter (`AuthRateLimitFilter`) + per email/IP limits in services; behind a proxy enable `app.security.trust-proxy-headers=true` to read `X-Forwarded-For`.

## Frontend
- Static client (Russian UI): `theme.js`, `api.js`, `auth.js`, `dashboard.js`, `recover.js`.

## Tests & coverage
- Backend: `mvn test` (JaCoCo report: `target/site/jacoco/index.html`).
- In GitHub Actions the report is uploaded as an artifact `jacoco-report`.

## Status
Spring Boot 3.2.5 scaffold with Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose for Postgres, basic `application.yaml`, health-check, migrations V1 (users/accounts/categories/transactions) + V2/V3/V4 (tokens/sessions + reset sessions), JWT security and Auth API (register/login/refresh/verify/reset with session-token), static client (Russian UI), and Swagger UI. Next — CRUD for Accounts/Categories/Transactions and reports.

