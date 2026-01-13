# FinGuard — Трекер личных финансов (Spring Boot)

[English](README.md) | Русский

[![CI](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml)
[![CodeQL](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml)

Портфолио‑проект: учёт личных финансов (счета/транзакции/отчёты), курсы валют и крипты, статический dashboard.

## Возможности (v1)
- Auth: регистрация + подтверждение email, логин, refresh, опциональный OTP; JWT в httpOnly cookie (или `Authorization: Bearer`).
- Счета: CRUD, архивирование, баланс пересчитывается по транзакциям.
- Категории: глобальные дефолты + пользовательские.
- Транзакции: доход/расход, пересчёт баланса счёта.
- Отчёты: summary/by-category/cash-flow с конвертацией в базовую валюту пользователя.
- FX: хранение и выдача курсов (admin upsert API).
- Crypto: курсы BTC/ETH + summary по крипто‑кошелькам.
- UI: лендинг, auth страницы, dashboard (EN/RU + переключение базовой валюты).

## Стек
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling, Actuator)
- PostgreSQL + Flyway
- Maven, Docker Compose (Postgres)
- Playwright (E2E)

## Быстрый старт (локально)
1) Требования: Java 17+, Maven, Docker
2) Настройка env
   - Скопируйте `.env.example` → `.env` и задайте `JWT_SECRET` (Base64, 32+ байта).
   - `./scripts/run-local.sh` автоматически подхватывает `.env`.
   - Сгенерировать секрет:
     ```bash
     python -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(48)).decode())"
     ```
3) Поднять Postgres
   ```bash
   docker compose up -d postgres
   ```
4) Запустить backend
   ```bash
   ./scripts/run-local.sh
   # или
   mvn spring-boot:run
   ```
5) Открыть
   - UI: `http://localhost:8080/`
   - Вход: `http://localhost:8080/app/login.html`
   - Dashboard: `http://localhost:8080/app/dashboard.html`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/actuator/health`

## Быстрый демо (UI + Swagger)
1) Зарегистрируйтесь в UI (`/app/login.html`) и подтвердите email (для локальной разработки поставьте `APP_SECURITY_TOKENS_FIXED_CODE=654321`, чтобы код был предсказуемым).
2) Создайте пару счетов/транзакций через Swagger (или `docs/API_EXAMPLES.ru.md`) и обновите `/app/dashboard.html`.

## Конфигурация (env)
- DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (или `DB_URL`).
- JWT: `JWT_SECRET` обязателен (Base64, 32+ байта).
- CSRF: `APP_SECURITY_CSRF_ENABLED=true|false` (для быстрых экспериментов с curl/Swagger можно отключить).
- OTP: `OTP_ENABLED=true|false`.
- Для разработки: `APP_SECURITY_TOKENS_FIXED_CODE=654321` (не использовать в prod).

## Примеры API
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Примеры запросов (curl + Postman): `docs/API_EXAMPLES.ru.md`

## Тесты
- Backend: `mvn test`
- E2E:
  ```bash
  npm ci
  npx playwright install --with-deps chromium
  npm run e2e
  ```

## Дорожная карта
- Правила + уведомления: лимит расходов по категории за месяц + in‑app алерты.
- Goals: прогресс + requiredMonthly.
- Документация: ER‑диаграмма, release notes, cookbook по API.
