# FinGuard: Wow-онбординг после подключения кошелька

## Цель
Пользователь подключает кошелек и в первые 30-60 секунд получает не пустой дашборд, а полноценную аналитику: стоимость портфеля, динамику, траты, recurring/подписки, аномалии и персональные инсайты.

## KPI успеха
- Time-to-First-Value (TTFV): <= 3 сек после успешного подключения кошелька.
- Time-to-Meaningful-Insights: <= 15 сек (первые 3-5 инсайтов).
- Time-to-Full-Analysis: <= 60 сек (история + recurring + риск-сигналы).
- Bounce в первые 2 минуты после wallet connect: снижение минимум на 20%.

## Область работ
- In scope:
  - Wallet bootstrap pipeline (ingest -> enrich -> aggregate -> insights).
  - Новый UX-поток "Instant Value" на dashboard.
  - API для прогресса анализа и выдачи готовых инсайтов.
  - Базовая модель recurring/подписок и категорий расходов.
- Out of scope:
  - Переписывание существующей auth-модели.
  - Полная BI-система и ML-классификация на первом этапе.

## Принципы реализации
- Сначала мгновенная ценность, потом глубокая аналитика.
- Никаких блокирующих sync-операций после `add wallet`.
- Progressive disclosure: данные появляются блоками по мере готовности.
- Минимум текста, максимум полезных метрик и коротких выводов.
- У каждого инсайта есть `confidence` и источник, чтобы не "галлюцинировать".

## Фаза 0 — Контракты и модель (1-2 дня)
### 0.1 Доменные сущности
- `wallet_bootstrap_job`:
  - `id`, `user_id`, `wallet_id`, `status` (`QUEUED|RUNNING|PARTIAL|DONE|FAILED`), `progress_pct`, `stage`, `started_at`, `finished_at`, `error`.
- `wallet_tx_raw`:
  - сырые транзакции/ивенты по кошельку.
- `wallet_tx_enriched`:
  - `tx_hash`, `timestamp`, `counterparty`, `direction`, `asset`, `amount`, `amount_usd`, `category`, `is_recurring_candidate`.
- `wallet_daily_snapshot`:
  - `date`, `portfolio_usd`, `net_flow_usd`, `pnl_usd`, `pnl_pct`.
- `wallet_insight`:
  - `type`, `title`, `value`, `meta_json`, `confidence`, `as_of`.

### 0.2 API-контракты
- `POST /api/crypto/wallets` (existing): после создания запускает bootstrap job.
- `GET /api/crypto/wallets/{id}/analysis/status`:
  - `{ status, progressPct, stage, startedAt, updatedAt, partialReady }`
- `GET /api/crypto/wallets/{id}/analysis/summary`:
  - quick summary: total value, 24h/7d deltas, asset allocation.
- `GET /api/crypto/wallets/{id}/analysis/insights`:
  - список инсайтов по мере готовности.
- `GET /api/crypto/wallets/{id}/analysis/series?range=7d|30d|90d`
  - динамика портфеля/flow.

### 0.3 Стадии job
- `FETCH_TX`
- `ENRICH_TX`
- `BUILD_SNAPSHOTS`
- `DETECT_RECURRING`
- `BUILD_INSIGHTS`
- `DONE`

## Фаза 1 — Instant Value (MVP, 3-5 дней)
### 1.1 Backend
- После `add wallet`:
  - сохраняем кошелек;
  - создаем `wallet_bootstrap_job` в `QUEUED`;
  - запускаем async worker.
- Быстрый слой summary:
  - текущая стоимость портфеля;
  - доля активов;
  - 24h и 7d delta (если доступны провайдером/кэшем).

### 1.2 Frontend Dashboard
- Новый "analysis bootstrap banner" в `dashboard.html`:
  - "Analyzing your wallet" + progress bar + stage label.
- Новые компактные блоки (минималистичные):
  - `Portfolio Value`
  - `7d Growth`
  - `Top Outflow`
  - `Recurring Spend (est.)`
- Polling статуса каждые 2 сек до `DONE|FAILED`.

### 1.3 UX-анимация
- При появлении каждой метрики:
  - reveal + легкий numeric count-up.
