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
- Регистрация/вход: токены `FG_AUTH` и `FG_REFRESH` в httpOnly cookie; фронт хранит лишь email в `sessionStorage`.
- Refresh: `POST /api/auth/refresh`
- Верификация email: `POST /api/auth/verify/request`, `POST /api/auth/verify`
- Восстановление пароля: `POST /api/auth/forgot`, `POST /api/auth/reset` (унифицированные ответы для защиты от enumeration)
- CORS: задайте `ALLOWED_ORIGINS` для SPA на другом домене, включено `credentials: true`.

## Frontend
- Статический клиент (русский): `theme.js`, `api.js`, `auth.js`, `dashboard.js`, `recover.js`; вкладки «Вход/Регистрация», формы для восстановления пароля, дашборд с проверкой health.
- SPA (React+TS, Vite): аналогичный флоу; запуск — `cd frontend && npm install && npm run dev` (прокси на бэк для `/api/**`, `/health`, `/actuator`, `/swagger-ui`). Детали: `frontend/README.md`.
- Тема (светлая/тёмная) переключается кнопкой «Тема».

## Статус
Собран каркас Spring Boot 3.2.5 с Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check, миграции V1 (users/accounts/categories/transactions) + V2/V3 для токенов/сессий, JWT security и Auth API (register/login/refresh/verify/reset), статический клиент (русский UI) и Swagger UI. Далее — CRUD для Accounts/Categories/Transactions и отчёты, перенос токена в httpOnly cookies для SPA.
