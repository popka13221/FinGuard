# FinGuard — TODO

Основано на `docs/PROJECT_PLAN.md` (там — полное описание; здесь — рабочий чеклист).

## Vision
Платформа для учёта личных финансов, целей и умных алертов по тратам, курсам валют и активам.

## Цели
- Для разработчика: показать продуманную доменную модель, работу с деньгами/датами/валютами, фоновые задачи, интеграции.
- Для работодателя: увидеть чистый backend, владение финансовой логикой и архитектурным подходом, умение работать со stateful-системой.

## v1 (MVP) — Scope
- [x] Аутентификация: регистрация, логин, JWT.
- [x] Роли: USER/ADMIN (пока только модель и `ROLE_*` authority).
- [x] Счета: CRUD, валюта, начальный баланс, текущий баланс по транзакциям.
- [x] Категории: базовые (Еда, Транспорт, Подписки, Инвестиции, Прочее) + пользовательские.
- [x] Транзакции: доход/расход, дата, категория, счёт, комментарий; пересчёт баланса.
- [x] Отчёты: суммы за период (неделя/месяц/диапазон), топ категорий, net cash flow.
- [x] Курсы валют: хранение и выдача текущих курсов, конвертация балансов в базовую валюту.
- [ ] Правила + алерты: лимит расходов по категории за месяц; планировщик создаёт уведомления.
- [ ] Уведомления: список, статус прочитано/непрочитано.

## Дорожная карта реализации (v1)
- [x] 1) Каркас: Spring Boot, Web/Security/Data JPA/Validation/Scheduling, Postgres, Flyway, health-check.
- [x] 2) Auth: сущность User, регистрация/логин, JWT, базовая защита `/api/**`.
- [x] 3) Accounts/Categories/Transactions: CRUD + пересчёт баланса, тесты.
- [x] 4) Reports: сервис агрегаций + эндпоинты summary/by-category/cash-flow.
- [x] 5) FX: сущность FXRate, API записи/получения, утилита конвертации.
- [ ] 6) Rules/Notifications: сущности, проверка простого правила, scheduler, эндпоинты уведомлений.
  - [ ] DB: миграции `rules`, `notifications` (+ индексы: `user_id`, `created_at`, `read_at`, `rule_id`).
  - [ ] Domain: `RuleType` (минимум `SPENDING_LIMIT_CATEGORY_MONTHLY`), `RuleStatus`/`active`, JSON params.
  - [ ] RuleEvaluator: расчёт трат по категории за месяц (в базовой валюте пользователя), сравнение с лимитом.
  - [ ] Дедуп алертов: не спамить (уникальность по `rule_id + month` или `lastTriggeredAt`).
  - [ ] Scheduler: daily job (или hourly), который проверяет активные правила пачками.
  - [ ] API Rules: CRUD, enable/disable, тесты на доступ только своего пользователя.
  - [ ] API Notifications: список (paging), unread count, mark read/unread, bulk mark read.
  - [ ] UI: бейдж непрочитанных + страница/модалка уведомлений.
  - [ ] Тесты: интеграционные (scheduler → notification), unit на evaluator, security (403/401).
- [ ] 7) Goals: сущность, CRUD, сервис расчётов progress и requiredMonthly.
  - [ ] DB: миграция `goals` (user_id, name, target_amount, currency, target_date, saved_amount, status).
  - [ ] Domain: `GoalStatus` (ACTIVE/PAUSED/DONE), валидации дат/сумм.
  - [ ] Service: `progress`, `requiredMonthly`, пересчёт по датам (UTC), edge cases (targetDate < now).
  - [ ] API: CRUD + эндпоинт “add contribution” (или PATCH saved_amount).
  - [ ] UI: список целей + карточка прогресса + создание/редактирование.
  - [ ] Тесты: unit на расчёты, интеграционные на CRUD.
- [ ] 8) Косметика: README, Swagger скрины, пример запросов/ответов, ER-диаграмма, релизы.
  - [x] README EN/RU: актуальные фичи, запуск, env.
  - [x] Скриншоты: dashboard + swagger (+ landing/login).
  - [x] Примеры API: `docs/API_EXAMPLES.md` и `docs/API_EXAMPLES.ru.md`.
  - [ ] Swagger: добавить/проверить примеры request/response в контроллерах, описать auth/CSRF сценарии.
  - [ ] ER-диаграмма: Mermaid (`docs/ER_DIAGRAM.md`) + ссылка из README.
  - [ ] Release process: semver, changelog, GitHub Releases (шаблон релиза).
  - [ ] “Demo data”: один короткий сценарий в README (что нажать/какие запросы сделать).

## UI / Dashboard backlog (v1)
- [ ] Последние транзакции на dashboard (10–20 строк) + фильтр периодов (7d/30d).
- [ ] Быстрое добавление транзакции (модалка) + автообновление balance/reports.
- [ ] Управление категориями (CRUD UI) и счета (CRUD UI) без “ручных” запросов.
- [ ] Empty states/ошибки: когда нет счетов/транзакций/курсов, понятные CTA.
  - [ ] Показать подсказку “создай счёт / добавь транзакцию” вместо “—”.

## v2 (Extensions)
- [ ] Цели накоплений (progress, requiredMonthly).
- [ ] Интеграции с FX/BTC/ETH API с кешированием.
- [ ] Продвинутые правила (курс/активы/комбинированные).
- [ ] Email/Telegram уведомления.

## v3 (Future)
- [ ] Выделение rule-engine в сервис.
- [ ] Очередь сообщений (Kafka/RabbitMQ), idempotency, observability.
