# FinGuard — Personal Finance & Alerts Platform

[![CI](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/ci.yml)
[![CodeQL](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml/badge.svg)](https://github.com/popka13221/FinGuard/actions/workflows/codeql.yml)

Платформа для учёта личных финансов, целей и алертов по тратам/курсам/активам. Цель — показать продуманную доменную модель, работу с деньгами/датами/валютами, фоновые задачи и интеграции.

## Что внутри
- **MVP**: пользователи + JWT, счета, категории, транзакции с пересчётом баланса, отчёты по периодам, курсы валют, простые правила с уведомлениями.
- **Рост**: цели накоплений, интеграции с FX/криптой и кешированием, продвинутые правила, Email/Telegram, вынос rule-engine и очередь сообщений.
- **Архитектура**: слоёный монолит (Controller → Service → Repository → Domain/Model + Config/Security/Scheduler).

## Стек
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling, Actuator)
- PostgreSQL + Flyway
- Maven, Docker Compose (Postgres)
- Планируется: OpenFeign/WebClient, Kafka/RabbitMQ

## Быстрый старт
1) Подготовка окружения
   - Java 17+, Maven
   - Docker + Docker Compose (для Postgres)
2) База (по умолчанию `finguard`/`finguard`)
   ```bash
   docker compose up -d
   ```
   или заведите свою БД и пропишите переменные:
   ```bash
   export DB_HOST=localhost DB_PORT=5432 DB_NAME=finguard DB_USER=finguard DB_PASSWORD=finguard
   ```
3) Запуск backend
   ```bash
   ./scripts/run-local.sh      # поднимет backend (и Postgres через docker compose, если доступен)
   # или
   mvn spring-boot:run
   ```
4) UI и API
   - Статика: `http://localhost:8080/app/login.html` (вход/регистрация/восстановление пароля, дашборд).
    - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
    - Health: `http://localhost:8080/health` или `/actuator/health`

## Auth-флоу
- Регистрация двухшаговая: `POST /api/auth/register` создаёт запись в `pending_registrations` и отправляет код на email; запись в `users` и токены выдаются только после `POST /api/auth/verify`.
- Токены `FG_AUTH` и `FG_REFRESH` ставятся в httpOnly cookie после успешного `POST /api/auth/login` (или `POST /api/auth/login/otp`) и после `POST /api/auth/verify`; SameSite настраивается через `app.security.jwt.cookie-samesite`.
- При попытке логина до подтверждения email возвращается 403, код `100006` (если пароль совпадает с pending-регистрацией).
- Refresh: `POST /api/auth/refresh`
- Верификация email: `POST /api/auth/verify/request`, `POST /api/auth/verify` (сейчас по умолчанию код фиксированный `654321`; можно переопределить через `app.security.tokens.fixed-code`)
- Восстановление пароля (двухшаговый, без прямой смены по коду):
  1) `POST /api/auth/forgot` — письмо/код.
  2) `POST /api/auth/reset/confirm` — принимает `email+код`, выдаёт короткоживущий `resetSessionToken` (1 на пользователя, TTL ~10–15 мин, привязка IP/UA, отдельные rate limits).
  3) `POST /api/auth/reset` — принимает `resetSessionToken` + новый пароль, инвалидация всех refresh-сессий.
- OTP (опционально): после успешного пароля выдаётся challenge 202; лимиты на выдачу по email+IP, повторный вход в окне действия возвращает 202 без пересылки кода.
- CORS: задайте `ALLOWED_ORIGINS` для SPA на другом домене, включено `credentials: true`.
- Rate limit: фильтр по IP (`AuthRateLimitFilter`), доп. лимиты по email/IP в сервисе; за прокси включите `app.security.trust-proxy-headers=true` для чтения `X-Forwarded-For`.

## Frontend
- Статический клиент (русский): `theme.js`, `api.js`, `auth.js`, `dashboard.js`, `recover.js`; вкладки «Вход/Регистрация», формы для восстановления пароля, дашборд с проверкой health.
- SPA (React+TS, Vite): аналогичный флоу; запуск — `cd frontend && npm install && npm run dev` (прокси на бэк для `/api/**`, `/health`, `/actuator`, `/swagger-ui`). Детали: `frontend/README.md`.

## Тесты и покрытие
- Backend: `mvn test` (JaCoCo отчёт: `target/site/jacoco/index.html`).
- В GitHub Actions отчёт загружается как artifact `jacoco-report`.

## Статус
Собран каркас Spring Boot 3.2.5 с Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check, миграции V1 (users/accounts/categories/transactions) + V2/V3/V4 (токены/сессии + reset-сессии), JWT security и Auth API (register/login/refresh/verify/reset с session-token), статический клиент (русский UI) и Swagger UI. Далее — CRUD для Accounts/Categories/Transactions и отчёты, перенос токена в httpOnly cookies для SPA.
