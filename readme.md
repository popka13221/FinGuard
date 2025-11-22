# FinGuard — Personal Finance & Alerts Platform

Платформа для учёта личных финансов, целей и умных алертов по тратам, курсам валют и активам. Проект задуман как демонстрация продуманной доменной модели, работы с деньгами/датами/валютами, фоновых задач и интеграций с внешними API.

## Что уже продумано
- Чёткое MVP: пользователи и JWT, счета, категории, транзакции с пересчётом баланса, отчёты по периодам, курсы валют, простые правила с уведомлениями.
- Расширения: цели накоплений, интеграции с курсами валют/криптой и кешированием, продвинутые правила, Email/Telegram нотификации, вынос rule-engine в отдельный сервис и очередь сообщений.
- Доменная модель и бизнес-формулы (балансы, конвертация валют, цели, проверки правил) описаны и готовы к реализации.

## Технологический стек
- Java 17+, Spring Boot (Web, Security, Data JPA, Validation, Scheduling)
- PostgreSQL + Flyway
- Maven/Gradle, Docker Compose для Postgres + приложения
- Опционально далее: OpenFeign/WebClient, Kafka/RabbitMQ

## Архитектура (слоёный монолит)
Controller → Service → Repository → Domain/Model + Config/Security/Scheduler. Предложенная структура пакетов лежит в каркасе `src/main/java/com/yourname/finguard/...` (создана пустая иерархия).

## Быстрый старт
1. Запусти Postgres: `docker compose up -d`.
2. Убедись в доступности БД (по умолчанию `jdbc:postgresql://localhost:5432/finguard`, пользователь/пароль `finguard` — настраивается через env).
3. Запусти приложение: `mvn spring-boot:run`.
4. Health-check: `GET /health` или `GET /actuator/health`.

## Документация и план
- Полный план разработки и модели: `docs/PROJECT_PLAN.md`.
- README будет дополняться по мере появления кода, Swagger-скриншотов и примеров запросов/ответов.

## Оценка текущего состояния
Создан каркас Spring Boot (3.2.5) с зависимостями Web/Security/Data JPA/Validation/Scheduling/Actuator, Flyway и PostgreSQL. Добавлен Docker Compose для Postgres, базовая конфигурация `application.yaml` и health-check контроллер. Следующий шаг — реализовать функциональные модули по плану (Auth, Accounts, Transactions и т.д.).
