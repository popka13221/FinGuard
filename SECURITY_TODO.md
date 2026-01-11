# FinGuard — Security TODO (Currencies + Crypto wallets)

Скоп: всё, что мы недавно добавили вокруг:
- базовая валюта + конвертация сумм (FX),
- watch-only крипто-кошельки (BTC/ETH/ARBITRUM),
- оценка портфеля через внешние провайдеры (Ethplorer/Blockscout/DefiLlama, JSON-RPC).

## P0 (критично перед продом)
- [x] Закрыть XSS именно в экранах “баланс/кошельки”: `innerHTML = \`...\`` с user-input (название аккаунта, label кошелька) → рендерить через DOM API (`textContent`) или централизованный `escapeHtml()` и применять в `dashboard.js` (и любых страницах, где выводятся эти поля).
- [x] Закрыть XSS в FX блоке дашборда: `renderFxTop/renderFxList/renderFxDetail` используют `innerHTML` и вставляют `item.code/item.name` → экранировать или рендерить через DOM API.
- [x] Убрать “стейблкоин = 1 USD по символу”: сейчас для ETH токенов есть fallback `USDT/USDC/... => 1 USD` даже без цены → можно накрутить value фейковым токеном с символом “USDT”. Решение: whitelist по **адресам контрактов** (и по сети) или убрать fallback полностью (`src/main/java/com/myname/finguard/crypto/service/HttpEthplorerWalletPortfolioProvider.java`).
- [x] Добавить таймауты/лимиты на внешние HTTP вызовы (`RestClient`): BTC/ETH balance, Arbitrum JSON-RPC, Ethplorer, Blockscout, coins.llama.fi, FX (`HttpFxRatesProvider`). Сейчас без явных connect/read timeouts это легко превращается в подвес потоков/DoS.
  - Конфиг: `app.http.connect-timeout-ms`, `app.http.read-timeout-ms`.
- [x] Abuse protection для `/api/crypto/wallets`: лимит количества кошельков на пользователя, rate-limit create/delete, и отдельный лимит на “обновления” (list → внешние провайдеры). Иначе можно выжечь квоты провайдеров и/или уронить сервер.
  - Конфиг: `app.crypto.wallet.max-per-user`, `app.security.rate-limit.wallets.*`, `app.security.rate-limit.public-rates.*`.
- [x] Ограничить/валидировать данные от провайдеров перед использованием:
  - проверять, что token contract address выглядит как `0x[0-9a-fA-F]{40}` (иначе игнор);
  - капнуть `decimals` (например, 0..30) и длину `rawBalance` (иначе игнор), чтобы не ловить BigDecimal/scale DoS;
  - капнуть число токенов, по которым вообще считаем стоимость (до запроса цен), чтобы не собирать URL на тысячи контрактов.
- [x] Ограничить размер in-memory cache’ей (сейчас `ConcurrentHashMap` без max-size): `CryptoWalletBalanceService`, `EthWalletPortfolioService`, `ArbitrumWalletPortfolioService` могут разрастаться на уникальных адресах → добавить `maximumSize`/eviction (например Caffeine) и/или лимит кошельков на пользователя.
  - Конфиг: `app.crypto.wallet.cache-max-entries`, `app.crypto.wallet.eth.portfolio.cache-max-entries`, `app.crypto.wallet.arbitrum.portfolio.cache-max-entries`.

## P1 (важно)
- [x] Ограничить объём работы на “тяжёлых” адресах: Ethplorer может вернуть сотни/тысячи токенов → поставить лимит токенов на обработку/время, и/или кешировать результаты дольше, и/или делать деградацию (“показываем только top-N, остальное — не считаем без premium провайдера”).
  - Конфиг: `app.crypto.wallet.eth.portfolio.max-tokens-scanned`.
- [x] Не логировать адреса целиком на WARN/ERROR (и вообще не логировать ответы провайдеров) — маскировать `0x12…ABCD`/`bc1q…` при необходимости.
  - Утилита: `src/main/java/com/myname/finguard/common/util/Redaction.java`.
- [x] Убрать/замаскировать PII в логах и в ключах rate-limit: email/ip сейчас могут попадать в WARN и в `RateLimitBucket.bucketKey` → логировать `maskEmail`/maskIp и хранить hash ключей.
- [x] Добавить негативные тесты на XSS именно через `label` кошелька / `account.name` (минимум unit для `escapeHtml`, максимум e2e “вставили <img onerror> и не исполнилось”).
- [x] Rate-limit для публичных конвертеров: `GET /api/fx/rates` и `GET /api/crypto/rates` (сейчас публичные) — чтобы не было дешёвого способа дергать внешние FX/CoinGecko и грузить сервер.
- [x] Явно документировать, что “ARBITRUM = ETH как native asset”: в UI и/или в API поле (сейчас можно спутать сеть и монету); добавить тест на отображение/формат.
- [x] Ужесточить CSP/security headers: добавить `frame-ancestors 'none'`, `object-src 'none'`, `base-uri 'self'`; по возможности убрать `style-src 'unsafe-inline'`.
- [x] Прод-режим: закрыть/отключить `/swagger-ui/**`, `/v3/api-docs/**`, `/playground/**` (только dev или только ADMIN).

## P2 (хорошо бы)
- [x] Ввести “provider hardening” слой: retries с backoff + circuit-breaker + отдельные бюджеты по провайдерам (Ethplorer/Blockscout/Llama/FX), чтобы сбой одного API не деградировал весь дашборд.
  - Конфиг: `app.external.providers.*` (см. `docs/EXTERNAL_PROVIDERS.md`).
- [x] Сделать конфигурацию провайдеров более “prod-safe”: запрет дефолтных `freekey`/публичных ключей в проде, отдельные профили, документация по ключам/лимитам.
  - Док: `docs/EXTERNAL_PROVIDERS.md` (и проверка `freekey` для `prod` профиля).
- [x] Для точности/целостности данных (anti-tamper): опционально сверять native ETH balance через 2 источника (например, JSON-RPC и BlockCypher) и логировать расхождение (без падения), чтобы ловить баги/провайдера.
  - Конфиг: `app.crypto.wallet.eth.cross-check.*` (см. `docs/EXTERNAL_PROVIDERS.md`).
- [ ] Автоматизировать security checks в CI: dependency scan (OWASP/Dependabot), SAST (semgrep), базовый DAST (ZAP) на e2e окружении.
