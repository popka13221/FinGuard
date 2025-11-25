# FinGuard — Personal Finance & Alerts Platform

Платформа для учёта личных финансов, целей и умных алертов по тратам, курсам валют и активам. Цель — показать продуманную доменную модель, работу с деньгами/датами/валютами, фоновые задачи и интеграции.

## Highlights
- MVP: пользователи + JWT, счета, категории, транзакции с пересчётом баланса, отчёты по периодам, курсы валют, простые правила с уведомлениями.
- Рост: цели накоплений, интеграции с FX/криптой и кешированием, продвинутые правила, Email/Telegram, вынос rule-engine и очередь сообщений.
- Архитектура: слоёный монолит (Controller → Service → Repository → Domain/Model + Config/Security/Scheduler).

## Стек
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling, Actuator)
- PostgreSQL + Flyway
- Maven, Docker Compose (Postgres)
- Далее: OpenFeign/WebClient, Kafka/RabbitMQ при нужде

## Быстрый старт
1) База
   - Docker: `docker compose up -d`
   - или локально (brew): установить Postgres, создать БД/пользователя `finguard`/`finguard`.
2) Переменные (пример):
   ```
   export DB_HOST=localhost DB_PORT=5432 DB_NAME=finguard DB_USER=finguard DB_PASSWORD=finguard
   ```
3) Запуск: `./scripts/run-local.sh` (сам поднимет Postgres через docker compose, если он установлен) или напрямую `mvn spring-boot:run`
4) UI:
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Статический клиент (Вход/Регистрация → Дэшборд): `http://localhost:8080/app/login.html`
   - SPA (React+TS, Vite): `frontend/` — `cd frontend && npm install && npm run dev` (прокси на бэк для `/api/**`, `/health`, `/actuator`, `/swagger-ui`)
   - Токен только в httpOnly cookie (`FG_AUTH`), фронт его не читает; хранится лишь email (для отображения) в sessionStorage
5) Health: `GET http://localhost:8080/health` или `/actuator/health`

## Документация
- Полный план и доменные модели: `docs/PROJECT_PLAN.md`
- Auth/flow детали: `docs/AUTH_FLOW.md`
- TODO/Frontend: `TODO.md`, `docs/TODO_SPA.md`

## Frontend
- Статика: русский UI, вкладки «Вход/Регистрация» и дашборд; минимальная клиентская валидация (обязательность + формат email), остальное — по кодам бэка. Модули: `theme.js`, `api.js`, `auth.js`, `dashboard.js`.
- SPA (React+TS, Vite): повторяет флоу статики, использует httpOnly cookie, прокси на бэк настроен в `frontend/vite.config.ts`.
- Тема (светлая/тёмная) переключается кнопкой «Тема».

## Статус
Собран каркас Spring Boot 3.2.5 с Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check, миграция V1 (users/accounts/categories/transactions), JWT security и Auth API (register/login), статический клиент (русский UI) для входа/регистрации и дэшборда, Swagger UI. Следующий шаг — CRUD для Accounts/Categories/Transactions и отчёты; позже — перенос токена в httpOnly cookies.
