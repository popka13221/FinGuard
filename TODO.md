# FinGuard — TODO

См. также:
- `docs/PROJECT_PLAN.md` — видение/роадмап.
- `readme.md` — запуск/статус/настройки.

## P0 / Next (ближайшие задачи)
- [x] JaCoCo: подключить покрытие в `pom.xml` + шаг в CI (artifact `jacoco-report`; html/xml).
- [x] Security headers: `SecurityHeadersIntegrationTest` проверяет заголовки на `GET /` и `GET /api/auth/csrf`:
  - `Content-Security-Policy` (строка из `SecurityConfig`)
  - `Referrer-Policy`
  - `Permissions-Policy`
  - `Strict-Transport-Security` (только для HTTPS запроса → в тесте нужно `secure(true)`).
- [x] Cookie flags: тесты на `Set-Cookie` для `FG_AUTH`/`FG_REFRESH` (verify/refresh/logout) в `AuthCookieSecurityIntegrationTest`/`AuthCookieSecureFlagIntegrationTest`:
  - `HttpOnly`, `SameSite`, `Path=/`, `Max-Age`
  - `Secure` — зависит от `app.security.jwt.cookie-secure` (проверить оба режима).
  - На `/api/auth/logout` cookies реально “протухают” (`Max-Age=0`).
- [ ] Email codes: убрать фиксированный код `654321` из “прод” режима (оставить только для тестов/локалки) + обновить тесты/README.
- [ ] Определиться с политикой `app.security.auth.require-email-verified=true`:
  - [ ] либо блокировать доступ к `/api/**` для unverified (кроме `/api/auth/**`),
  - [ ] либо оставить как есть, но явно задокументировать “что можно делать без верификации” + покрыть тестами.
- [x] E2E smoke (Playwright): happy-path `register → verify(654321) → login → dashboard` (`frontend: npm run e2e`).

## Infra / GitHub
- [x] GitHub Actions CI: `mvn test` + `npm ci/build/test` — `.github/workflows/ci.yml`.
- [x] Подчистить статику: удалить дубли из `src/main/resources/static/app/assets/`, урезать `src/main/resources/static/crypto/` до реально используемых иконок.
- [x] Добавить `npm run lint` в CI (и при желании `mvn -B verify` вместо `test`).
- [ ] Dependabot (version updates) — сейчас отключено (удалён `.github/dependabot.yml`), включить при желании.
- [x] Добавить CodeQL (`.github/workflows/codeql.yml`) для Java + TS.
- [x] Добавить Dependency Review (`.github/workflows/dependency-review.yml`) для PR.
- [ ] Включить GitHub Dependency graph (Settings → Security & analysis), иначе Dependency Review падает.
- [x] Добавить CI/CodeQL badge в `readme.md`.

## Security — расширить покрытие (если хочется “как в проде”)
- [ ] CSRF: тест на whitelist (например, что `POST /api/currencies` или “публичные” endpoints не требуют XSRF, а state-changing `/api/**` требует).
- [x] CORS: тесты на allow-headers/methods + что при `credentials=true` не приходит `Access-Control-Allow-Origin: *` (`CorsSecurityIntegrationTest`).
- [x] Auth cookies: refresh принимает только `FG_REFRESH` cookie (а не `Authorization`, и не `FG_AUTH`) (`AuthCookieSecurityIntegrationTest`).

---

## Tests coverage (что уже добавлено)

## Backend – unit/service
- [x] AccountService.getUserBalance: unauthorized on null user, filters archived from totals, sums per currency, null-safe balances, single repo call.
- [x] AccountController.resolveUserId: resolves UserPrincipal vs UserDetails vs name, throws 401 for anonymous/null, only fetches own user.
- [x] AuthService register/login/otp: register создает `pending_registrations` (без записи в `users`), verify создает `User` и выдает токены; login до verify возвращает 403 `100006` если пароль совпадает с pending.
- [x] JwtAuthenticationFilter: skips when no header, ignores invalid/non-access tokens, blocks revoked tokens, checks tokenVersion, supports cookie auth, populates SecurityContext.
- [x] RateLimiterService/LoginAttemptService: increments buckets, locks when threshold exceeded, resets after window; prevents login when blocked.
- [x] SecurityConfig/AppAccessDeniedHandler: returns 403 JSON with code, respects CSRF on state-changing endpoints.
- [x] CurrencyService: fetch rates success/failure, caches result, handles unsupported currency.
- [x] Category/Transaction repositories: enforce user scoping queries, archived handling.

## Backend – integration/API
- [x] /api/accounts/balance: 403 anonymous, 200 for owner only, excludes other users’ accounts, archived excluded from totals, multi-currency totals sorted.
- [x] /api/auth/login flow: password path 200, otp-required 202 then otp verification, invalid otp 401, session email stored, cookies set.
- [x] /api/auth/register → pending: без `users`, шлёт verify-код, возвращает `verificationRequired=true` и без cookies.
- [x] /api/auth/verify: с `email+token` создаёт `User(emailVerified=true)`, удаляет pending, выдает cookies/токены.
- [x] /api/auth/verify/request: переиздаёт код для pending-регистрации (и сохраняет legacy поведение для существующих unverified users).
- [x] Rate limiters: registration/login OTP blocked after threshold, returns correct error code.
- [x] Token revocation: logout invalidates token, blacklist prevents reuse, refresh token rotation works.
- [x] CSRF: POST without XSRF when cookie exists returns 403, with token passes (e.g., /api/auth/logout).
- [x] Health/OpenAPI endpoints accessible without auth; secured endpoints require bearer.

## Frontend – unit/component
- [x] AuthPage: login success redirects, otp flow renders second step, registration поддерживает шаг “код из письма” (register → verify), stores email to sessionStorage.
- [x] Forgot/Reset flow: invalid email shows inline error, successful forgot reveals code input, confirm/reset failures show message, success redirects.
- [x] DashboardPage: displays fetched accounts and totals, handles loading/error/empty state, formats currency per account, add-button toggles overlay and closes on outside click.
- [x] Pie chart (expenses): legend hover highlights matching slice, displays correct totals/percentages, responsive layout (2 columns desktop, 1 mobile) without overflow.
- [x] Balance chart: renders line without points, y-axis labels not overlapping, shows mini-metrics (min/max/change/avg/trend) with correct formatting.

## Frontend – e2e/smoke (if Cypress/Playwright available)
- [ ] Auth happy path: register → login → dashboard reachable, CSRF cookie present.
- [ ] Unauthorized redirect: open /app/dashboard.html without session redirects to login.
- [ ] Add-menu overlay: opens centered, stays above other cards, closes on Escape/outside click.

## Tooling/infra
- [x] Ensure vitest setup runs with jest-dom matchers; add CI job to run `npm run build` and `npm test`.

## Recently done
- [x] CI (GitHub Actions): backend `mvn test`, frontend `npm ci/build/test`.
- [x] Двухшаговая регистрация: пока не введён код — аккаунта в `users` нет (`pending_registrations`).
