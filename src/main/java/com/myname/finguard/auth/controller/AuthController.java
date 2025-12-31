package com.myname.finguard.auth.controller;

import com.myname.finguard.auth.dto.AuthResponse;
import com.myname.finguard.auth.dto.AuthTokens;
import com.myname.finguard.auth.dto.LoginRequest;
import com.myname.finguard.auth.dto.RegisterRequest;
import com.myname.finguard.auth.dto.ResetPasswordRequest;
import com.myname.finguard.auth.dto.ResetSessionResponse;
import com.myname.finguard.auth.dto.ValidateResetTokenRequest;
import com.myname.finguard.auth.dto.UserProfileResponse;
import com.myname.finguard.auth.dto.ForgotPasswordRequest;
import com.myname.finguard.auth.dto.VerifyRequest;
import com.myname.finguard.auth.dto.OtpVerifyRequest;
import com.myname.finguard.auth.dto.OtpChallengeResponse;
import com.myname.finguard.auth.dto.RegistrationResponse;
import com.myname.finguard.auth.service.AuthService;
import com.myname.finguard.auth.service.UserTokenService;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.dto.ApiError;
import com.myname.finguard.security.RateLimiterService;
import com.myname.finguard.security.JwtTokenProvider;
import com.myname.finguard.security.ClientIpResolver;
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
import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration, login, refresh, and session management")
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
    private final int verifyLimit;
    private final long verifyWindowMs;

    public AuthController(AuthService authService,
                          JwtTokenProvider jwtTokenProvider,
                          RateLimiterService rateLimiterService,
                          UserTokenService userTokenService,
                          ClientIpResolver clientIpResolver,
                          @Value("${app.security.jwt.cookie-secure:true}") boolean cookieSecure,
                          @Value("${app.security.jwt.cookie-samesite:Strict}") String cookieSameSite,
                          @Value("${app.security.rate-limit.forgot.limit:5}") int forgotLimit,
                          @Value("${app.security.rate-limit.forgot.window-ms:300000}") long forgotWindowMs,
                          @Value("${app.security.rate-limit.verify.limit:3}") int verifyLimit,
                          @Value("${app.security.rate-limit.verify.window-ms:300000}") long verifyWindowMs) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.clientIpResolver = clientIpResolver;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = (cookieSameSite == null || cookieSameSite.isBlank()) ? "Lax" : cookieSameSite;
        this.rateLimiterService = rateLimiterService;
        this.userTokenService = userTokenService;
        this.forgotLimit = forgotLimit;
        this.forgotWindowMs = forgotWindowMs;
        this.verifyLimit = verifyLimit;
        this.verifyWindowMs = verifyWindowMs;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a pending registration and sends a verification code by email. "
            + "User and tokens are created after /api/auth/verify confirms the email.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration started (pending registration)"),
            @ApiResponse(responseCode = "400", description = "Invalid input or weak password", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        AuthService.RegistrationResult result = authService.register(request, ip);
        AuthTokens tokens = result.tokens();
        String message = "We've sent a verification code to your email. Verify it to finish registration.";
        if (tokens != null) {
            return ResponseEntity.status(201)
                    .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                    .body(new RegistrationResponse(
                            result.verificationRequired(),
                            message,
                            tokens.accessToken()
                    ));
        }
        return ResponseEntity.status(201)
                .body(new RegistrationResponse(
                        result.verificationRequired(),
                        message,
                        null
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Validates email and password, issues access/refresh JWT, and sets httpOnly cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "202", description = "OTP required"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Too many attempts", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
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
    @Operation(summary = "Current user profile", description = "Returns email, name, base currency, and role.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        UserProfileResponse profile = authService.profile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/verify/request")
    @Operation(summary = "Request email verification code", description = "Sends a verification email with a confirmation code.")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    public ResponseEntity<?> requestVerification(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email().trim().toLowerCase();
        String ip = clientIpResolver.resolve(httpRequest);
        RateLimiterService.Result ipRes = rateLimiterService.check("verify:ip:" + ip, verifyLimit, verifyWindowMs);
        RateLimiterService.Result emailRes = rateLimiterService.check("verify:email:" + email, verifyLimit, verifyWindowMs);
        if (!ipRes.allowed() || !emailRes.allowed()) {
            long retryMs = Math.max(ipRes.retryAfterMs(), emailRes.retryAfterMs());
            long retrySec = (long) Math.ceil(retryMs / 1000.0);
            return ResponseEntity.status(429)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(retrySec, 1)))
                    .body(new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Please try again later.", Math.max(retrySec, 1)));
        }
        authService.requestVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Validates the verification code, marks email as verified, and issues tokens.")
    @ApiResponse(responseCode = "200", description = "Email verified")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest request, HttpServletRequest httpRequest) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        String ip = clientIpResolver.resolve(httpRequest);
        RateLimiterService.Result ipRes = rateLimiterService.check("verify:ip:" + ip, verifyLimit, verifyWindowMs);
        RateLimiterService.Result emailRes = email.isBlank()
                ? new RateLimiterService.Result(true, 0)
                : rateLimiterService.check("verify:email:" + email, verifyLimit, verifyWindowMs);
        if (!ipRes.allowed() || !emailRes.allowed()) {
            long retryMs = Math.max(ipRes.retryAfterMs(), emailRes.retryAfterMs());
            long retrySec = (long) Math.ceil(retryMs / 1000.0);
            return ResponseEntity.status(429)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(retrySec, 1)))
                    .body(new ApiError(ErrorCodes.RATE_LIMIT, "Too many attempts. Please try again later.", Math.max(retrySec, 1)));
        }
        AuthTokens tokens = authService.verify(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/login/otp")
    @Operation(summary = "Complete login with OTP", description = "Accepts email and OTP code, issues access/refresh JWT, and sets cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login completed"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content(schema = @Schema(implementation = ApiError.class)))
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
    @Operation(summary = "Request password reset code", description = "Sends a reset code to email. Rate limited by IP and email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset code sent"),
            @ApiResponse(responseCode = "429", description = "Too many attempts", content = @Content(schema = @Schema(implementation = ApiError.class)))
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
                    .body(new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Please try again later.", Math.max(retrySec, 1)));
        }
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If this email exists, we've sent a reset code."));
    }

    @PostMapping({"/reset/confirm", "/reset/check"})
    @Operation(summary = "Confirm reset code", description = "Validates the email code and returns resetSessionToken with TTL.")
    public ResponseEntity<ResetSessionResponse> confirmReset(@Valid @RequestBody ValidateResetTokenRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        ResetSessionResponse response = authService.confirmResetToken(request, ip, httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset password with resetSessionToken", description = "Accepts resetSessionToken and a new password. Invalidates refresh sessions.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String ip = clientIpResolver.resolve(httpRequest);
        authService.resetPassword(request, ip, httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Uses the refresh cookie FG_REFRESH to issue a new token set and set cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed"),
            @ApiResponse(responseCode = "401", description = "Refresh token is missing or invalid", content = @Content(schema = @Schema(implementation = ApiError.class)))
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
    @Operation(summary = "Logout", description = "Clears FG_AUTH/FG_REFRESH cookies and revokes tokens/sessions.")
    @ApiResponse(responseCode = "200", description = "Logout completed")
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
    @Operation(summary = "CSRF token", description = "Returns CSRF token and sets the XSRF-TOKEN cookie.")
    public ResponseEntity<Map<String, String>> csrf(@Nullable CsrfToken token) {
        String value = token == null ? "" : token.getToken();
        return ResponseEntity.ok(Map.of("token", value));
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
