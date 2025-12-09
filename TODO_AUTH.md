

## Новые задачи (порядок выполнения)
- [x] Cookies: включено `app.security.jwt.cookie-secure` (дефолт true) для FG_AUTH/FG_REFRESH, добавлена настройка SameSite (`app.security.jwt.cookie-samesite`).
- [x] Email verified: логин/OTP блокируются, если `email_verified=false`, с понятной ошибкой.
- [x] OTP выдача: добавлен IP-лимит на `login-otp-issue`; при активном OTP возвращается 202 с TTL вместо немой 429; 429 с `OTP_ALREADY_SENT` остаётся при попытке выдать новый код сверх лимитов.
- [x] Rate limit за прокси: `ClientIpResolver` учитывает `X-Forwarded-For`/`X-Real-IP` при `app.security.trust-proxy-headers=true` и используется в фильтре/контроллере.
- [x] In-memory состояние: добавлены предупреждения о сбросе состояния при рестарте для TokenBlacklistService/RateLimiterService/LoginAttemptService/OtpService.
- [x] Reset session context: хэш ip/user-agent в reset-сессиях переведён на SHA-256.
