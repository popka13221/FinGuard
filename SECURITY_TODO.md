# FinGuard — Security TODO

Цель: довести проект до уровня “можно показывать людям” без очевидных дыр (XSS/OTP/токены/конфиги), плюс защитить от злоупотреблений (rate-limit/quota/таймауты).

## P0 (критично)
- [ ] Убрать фиксированный OTP `654321` из прод-потока: `OtpService.generateCode()` всегда возвращает фикс-код → заменить на `SecureRandom` + оставлять fixed-code только под `test/e2e/local` профили или флаг (`src/main/java/com/myname/finguard/security/OtpService.java`).
- [ ] Закрыть XSS в статическом UI: сейчас много `innerHTML = \`...\`` с пользовательскими строками (account name, wallet label, etc). Перевести рендеринг на DOM API (`textContent`) или централизованный `escapeHtml()` и применять везде (`src/main/resources/static/app/dashboard.js`, `src/main/resources/static/app/auth.js`, `src/main/resources/static/app/recover.js`, и т.д.).
- [ ] Не возвращать access token в JSON для браузерного флоу (мы и так используем httpOnly cookies). Сейчас `AuthController` отдаёт `new AuthResponse(tokens.accessToken())` → это делает XSS намного опаснее. Варианты: убрать токен из body для web, или включать только для “API clients” по флагу/заголовку (`src/main/java/com/myname/finguard/auth/controller/AuthController.java`).
- [ ] Ограничить “цену стейблкоинов по символу”: сейчас для ETH токенов есть fallback `USDT/USDC/... => 1 USD` даже без цены → можно накрутить value фейковым токеном с символом “USDT”. Сделать whitelist по **адресам контрактов** (по сетям) или убрать fallback полностью (`src/main/java/com/myname/finguard/crypto/service/HttpEthplorerWalletPortfolioProvider.java`).
- [ ] Добавить таймауты/лимиты на внешние HTTP вызовы `RestClient` (BTC/ETH/Arbitrum/Blockscout/Llama/Ethplorer): сейчас нет явных connect/read timeouts → риск подвеса потоков и деградации под нагрузкой.
- [ ] Пересмотреть public surface: `swagger-ui` и `v3/api-docs` сейчас `permitAll()` → для прод окружения лучше закрыть/спрятать за auth/флагом (`src/main/java/com/myname/finguard/security/SecurityConfig.java`).

## P1 (важно)
- [ ] Rate-limit + quotas для crypto endpoints: защита от спама адресами/вызовов внешних провайдеров (`/api/crypto/wallets` list/create/delete). Например: лимит кошельков на пользователя, лимит операций/мин, лимит “обновлений баланса” на адрес.
- [ ] Нормализовать/валидировать все user-facing строки на backend (имя аккаунта/кошелька) и/или хранить отдельное “display-safe” поле (не заменяет фронтовую защиту от XSS, но снижает риск).
- [ ] Явно задокументировать/показывать в UI, что адреса кошельков отправляются внешним провайдерам (privacy), и дать возможность отключать провайдеры/ключи через конфиг.
- [ ] Свести к одному механизму выдачи кодов: сейчас есть и `OtpService` (фикс) и `UserTokenService` (random/fixed via property) → выбрать один подход и убрать дублирование.
- [ ] Добавить “security regression” тесты на XSS (хотя бы unit-тест `escapeHtml` + e2e с вводом `<img onerror=...>` и проверкой, что не исполняется).

## P2 (хорошо бы)
- [ ] Строже контролировать `trust-proxy-headers`: включать только за reverse-proxy и только с правильной настройкой trusted proxies (`src/main/java/com/myname/finguard/security/ClientIpResolver.java`).
- [ ] Security hardening заголовков (опционально): добавить `Cross-Origin-Opener-Policy`, `Cross-Origin-Resource-Policy`, `Cross-Origin-Embedder-Policy` (если не ломает статику/встраивания), и расширить тесты.
- [ ] Аудит логов: убедиться, что нигде не логируются токены/коды/пароли (маскирование уже частично есть, но пройтись поиском).
- [ ] Observability: метрики по rate-limit, ошибкам внешних провайдеров, времени ответа (помогает ловить атаки/деградации).

