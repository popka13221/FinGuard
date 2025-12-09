# FinGuard — Personal Finance & Alerts Platform

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
   ./scripts/run-local.sh      # сам поднимет Postgres через docker compose, если он доступен
   # или
   mvn spring-boot:run
   ```
4) UI и API
   - Статика: `http://localhost:8080/app/login.html` (вход/регистрация/восстановление пароля, дашборд).
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/health` или `/actuator/health`

## Auth-флоу
- Регистрация/вход: токены `FG_AUTH` и `FG_REFRESH` в httpOnly cookie (по умолчанию `secure=true`, SameSite настраивается через `app.security.jwt.cookie-samesite`); фронт хранит лишь email в `sessionStorage`.
- Вход может требовать подтверждённый email (`app.security.auth.require-email-verified`, по умолчанию выключено; при включении неверифицированные получают 403, код `100006`).
- Refresh: `POST /api/auth/refresh`
- Верификация email: `POST /api/auth/verify/request`, `POST /api/auth/verify` (dev-коды поддерживаются, но на проде выключайте/переопределяйте)
- Восстановление пароля (двухшаговый, без прямой смены по коду):
  1) `POST /api/auth/forgot` — письмо/код.
  2) `POST /api/auth/reset/confirm` — принимает код, выдаёт короткоживущий `resetSessionToken` (1 на пользователя, TTL ~10–15 мин, привязка IP/UA, отдельные rate limits).
  3) `POST /api/auth/reset` — принимает `resetSessionToken` + новый пароль, инвалидация всех refresh-сессий.
- OTP (опционально): после успешного пароля выдаётся challenge 202; лимиты на выдачу по email+IP, повторный вход в окне действия возвращает 202 без пересылки кода.
- CORS: задайте `ALLOWED_ORIGINS` для SPA на другом домене, включено `credentials: true`.
- Rate limit: фильтр по IP (`AuthRateLimitFilter`), доп. лимиты по email/IP в сервисе; за прокси включите `app.security.trust-proxy-headers=true` для чтения `X-Forwarded-For`.

## Frontend
- Статический клиент (русский): `theme.js`, `api.js`, `auth.js`, `dashboard.js`, `recover.js`; вкладки «Вход/Регистрация», формы для восстановления пароля, дашборд с проверкой health.
- SPA (React+TS, Vite): аналогичный флоу; запуск — `cd frontend && npm install && npm run dev` (прокси на бэк для `/api/**`, `/health`, `/actuator`, `/swagger-ui`). Детали: `frontend/README.md`.

## Статус
Собран каркас Spring Boot 3.2.5 с Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check, миграции V1 (users/accounts/categories/transactions) + V2/V3/V4 (токены/сессии + reset-сессии), JWT security и Auth API (register/login/refresh/verify/reset с session-token), статический клиент (русский UI) и Swagger UI. Далее — CRUD для Accounts/Categories/Transactions и отчёты, перенос токена в httpOnly cookies для SPA.
