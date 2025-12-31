package com.myname.finguard.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.auth.dto.LoginRequest;
import com.myname.finguard.auth.dto.OtpVerifyRequest;
import com.myname.finguard.auth.dto.RegisterRequest;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.UserTokenType;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.common.service.PwnedPasswordChecker;
import com.myname.finguard.security.JwtTokenProvider;
import com.myname.finguard.security.LoginAttemptService;
import com.myname.finguard.security.OtpService;
import com.myname.finguard.security.RateLimiterService;
import com.myname.finguard.security.TokenBlacklistService;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final com.myname.finguard.auth.repository.UserRepository userRepository = mock(com.myname.finguard.auth.repository.UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
    private final CurrencyService currencyService = new CurrencyService();
    private final PwnedPasswordChecker pwnedPasswordChecker = mock(PwnedPasswordChecker.class);
    private final TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
    private final UserTokenService userTokenService = mock(UserTokenService.class);
    private final PendingRegistrationService pendingRegistrationService = mock(PendingRegistrationService.class);
    private final UserSessionService userSessionService = mock(UserSessionService.class);
    private final PasswordResetSessionService passwordResetSessionService = mock(PasswordResetSessionService.class);
    private final RateLimiterService rateLimiterService = mock(RateLimiterService.class);
    private final OtpService otpService = mock(OtpService.class);
    private final MailService mailService = mock(MailService.class);

    @Test
    void registerCreatesPendingRegistrationAndSendsVerifyEmail() {
        AuthService authService = createService(false);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(pwnedPasswordChecker.isPwned(anyString())).thenReturn(false);

        when(userTokenService.generateVerifyCode()).thenReturn("verify-code");

        RegisterRequest request = new RegisterRequest("USER@EXAMPLE.COM", "StrongPass1!", "  User  ", " usd ");
        AuthService.RegistrationResult result = authService.register(request, "10.0.0.1");

        assertThat(result.verificationRequired()).isTrue();
        assertThat(result.tokens()).isNull();

        verify(pendingRegistrationService).createOrUpdate(
                eq("user@example.com"),
                eq("StrongPass1!"),
                eq("User"),
                eq("USD"),
                eq(Role.USER),
                eq("verify-code")
        );
        verify(mailService).sendVerifyEmail(eq("user@example.com"), eq("verify-code"), any());

        verify(userRepository, never()).save(any(User.class));
        verify(userTokenService, never()).issue(any(User.class), any());
        verify(userSessionService, never()).register(any(User.class), anyString(), any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        AuthService authService = createService(false);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("USER@EXAMPLE.COM", "StrongPass1!", "User", "USD");
        assertThatThrownBy(() -> authService.register(request, "10.0.0.1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.AUTH_EMAIL_EXISTS);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsWeakPassword() {
        AuthService authService = createService(false);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(pwnedPasswordChecker.isPwned(anyString())).thenReturn(false);

        RegisterRequest request = new RegisterRequest("user@example.com", "short", "User", "USD");
        assertThatThrownBy(() -> authService.register(request, "10.0.0.1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.AUTH_WEAK_PASSWORD);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void loginIssuesTokensWhenOtpDisabled() {
        AuthService authService = createService(false);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(loginAttemptService.isLocked("user@example.com")).thenReturn(false);

        User user = user(99L, "user@example.com", true, 0);
        Authentication auth = new UsernamePasswordAuthenticationToken(new com.myname.finguard.security.UserPrincipal(user), null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));

        stubTokenIssuance(99L, "user@example.com", 0);

        AuthService.LoginOutcome outcome = authService.login(new LoginRequest("user@example.com", "StrongPass1!"), "10.0.0.1");

        assertThat(outcome.otpRequired()).isFalse();
        assertThat(outcome.tokens()).isNotNull();
        assertThat(outcome.tokens().accessToken()).isEqualTo("access");

        verify(loginAttemptService).recordSuccess("user@example.com");
        verify(userSessionService).register(any(User.class), eq("jti-refresh"), any());
        verify(mailService, never()).sendOtpEmail(anyString(), anyString(), any());
        verify(otpService, never()).issue(anyString());
    }

    @Test
    void loginReturnsOtpChallengeWhenEnabledAndNoActiveCode() {
        AuthService authService = createService(true);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(loginAttemptService.isLocked("user@example.com")).thenReturn(false);

        User user = user(99L, "user@example.com", true, 0);
        Authentication auth = new UsernamePasswordAuthenticationToken(new com.myname.finguard.security.UserPrincipal(user), null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));

        when(otpService.getActive("user@example.com")).thenReturn(null);
        when(otpService.issue("user@example.com")).thenReturn(new OtpService.IssuedOtp(
                "654321",
                Instant.now().plusSeconds(300),
                true
        ));

        AuthService.LoginOutcome outcome = authService.login(new LoginRequest("user@example.com", "StrongPass1!"), "10.0.0.1");

        assertThat(outcome.otpRequired()).isTrue();
        assertThat(outcome.tokens()).isNull();
        assertThat(outcome.otpExpiresInSeconds()).isGreaterThan(0);

        verify(mailService).sendOtpEmail(eq("user@example.com"), eq("654321"), any());
        verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString(), anyInt());
    }

    @Test
    void loginDoesNotReissueOtpWhenActiveCodeExists() {
        AuthService authService = createService(true);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(loginAttemptService.isLocked("user@example.com")).thenReturn(false);

        User user = user(99L, "user@example.com", true, 0);
        Authentication auth = new UsernamePasswordAuthenticationToken(new com.myname.finguard.security.UserPrincipal(user), null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));

        when(otpService.getActive("user@example.com")).thenReturn(new OtpService.IssuedOtp(
                "",
                Instant.now().plusSeconds(250),
                false
        ));

        AuthService.LoginOutcome outcome = authService.login(new LoginRequest("user@example.com", "StrongPass1!"), "10.0.0.1");

        assertThat(outcome.otpRequired()).isTrue();
        assertThat(outcome.tokens()).isNull();
        assertThat(outcome.otpExpiresInSeconds()).isGreaterThan(0);

        verify(otpService, never()).issue(anyString());
        verify(mailService, never()).sendOtpEmail(anyString(), anyString(), any());
    }

    @Test
    void verifyOtpReturnsTokensWhenCodeIsValid() {
        AuthService authService = createService(true);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(otpService.verify("user@example.com", "654321")).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(7L, "user@example.com", true, 0)));

        stubTokenIssuance(7L, "user@example.com", 0);

        var tokens = authService.verifyOtp(new OtpVerifyRequest("user@example.com", "654321"), "10.0.0.1");
        assertThat(tokens.accessToken()).isEqualTo("access");
        assertThat(tokens.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void verifyOtpRejectsInvalidCode() {
        AuthService authService = createService(true);

        when(rateLimiterService.check(anyString(), anyInt(), anyLong()))
                .thenReturn(new RateLimiterService.Result(true, 0));
        when(otpService.verify(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyOtp(new OtpVerifyRequest("user@example.com", "000000"), "10.0.0.1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.AUTH_INVALID_CREDENTIALS);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    void revokeTokenDoesNothingWhenInvalid() {
        AuthService authService = createService(false);
        when(jwtTokenProvider.isValid("bad")).thenReturn(false);

        authService.revokeToken("bad");

        verify(tokenBlacklistService, never()).revoke(anyString(), any());
        verify(userSessionService, never()).revoke(anyString());
    }

    @Test
    void revokeTokenBlacklistsAndRevokesSessionWhenValid() {
        AuthService authService = createService(false);
        when(jwtTokenProvider.isValid("good")).thenReturn(true);
        when(jwtTokenProvider.getJti("good")).thenReturn("jti-1");
        when(jwtTokenProvider.getExpiry("good")).thenReturn(Date.from(Instant.now().plusSeconds(60)));

        authService.revokeToken("good");

        verify(tokenBlacklistService).revoke(eq("jti-1"), any());
        verify(userSessionService).revoke("jti-1");
    }

    private AuthService createService(boolean otpEnabled) {
        return new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                loginAttemptService,
                currencyService,
                pwnedPasswordChecker,
                tokenBlacklistService,
                userTokenService,
                pendingRegistrationService,
                userSessionService,
                passwordResetSessionService,
                rateLimiterService,
                otpService,
                mailService,
                5,
                300_000,
                5,
                300_000,
                5,
                300_000,
                5,
                300_000,
                1,
                60_000,
                10,
                300_000,
                5,
                300_000,
                otpEnabled
        );
    }

    private void stubTokenIssuance(Long userId, String email, int tokenVersion) {
        when(jwtTokenProvider.generateAccessToken(userId, email, tokenVersion)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(userId, email, tokenVersion)).thenReturn("refresh");
        when(jwtTokenProvider.getJti("refresh")).thenReturn("jti-refresh");
        when(jwtTokenProvider.getExpiry("refresh")).thenReturn(Date.from(Instant.now().plusSeconds(600)));
    }

    private User user(Long id, String email, boolean emailVerified, int tokenVersion) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(Role.USER);
        user.setEmailVerified(emailVerified);
        user.setTokenVersion(tokenVersion);
        user.setBaseCurrency("USD");
        user.setFullName("User");
        return user;
    }
}
