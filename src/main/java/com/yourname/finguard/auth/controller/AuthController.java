package com.yourname.finguard.auth.controller;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.AuthTokens;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.dto.ResetPasswordRequest;
import com.yourname.finguard.auth.dto.ValidateResetTokenRequest;
import com.yourname.finguard.auth.dto.UserProfileResponse;
import com.yourname.finguard.auth.dto.ForgotPasswordRequest;
import com.yourname.finguard.auth.dto.VerifyRequest;
import com.yourname.finguard.auth.service.AuthService;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.dto.ApiError;
import com.yourname.finguard.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService,
                          JwtTokenProvider jwtTokenProvider,
                          @Value("${app.security.jwt.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokens tokens = authService.register(request);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        UserProfileResponse profile = authService.profile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/verify/request")
    public ResponseEntity<Void> requestVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyRequest request) {
        authService.verify(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/check")
    public ResponseEntity<Void> validateReset(@Valid @RequestBody ValidateResetTokenRequest request) {
        authService.validateResetToken(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
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
