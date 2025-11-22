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
3) Запуск: `mvn spring-boot:run`
4) Health: `GET http://localhost:8080/health` или `/actuator/health`

## Документация
- Полный план и доменные модели: `docs/PROJECT_PLAN.md`

## Статус
Каркас Spring Boot 3.2.5: зависимости Web/Security/Data JPA/Validation/Scheduling/Actuator/Flyway/PostgreSQL, Docker Compose для Postgres, базовый `application.yaml`, health-check контроллер. Следующий шаг — миграции и своя security/JWT, затем модули Auth/Accounts/Transactions по плану.
