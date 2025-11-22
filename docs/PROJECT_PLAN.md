# FinGuard — Project Plan

## Vision
Платформа для учёта личных финансов, целей и умных алертов по тратам, курсам валют и активам.

## Цели
- Для разработчика: показать продуманную доменную модель, работу с деньгами/датами/валютами, фоновые задачи, интеграции.
- Для работодателя: увидеть чистый backend, владение финансовой логикой и архитектурным подходом, умение работать со stateful-системой.

## MVP (v1)
- **Аутентификация**: регистрация, логин, JWT, роли USER/ADMIN (на будущее).
- **Счета**: CRUD, валюта, начальный баланс, текущий баланс по транзакциям.
- **Категории**: базовые (Еда, Транспорт, Подписки, Инвестиции, Прочее) + пользовательские.
- **Транзакции**: доход/расход, дата, категория, счёт, комментарий; пересчёт баланса.
- **Отчёты**: суммы за период (неделя/месяц/диапазон), топ категорий, net cash flow.
- **Курсы валют**: хранение и выдача текущих курсов, конвертация балансов в базовую валюту.
- **Правила + алерты**: лимит расходов по категории за месяц; планировщик создаёт уведомления.
- **Уведомления**: список, статус прочитано/непрочитано.

## Расширения
- **v2**: цели накоплений (прогресс, requiredMonthly), интеграции с FX/BTC/ETH API с кешированием, продвинутые правила (курс/активы/комбинированные), Email/Telegram.
- **v3**: выделение rule-engine в сервис, очередь сообщений (Kafka/RabbitMQ) между сервисами, idempotency, observability.

## Архитектура и стек
- **Стек**: Java 17+, Spring Boot (Web, Security, Data JPA, Scheduling, Validation), PostgreSQL, Flyway, Maven/Gradle, Docker Compose; для v2/v3 — OpenFeign/WebClient, Kafka/RabbitMQ при необходимости.
- **Подход**: слоёный монолит (controller → service → repository → domain/model + config/security/scheduler). Далее возможен вынос rule-engine в отдельный модуль/сервис.
- **Пакеты**: `config`, `security`, `auth`, `accounts`, `categories`, `transactions`, `reports`, `goals`, `fx`, `rules`, `notifications`, `common` (`exception`, `dto`, `util`).

## Доменная модель
- **User**: id, email, passwordHash, fullName, baseCurrency, createdAt.
- **Account**: id, user, name, currency, initialBalance, currentBalance (денорм), createdAt, isArchived.
- **Category**: id, user (nullable для глобальных), name, type (Expense/Income/Both).
- **Transaction**: id, user, account, category, type (INCOME/EXPENSE), amount, currency, transactionDate, description, createdAt.
- **FXRate**: id, baseCurrency, quoteCurrency, rate, asOf (индекс по base/quote/asOf).
- **Goal**: id, user, name, targetAmount, currency, targetDate, currentSavedAmount, createdAt, status.
- **Rule**: id, user, type (SPENDING_LIMIT_CATEGORY, EXCHANGE_RATE_THRESHOLD, ASSET_PRICE_CHANGE ...), parameters (JSON), isActive, createdAt, lastCheckedAt.
- **Notification**: id, user, rule, message, createdAt, read.

## Ключевая бизнес-логика
- **Балансы**: при транзакции обновлять `account.currentBalance` (INCOME +amount, EXPENSE -amount); все операции под @Transactional.
- **Отчёты**: SUM по типу/периоду, net_cash_flow = income - expenses, разбивка по категориям.
- **Конвертация валют**: amountInBase = amount * rate(CUR → base), где rate = quotePerBase.
- **Цели**: `progress = currentSavedAmount / targetAmount`; `requiredMonthly = (targetAmount - currentSavedAmount) / monthsLeft`.
- **Правила** (пример SPENDING_LIMIT_CATEGORY): вычислить расходы по категории за период, конвертировать при необходимости, при превышении лимита создать Notification; планировщик гоняет активные правила.

## API набросок
- **Auth**: POST /api/auth/register, /api/auth/login.
- **Accounts**: GET/POST /api/accounts, GET/PUT/DELETE /api/accounts/{id}.
- **Categories**: GET/POST /api/categories, PUT/DELETE /api/categories/{id}.
- **Transactions**: GET /api/transactions?from=&to=&accountId=&categoryId=..., POST /api/transactions, PUT/DELETE /api/transactions/{id}.
- **Reports**: GET /api/reports/summary?period=MONTH&from=&to=, GET /api/reports/by-category?period=MONTH, GET /api/reports/cash-flow?from=&to=.
- **FX**: GET /api/fx/rates?base=USD&quote=RUB, POST /api/fx/rates (admin), автоподтяжка позже.
- **Goals**: GET/POST /api/goals, GET/PUT/DELETE /api/goals/{id}.
- **Rules**: GET/POST /api/rules, PUT/DELETE /api/rules/{id}.
- **Notifications**: GET /api/notifications, POST /api/notifications/{id}/read.

## Нефункциональные требования
- Безопасность: JWT, BCrypt пароли, доступ к чувствительным операциям только авторизованным.
- Тестирование: unit на сервисы, интеграционные MockMvc на контроллеры.
- Производительность: индексы (user_id, dates, category_id), пагинация списков.
- Надёжность: @Transactional в критичных операциях.

## Дорожная карта реализации
1) Каркас: Spring Boot, Web/Security/Data JPA/Validation/Scheduling, Postgres, Flyway, health-check.
2) Auth: сущность User, регистрация/логин, JWT, базовая защита /api/**.
3) Accounts/Categories/Transactions: CRUD + пересчёт баланса, пара тестов.
4) Reports: сервис агрегаций + эндпоинты summary/by-category/cash-flow.
5) FX: сущность FXRate, API записи/получения, утилита конвертации.
6) Rules/Notifications: сущности, проверка простого правила, scheduler, эндпоинты уведомлений.
7) Goals: сущность, CRUD, сервис расчётов прогресса и requiredMonthly.
8) Косметика: README, Swagger скрины, пример запросов/ответов, ER-диаграмма, релизы.

## Следующие шаги
- Запустить инициализацию Spring Boot проекта, подключить Postgres/Flyway.
- Описать baseCurrency и денормализованный currentBalance в схемах и миграциях.
- Настроить JWT security и шаблоны контроллеров под вышеуказанные пакеты.
