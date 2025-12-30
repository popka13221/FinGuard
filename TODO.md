# Test coverage to add

## Backend – unit/service
- [x] AccountService.getUserBalance: unauthorized on null user, filters archived from totals, sums per currency, null-safe balances, single repo call.
- [x] AccountController.resolveUserId: resolves UserPrincipal vs UserDetails vs name, throws 401 for anonymous/null, only fetches own user.
- [x] AuthService register/login/otp: generates tokens, enforces unique email, baseCurrency/role defaults, rejects weak password, revokes sessions on logout.
- [x] JwtAuthenticationFilter: skips when no header, blocks expired/blacklisted tokens, loads user with requireEmailVerified=true, populates SecurityContext.
- [x] RateLimiterService/LoginAttemptService: increments buckets, locks when threshold exceeded, resets after window; prevents login when blocked.
- [x] SecurityConfig/AppAccessDeniedHandler: returns 403 JSON with code, respects CSRF on state-changing endpoints.
- [x] CurrencyService: fetch rates success/failure, caches result, handles unsupported currency.
- [x] Category/Transaction repositories: enforce user scoping queries, archived handling.

## Backend – integration/API
- [x] /api/accounts/balance: 401 anonymous, 200 for owner only, excludes other users’ accounts, archived excluded from totals, multi-currency totals sorted.
- [x] /api/auth/login flow: password path 200, otp-required 202 then otp verification, invalid otp 401, session email stored, cookies set.
- [x] /api/auth/register: creates user, sends mail, rejects duplicate, enforces password policy, baseCurrency persistence.
- [x] Rate limiters: registration/login OTP blocked after threshold, returns correct error code.
- [x] Token revocation: logout invalidates token, blacklist prevents reuse, refresh token rotation works.
- [x] CSRF: POST without XSRF when cookie exists returns 403, with token passes (e.g., /api/auth/logout).
- [x] Health/OpenAPI endpoints accessible without auth; secured endpoints require bearer.

## Frontend – unit/component
- [x] AuthPage: login success redirects, otp flow renders second step, disables submit without otp, shows backend errors, stores email to sessionStorage.
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

## Test failures to fix (mvn test)
- [x] Verification flow: `/api/auth/verify` with dev code (123456) fails with incorrect result size; make pending registration lookup unique per dev code (delete existing by hash or replace on create) so verify works without email.
- [x] Login/verify: many auth tests return 401/500; align dev verify/reset tokens (123456) to be issued and accepted, ensure mail outbox contains tokens for tests.
- [x] OTP/login flow: expected 202/200 but returns 401; ensure login requires verification when configured, OTP challenge issued, and verifyOtp accepts dev code.
- [x] Reset flow: tests expect reset token 123456 present and latestResetToken not empty; ensure UserTokenService issues and stores reset dev token, and outbox contains it.
- [x] Rate limit/profile/refresh tests: errors due to auth failures; revisit AuthSecurityIntegrationTest expectations after fixing verify/login.
- [x] AccountBalanceIntegrationTest: avoid null current_balance insertion (use BigDecimal.ZERO or default), expect 403 for anonymous per AppAccessDeniedHandler.
