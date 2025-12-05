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
import com.yourname.finguard.auth.service.AuthService;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.dto.ApiError;
import com.yourname.finguard.security.RateLimiterService;
import com.yourname.finguard.security.JwtTokenProvider;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Регистрация, логин, refresh и управление сессиями")
public class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimiterService rateLimiterService;
    private final int forgotLimit;
    private final long forgotWindowMs;

    public AuthController(AuthService authService,
                          JwtTokenProvider jwtTokenProvider,
                          RateLimiterService rateLimiterService,
                          @Value("${app.security.jwt.cookie-secure:false}") boolean cookieSecure,
                          @Value("${app.security.rate-limit.forgot.limit:5}") int forgotLimit,
                          @Value("${app.security.rate-limit.forgot.window-ms:300000}") long forgotWindowMs) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieSecure = cookieSecure;
        this.rateLimiterService = rateLimiterService;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokens tokens = authService.register(request);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход", description = "Проверяет email/пароль, выдает access/refresh JWT и ставит httpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Аккаунт временно заблокирован", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);
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

    @PostMapping("/forgot")
    @Operation(summary = "Запрос кода для сброса пароля", description = "Отправляет код на email. Ограничено rate limit по IP и email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код отправлен"),
            @ApiResponse(responseCode = "429", description = "Слишком много попыток", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email().trim().toLowerCase();
        String ipKey = "forgot:ip:" + httpRequest.getRemoteAddr();
        String emailKey = "forgot:email:" + email;
        if (!rateLimiterService.allow(ipKey, forgotLimit, forgotWindowMs)
                || !rateLimiterService.allow(emailKey, forgotLimit, forgotWindowMs)) {
            return ResponseEntity.status(429)
                    .body(new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Try again later."));
        }
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If this email exists, we've sent a reset code."));
    }

    @PostMapping({"/reset/confirm", "/reset/check"})
    @Operation(summary = "Подтверждение кода сброса", description = "Принимает код из письма, возвращает resetSessionToken с TTL")
    public ResponseEntity<ResetSessionResponse> confirmReset(@Valid @RequestBody ValidateResetTokenRequest request, HttpServletRequest httpRequest) {
        ResetSessionResponse response = authService.confirmResetToken(request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Operation(summary = "Смена пароля по resetSessionToken", description = "Принимает resetSessionToken и новый пароль. Инвалидирует refresh-сессии пользователя.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
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
            return ResponseEntity.status(401).body(new ApiError(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token"));
        }
        AuthTokens tokens = authService.refresh(refresh);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
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

    private ResponseCookie buildAccessCookie(String token) {
        long maxAge = authService.tokenTtlSeconds();
        return ResponseCookie.from("FG_AUTH", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        long maxAge = jwtTokenProvider.getRefreshValiditySeconds();
        return ResponseCookie.from("FG_REFRESH", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie expireAccessCookie() {
        return ResponseCookie.from("FG_AUTH", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie expireRefreshCookie() {
        return ResponseCookie.from("FG_REFRESH", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
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
