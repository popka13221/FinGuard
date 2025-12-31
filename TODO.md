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
- [x] Зафиксировать, что регистрация считается завершённой после `/api/auth/verify` (зафиксировано в README).

## Infra / GitHub
- [x] GitHub Actions CI: `mvn test` — `.github/workflows/ci.yml`.
- [x] Подчистить статику: удалить дубли из `src/main/resources/static/app/assets/`, урезать `src/main/resources/static/crypto/` до реально используемых иконок.
- [ ] Dependabot (version updates) — сейчас отключено (удалён `.github/dependabot.yml`), включить при желании.
- [x] Добавить CodeQL (`.github/workflows/codeql.yml`) для Java.
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

## Recently done
- [x] Двухшаговая регистрация: пока не введён код — аккаунта в `users` нет (`pending_registrations`).
