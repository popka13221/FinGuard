package com.yourname.finguard.auth.controller;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.AuthTokens;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.dto.ResetPasswordRequest;
import com.yourname.finguard.auth.dto.ResetSessionResponse;
import com.yourname.finguard.auth.dto.ValidateResetTokenRequest;
import com.yourname.finguard.auth.dto.UserProfileResponse;
import com.yourname.finguard.auth.dto.ForgotPasswordRequest;
import com.yourname.finguard.auth.dto.VerifyRequest;
import com.yourname.finguard.auth.dto.OtpVerifyRequest;
import com.yourname.finguard.auth.dto.OtpChallengeResponse;
import com.yourname.finguard.auth.service.AuthService;
import com.yourname.finguard.auth.service.UserTokenService;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.dto.ApiError;
import com.yourname.finguard.security.RateLimiterService;
import com.yourname.finguard.security.JwtTokenProvider;
import com.yourname.finguard.security.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Регистрация, логин, refresh и управление сессиями")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientIpResolver clientIpResolver;
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final RateLimiterService rateLimiterService;
    private final UserTokenService userTokenService;
    private final int forgotLimit;
    private final long forgotWindowMs;

    public AuthController(AuthService authService,
                          JwtTokenProvider jwtTokenProvider,
                          RateLimiterService rateLimiterService,
                          UserTokenService userTokenService,
                          ClientIpResolver clientIpResolver,
                          @Value("${app.security.jwt.cookie-secure:true}") boolean cookieSecure,
                          @Value("${app.security.jwt.cookie-samesite:Lax}") String cookieSameSite,
                          @Value("${app.security.rate-limit.forgot.limit:5}") int forgotLimit,
                          @Value("${app.security.rate-limit.forgot.window-ms:300000}") long forgotWindowMs) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.clientIpResolver = clientIpResolver;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = (cookieSameSite == null || cookieSameSite.isBlank()) ? "Lax" : cookieSameSite;
        this.rateLimiterService = rateLimiterService;
        this.userTokenService = userTokenService;
        this.forgotLimit = forgotLimit;
        this.forgotWindowMs = forgotWindowMs;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает учетную запись, выдает access/refresh JWT и ставит httpOnly cookies FG_AUTH/FG_REFRESH")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или слабый пароль", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email уже занят", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        AuthTokens tokens = authService.register(request, ip);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход", description = "Проверяет email/пароль, выдает access/refresh JWT и ставит httpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "202", description = "Требуется OTP"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Аккаунт временно заблокирован", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .info("AuthController login start email={}, ip={}", request.email(), ip);
        AuthService.LoginOutcome outcome = authService.login(request, ip);
        if (outcome.otpRequired()) {
            return ResponseEntity.accepted()
                    .body(new OtpChallengeResponse(true, outcome.otpExpiresInSeconds()));
        }
        AuthTokens tokens = outcome.tokens();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @GetMapping("/me")
    @Operation(summary = "Профиль текущего пользователя", description = "Возвращает email, имя, базовую валюту и роль")
    @ApiResponse(responseCode = "200", description = "Профиль получен")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        UserProfileResponse profile = authService.profile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/verify/request")
    @Operation(summary = "Запрос кода верификации email", description = "Отправляет письмо с кодом для подтверждения адреса")
    @ApiResponse(responseCode = "200", description = "Письмо отправлено")
    public ResponseEntity<Void> requestVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    @Operation(summary = "Подтверждение email", description = "Проверяет код верификации и помечает email подтвержденным")
    @ApiResponse(responseCode = "200", description = "Email подтвержден")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyRequest request) {
        authService.verify(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/otp")
    @Operation(summary = "Завершение входа по OTP", description = "Принимает email+OTP код, выдает access/refresh JWT и ставит cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Вход завершен"),
            @ApiResponse(responseCode = "401", description = "Неверный или истекший OTP", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        AuthTokens tokens = authService.verifyOtp(request, ip);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/forgot")
    @Operation(summary = "Запрос кода для сброса пароля", description = "Отправляет код на email. Ограничено rate limit по IP и email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код отправлен"),
            @ApiResponse(responseCode = "429", description = "Слишком много попыток", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email().trim().toLowerCase();
        String ip = clientIpResolver.resolve(httpRequest);
        String ipKey = "forgot:ip:" + ip;
        String emailKey = "forgot:email:" + email;
        RateLimiterService.Result ipRes = rateLimiterService.check(ipKey, forgotLimit, forgotWindowMs);
        RateLimiterService.Result emailRes = rateLimiterService.check(emailKey, forgotLimit, forgotWindowMs);
        long cooldownMs = userTokenService.getResetCooldown().toMillis();
        RateLimiterService.Result cooldownIpRes = cooldownMs > 0
                ? rateLimiterService.check("forgot:cooldown:ip:" + ip, 1, cooldownMs)
                : new RateLimiterService.Result(true, 0);
        RateLimiterService.Result cooldownEmailRes = cooldownMs > 0
                ? rateLimiterService.check("forgot:cooldown:email:" + email, 1, cooldownMs)
                : new RateLimiterService.Result(true, 0);
        if (!ipRes.allowed() || !emailRes.allowed() || !cooldownIpRes.allowed() || !cooldownEmailRes.allowed()) {
            long retryMs = Math.max(
                    Math.max(ipRes.retryAfterMs(), emailRes.retryAfterMs()),
                    Math.max(cooldownIpRes.retryAfterMs(), cooldownEmailRes.retryAfterMs())
            );
            long retrySec = (long) Math.ceil(retryMs / 1000.0);
            return ResponseEntity.status(429)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(retrySec, 1)))
                    .body(new ApiError(ErrorCodes.RATE_LIMIT, "Слишком много запросов. Попробуйте позже.", Math.max(retrySec, 1)));
        }
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If this email exists, we've sent a reset code."));
    }

    @PostMapping({"/reset/confirm", "/reset/check"})
    @Operation(summary = "Подтверждение кода сброса", description = "Принимает код из письма, возвращает resetSessionToken с TTL")
    public ResponseEntity<ResetSessionResponse> confirmReset(@Valid @RequestBody ValidateResetTokenRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        ResetSessionResponse response = authService.confirmResetToken(request, ip, httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Operation(summary = "Смена пароля по resetSessionToken", description = "Принимает resetSessionToken и новый пароль. Инвалидирует refresh-сессии пользователя.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        authService.resetPassword(request, ip, httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление access/refresh", description = "Берет refresh из cookie FG_REFRESH, выдает новый набор токенов и ставит cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токены обновлены"),
            @ApiResponse(responseCode = "401", description = "Refresh отсутствует или невалиден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refresh = readCookie(request, "FG_REFRESH");
        if (!StringUtils.hasText(refresh)) {
            return ResponseEntity.status(401).body(new ApiError(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", null));
        }
        AuthTokens tokens = authService.refresh(refresh);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse("ok"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход", description = "Стирает cookie FG_AUTH/FG_REFRESH, ревокирует токены/сессии")
    @ApiResponse(responseCode = "200", description = "Выход выполнен")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String access = readCookie(request, "FG_AUTH");
        String refresh = readCookie(request, "FG_REFRESH");
        if (StringUtils.hasText(access)) {
            authService.revokeToken(access);
        }
        if (StringUtils.hasText(refresh)) {
            authService.revokeToken(refresh);
            // refresh jti is removed inside revokeToken via blacklist; also remove session
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expireAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, expireRefreshCookie().toString())
                .build();
    }

    @GetMapping("/csrf")
    @Operation(summary = "CSRF токен", description = "Возвращает CSRF токен и устанавливает cookie XSRF-TOKEN")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok(Map.of("token", token.getToken()));
    }

    private ResponseCookie buildAccessCookie(String token) {
        long maxAge = authService.tokenTtlSeconds();
        return ResponseCookie.from("FG_AUTH", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite(cookieSameSite)
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        long maxAge = jwtTokenProvider.getRefreshValiditySeconds();
        return ResponseCookie.from("FG_REFRESH", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite(cookieSameSite)
                .build();
    }

    private ResponseCookie expireAccessCookie() {
        return ResponseCookie.from("FG_AUTH", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    private ResponseCookie expireRefreshCookie() {
        return ResponseCookie.from("FG_REFRESH", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
