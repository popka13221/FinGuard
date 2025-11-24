package com.yourname.finguard.auth.controller;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.dto.UserProfileResponse;
import com.yourname.finguard.auth.service.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    public AuthController(AuthService authService,
                          @Value("${app.security.jwt.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, buildCookie(response.token()).toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookie(response.token()).toString())
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        UserProfileResponse profile = authService.profile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expireCookie().toString())
                .build();
    }

    private ResponseCookie buildCookie(String token) {
        long maxAge = authService.tokenTtlSeconds();
        return ResponseCookie.from("FG_AUTH", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie expireCookie() {
        return ResponseCookie.from("FG_AUTH", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
