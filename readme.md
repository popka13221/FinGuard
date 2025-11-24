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
   - Клиент (Вход/Регистрация → Дэшборд): `http://localhost:8080/app/login.html`
   - Токен хранится в sessionStorage (временная мера; переход на httpOnly cookies запланирован)
5) Health: `GET http://localhost:8080/health` или `/actuator/health`

## Документация
- Полный план и доменные модели: `docs/PROJECT_PLAN.md`
 - TODO/Frontend: `TODO.md`

## Frontend (статический клиент)
- Русский UI, вкладки «Вход/Регистрация» и дашборд; минимальная клиентская валидация (обязательность + формат email), остальное — по кодам бэка.
- JS модули: `theme.js` (тема), `api.js` (токен/email в sessionStorage, вызовы API), `auth.js` (логин/регистрация), `dashboard.js` (health/logout/отображение токена).
- Тема (светлая/тёмная) переключается кнопкой «Тема».
- План: переход на httpOnly cookies для токенов после доработки бэка.

## Статус
Собран каркас Spring Boot 3.2.5 с Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check, миграция V1 (users/accounts/categories/transactions), JWT security и Auth API (register/login), статический клиент (русский UI) для входа/регистрации и дэшборда, Swagger UI. Следующий шаг — CRUD для Accounts/Categories/Transactions и отчёты; позже — перенос токена в httpOnly cookies.