- Никаких тяжелых layout-анимаций.

### 1.4 Acceptance
- После успешного подключения кошелька пользователь видит первые meaningful-данные <= 3 сек.
- При `RUNNING` состояние не выглядит "пустым".

## Фаза 2 — Meaningful Insights (5-7 дней)
### 2.1 Enrichment
- Нормализация контрагентов.
- Определение `direction` (`IN|OUT|SELF`).
- Категоризация расходов:
  - transfer, swap, bridge, exchange, subscription-like, fees, other.

### 2.2 Recurring detection (rule-based v1)
- Критерии:
  - >=3 платежа одному контрагенту;
  - период 26-33 дня или 6-8 дней;
  - разброс суммы <= 20%.
- Выход:
  - `next_estimated_charge_at`, `avg_amount`, `confidence`.

### 2.3 Insight engine
- Инсайты v1:
  - "Вы тратите X USD/мес на recurring."
  - "Крупнейший отток: category Y."
  - "Портфель вырос/снизился на Z% за 30d."
  - "N аномальных списаний за 7d."

### 2.4 Acceptance
- У каждого инсайта есть `confidence` и `as_of`.
- Минимум 3 инсайта для активного кошелька.

## Фаза 3 — Full Analysis (7-10 дней)
### 3.1 Историческая динамика
- Ежедневные snapshots за 90d.
- Графики:
  - value curve,
  - inflow/outflow bars,
  - net growth.

### 3.2 Personal Finance слой
- "Подписки и recurring".
- "Куда уходят деньги" (top categories + trend).
- "Риск-сигналы":
  - внезапный spike outflow,
  - резкие просадки стоимости.

### 3.3 Acceptance
- Полная аналитика готовится <= 60 сек на среднем профиле.
- UI не блокируется, всегда есть промежуточная ценность.

## Фаза 4 — Product polish (3-5 дней)
### 4.1 UX
- Минималистичные карточки:
  - меньше текста,
  - четкая иерархия цифр,
  - единые отступы и scale.
- One-screen summary:
  - главное видно без лишнего скролла на desktop.

### 4.2 Надежность
- Retry для фазы job.
- Idempotent обработка tx.
- Лимиты/дедупликация провайдер-запросов.

### 4.3 Monitoring
- Метрики:
  - `bootstrap_duration_ms`,
  - `bootstrap_fail_rate`,
  - `insights_generated_count`,
  - `ttfv_ms`.

## Технические задачи по файлам
- Backend:
  - `src/main/java/.../crypto/*` — orchestrator + worker + repositories + endpoints.
  - `src/main/resources/db/migration/*` — новые таблицы анализа.
- Frontend:
  - `src/main/resources/static/app/dashboard.html` — блок bootstrap/progress + insight cards.
  - `src/main/resources/static/app/dashboard.css` — минималистичный стиль insight-блоков.
  - `src/main/resources/static/app/dashboard.js` — polling статуса, staged reveal, рендер карточек.
- E2E:
  - `e2e/dashboard-ui-smoke.spec.js` + новый `e2e/dashboard-analysis.spec.js`.

## Тест-план
- Unit:
  - recurring detection rules.
  - insight scoring/confidence.
- Integration:
  - wallet bootstrap lifecycle.
  - partial-ready responses.
- E2E:
  - connect wallet -> progress -> first value <= 3 sec.
  - connect wallet -> full insights visible.
  - fail provider -> graceful fallback + retry CTA.

## Риски и контроль
- Риск: медленные провайдеры/лимиты API.
  - Митигация: staged output + cache + retry/backoff.
- Риск: ложные recurring.
  - Митигация: confidence + conservative thresholds.
- Риск: перегруженный UI.
  - Митигация: максимум 4 primary cards + "show details" по клику.

## План запуска
- PR1: schema + bootstrap job + status API.
- PR2: dashboard progress + instant cards.
- PR3: recurring + insights engine v1.
- PR4: full analysis + polish + monitoring.

## Определение "готово"
- Пользователь после wallet connect не видит пустой дашборд.
- Первые полезные метрики появляются <= 3 сек.
- Полный набор аналитики появляется <= 60 сек.
- e2e/интеграционные тесты зеленые.
