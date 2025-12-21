package com.myname.finguard.auth.service;

import com.myname.finguard.auth.dto.AuthTokens;
import com.myname.finguard.auth.dto.ForgotPasswordRequest;
import com.myname.finguard.auth.dto.LoginRequest;
import com.myname.finguard.auth.dto.RegisterRequest;
import com.myname.finguard.auth.dto.ResetPasswordRequest;
import com.myname.finguard.auth.dto.ResetSessionResponse;
import com.myname.finguard.auth.dto.ValidateResetTokenRequest;
import com.myname.finguard.auth.dto.UserProfileResponse;
import com.myname.finguard.auth.dto.VerifyRequest;
import com.myname.finguard.auth.dto.OtpVerifyRequest;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.PendingRegistration;
import com.myname.finguard.auth.model.PasswordResetSession;
import com.myname.finguard.auth.model.UserToken;
import com.myname.finguard.auth.model.UserTokenType;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.common.util.PasswordValidator;
import com.myname.finguard.auth.service.PendingRegistrationService;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.common.service.PwnedPasswordChecker;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.security.JwtTokenProvider;
import com.myname.finguard.security.LoginAttemptService;
import com.myname.finguard.security.RateLimiterService;
import com.myname.finguard.security.TokenBlacklistService;
import com.myname.finguard.security.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final CurrencyService currencyService;
    private final PwnedPasswordChecker pwnedPasswordChecker;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserTokenService userTokenService;
    private final UserSessionService userSessionService;
    private final MailService mailService;
    private final PendingRegistrationService pendingRegistrationService;
    private final PasswordResetSessionService passwordResetSessionService;
    private final RateLimiterService rateLimiterService;
    private final OtpService otpService;
    private final int resetConfirmLimit;
    private final long resetConfirmWindowMs;
    private final int resetLimit;
    private final long resetWindowMs;
    private final int loginEmailLimit;
    private final long loginEmailWindowMs;
    private final boolean otpEnabled;
    private final int loginOtpLimit;
    private final long loginOtpWindowMs;
    private final int loginOtpIssueLimit;
    private final long loginOtpIssueWindowMs;
    private final int registerIpLimit;
    private final long registerIpWindowMs;
    private final int registerEmailLimit;
    private final long registerEmailWindowMs;
    private final boolean requireEmailVerified;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       LoginAttemptService loginAttemptService,
                       CurrencyService currencyService,
                       PwnedPasswordChecker pwnedPasswordChecker,
                       TokenBlacklistService tokenBlacklistService,
                       UserTokenService userTokenService,
                       UserSessionService userSessionService,
                       PendingRegistrationService pendingRegistrationService,
                       PasswordResetSessionService passwordResetSessionService,
                       RateLimiterService rateLimiterService,
                       OtpService otpService,
                       MailService mailService,
                       @Value("${app.security.rate-limit.reset-confirm.limit:5}") int resetConfirmLimit,
                       @Value("${app.security.rate-limit.reset-confirm.window-ms:300000}") long resetConfirmWindowMs,
                       @Value("${app.security.rate-limit.reset.limit:5}") int resetLimit,
                       @Value("${app.security.rate-limit.reset.window-ms:300000}") long resetWindowMs,
                       @Value("${app.security.rate-limit.login-email.limit:5}") int loginEmailLimit,
                       @Value("${app.security.rate-limit.login-email.window-ms:300000}") long loginEmailWindowMs,
                       @Value("${app.security.rate-limit.login-otp.limit:5}") int loginOtpLimit,
                       @Value("${app.security.rate-limit.login-otp.window-ms:300000}") long loginOtpWindowMs,
                       @Value("${app.security.rate-limit.login-otp-issue.limit:1}") int loginOtpIssueLimit,
                       @Value("${app.security.rate-limit.login-otp-issue.window-ms:60000}") long loginOtpIssueWindowMs,
                       @Value("${app.security.rate-limit.register-ip.limit:10}") int registerIpLimit,
                       @Value("${app.security.rate-limit.register-ip.window-ms:300000}") long registerIpWindowMs,
                       @Value("${app.security.rate-limit.register-email.limit:5}") int registerEmailLimit,
                       @Value("${app.security.rate-limit.register-email.window-ms:300000}") long registerEmailWindowMs,
                       @Value("${app.security.auth.require-email-verified:true}") boolean requireEmailVerified,
                       @Value("${app.security.otp.enabled:false}") boolean otpEnabled) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.currencyService = currencyService;
        this.pwnedPasswordChecker = pwnedPasswordChecker;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userTokenService = userTokenService;
        this.userSessionService = userSessionService;
        this.mailService = mailService;
        this.pendingRegistrationService = pendingRegistrationService;
        this.passwordResetSessionService = passwordResetSessionService;
        this.rateLimiterService = rateLimiterService;
        this.otpService = otpService;
        this.resetConfirmLimit = resetConfirmLimit;
        this.resetConfirmWindowMs = resetConfirmWindowMs;
        this.resetLimit = resetLimit;
        this.resetWindowMs = resetWindowMs;
        this.loginEmailLimit = loginEmailLimit;
        this.loginEmailWindowMs = loginEmailWindowMs;
        this.loginOtpLimit = loginOtpLimit;
        this.loginOtpWindowMs = loginOtpWindowMs;
        this.loginOtpIssueLimit = loginOtpIssueLimit;
        this.loginOtpIssueWindowMs = loginOtpIssueWindowMs;
        this.registerIpLimit = registerIpLimit;
        this.registerIpWindowMs = registerIpWindowMs;
        this.registerEmailLimit = registerEmailLimit;
        this.registerEmailWindowMs = registerEmailWindowMs;
        this.requireEmailVerified = requireEmailVerified;
        this.otpEnabled = otpEnabled;
    }

    @Transactional
    public RegistrationResult register(RegisterRequest request, String ip) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        String fullName = request.fullName() == null ? "" : request.fullName().trim();
        String baseCurrency = currencyService.normalize(request.baseCurrency());
        enforceRateLimit("register:ip:" + safe(ip), registerIpLimit, registerIpWindowMs);
        enforceRateLimit("register:email:" + email, registerEmailLimit, registerEmailWindowMs);

        if (fullName.isBlank()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Full name is required", HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isSupported(baseCurrency)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCodes.AUTH_EMAIL_EXISTS, "Email is already registered", HttpStatus.BAD_REQUEST);
        }
        if (PasswordValidator.isWeak(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is too weak. Use a unique, strong password.", HttpStatus.BAD_REQUEST);
        }
        if (pwnedPasswordChecker.isPwned(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is compromised. Choose another password.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCodes.AUTH_EMAIL_EXISTS, "Email is already registered", HttpStatus.BAD_REQUEST);
        }
        String verifyCode = userTokenService.generateVerifyCode();
        pendingRegistrationService.createOrUpdate(email, request.password(), fullName, baseCurrency, Role.USER, verifyCode);
        mailService.sendVerifyEmail(email, verifyCode, pendingRegistrationService.getVerifyTtl());
        return new RegistrationResult(null, true);
    }

    public LoginOutcome login(LoginRequest request, String ip) {
        String email = request.email().trim().toLowerCase();
        enforceRateLimit("login:email:" + email, loginEmailLimit, loginEmailWindowMs);
        if (loginAttemptService.isLocked(email)) {
            log.warn("Login blocked due to lockout for email={}", email);
            throw new ApiException(ErrorCodes.AUTH_LOCKED, "Too many attempts. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            Object principal = authentication.getPrincipal();
            Long userId;
            if (principal instanceof com.myname.finguard.security.UserPrincipal userPrincipal) {
                userId = userPrincipal.getId();
                email = userPrincipal.getUsername();
            } else {
                userId = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User not found after authentication"))
                        .getId();
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
            if (requireEmailVerified && !user.isEmailVerified()) {
                log.warn("Login blocked: email not verified email={}", email);
                throw new ApiException(
                        ErrorCodes.AUTH_EMAIL_NOT_VERIFIED,
                        "Email is not verified. Please check your email for the verification code.",
                        HttpStatus.FORBIDDEN
                );
            }
            loginAttemptService.recordSuccess(email);
            if (!otpEnabled) {
                AuthTokens tokens = issueTokens(user);
                return new LoginOutcome(tokens, false, 0);
            }
            OtpService.IssuedOtp existing = otpService.getActive(email);
            if (existing != null) {
                long ttlSeconds = Duration.between(Instant.now(), existing.expiresAt()).getSeconds();
                return new LoginOutcome(null, true, Math.max(ttlSeconds, 1));
            }
            RateLimiterService.Result issueEmailLimit = rateLimiterService.check(
                    "login-otp-issue:email:" + email, loginOtpIssueLimit, loginOtpIssueWindowMs);
            RateLimiterService.Result issueIpLimit = rateLimiterService.check(
                    "login-otp-issue:ip:" + safe(ip), loginOtpIssueLimit, loginOtpIssueWindowMs);
            if (!issueEmailLimit.allowed() || !issueIpLimit.allowed()) {
                long retryMs = Math.max(issueEmailLimit.retryAfterMs(), issueIpLimit.retryAfterMs());
                long retry = Math.max(1, Math.round(Math.ceil(retryMs / 1000.0)));
                log.warn("OTP issuance limited email={}, ip={}, retryAfterSec={}", email, safe(ip), retry);
                throw new ApiException(
                        ErrorCodes.OTP_ALREADY_SENT,
                        "OTP code already sent. Please check your email and try again later.",
                        HttpStatus.TOO_MANY_REQUESTS,
                        retry
                );
            }
            OtpService.IssuedOtp issued = otpService.issue(email);
            mailService.sendOtpEmail(email, issued.code(), Duration.between(Instant.now(), issued.expiresAt()));
            long ttlSeconds = Duration.between(Instant.now(), issued.expiresAt()).getSeconds();
            return new LoginOutcome(null, true, Math.max(ttlSeconds, 1));
        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailure(email);
            log.warn("Login failed for email={}", email);
            throw new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    public UserProfileResponse profile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found", HttpStatus.UNAUTHORIZED));
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getBaseCurrency(),
                user.getRole().name()
        );
    }

    public long tokenTtlSeconds() {
        return jwtTokenProvider.getValiditySeconds();
    }

    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken) || !"refresh".equalsIgnoreCase(jwtTokenProvider.getType(refreshToken))) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        String jti = jwtTokenProvider.getJti(refreshToken);
        if (tokenBlacklistService.isRevoked(jti)) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        if (!userSessionService.isActive(jti)) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);
        if (userId == null || email == null) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        int tokenVersion = jwtTokenProvider.getTokenVersion(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (tokenVersion != user.getTokenVersion()) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
        if (requireEmailVerified && !user.isEmailVerified()) {
            throw new ApiException(
                    ErrorCodes.AUTH_EMAIL_NOT_VERIFIED,
                    "Email is not verified. Please check your email for the verification code.",
                    HttpStatus.FORBIDDEN
            );
        }
        // revoke the old refresh token on rotation
        tokenBlacklistService.revoke(jti, jwtTokenProvider.getExpiry(refreshToken).toInstant());
        userSessionService.revoke(jti);
        return issueTokens(user);
    }

    public void revokeToken(String token) {
        if (!jwtTokenProvider.isValid(token)) {
            return;
        }
        String jti = jwtTokenProvider.getJti(token);
        Date expiry = jwtTokenProvider.getExpiry(token);
        if (jti != null && expiry != null) {
            tokenBlacklistService.revoke(jti, expiry.toInstant());
            userSessionService.revoke(jti);
        }
    }

    private AuthTokens issueTokens(User user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found", HttpStatus.UNAUTHORIZED);
        }
        int version = user.getTokenVersion();
        String access = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), version);
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), version);
        String jtiRefresh = jwtTokenProvider.getJti(refresh);
        userSessionService.register(user, jtiRefresh, jwtTokenProvider.getExpiry(refresh).toInstant());
        return new AuthTokens(access, refresh);
    }

    public void requestVerification(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                String token = userTokenService.issue(user, UserTokenType.VERIFY);
                mailService.sendVerifyEmail(user.getEmail(), token, userTokenService.getVerifyTtl());
            }
        });
    }

    public AuthTokens verify(VerifyRequest request) {
        String email = request.email().trim().toLowerCase();
        PendingRegistration pending = pendingRegistrationService.findValid(email, request.token())
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Verification token is invalid or expired", HttpStatus.BAD_REQUEST));
        if (userRepository.existsByEmail(email)) {
            pendingRegistrationService.delete(pending);
            throw new ApiException(ErrorCodes.AUTH_EMAIL_EXISTS, "Email is already registered", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(pending.getPasswordHash());
        user.setFullName(pending.getFullName());
        user.setBaseCurrency(pending.getBaseCurrency());
        user.setRole(pending.getRole() == null ? Role.USER : pending.getRole());
        user.setEmailVerified(true);
        User saved = userRepository.save(user);
        pendingRegistrationService.delete(pending);
        return issueTokens(saved);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            userTokenService.invalidateActive(user, UserTokenType.RESET);
            String tokenValue = userTokenService.issue(user, UserTokenType.RESET);
            mailService.sendResetEmail(user.getEmail(), tokenValue, userTokenService.getResetTtl());
            log.info("Reset requested for email={}", maskEmail(user.getEmail()));
        });
    }

    public ResetSessionResponse confirmResetToken(ValidateResetTokenRequest request, String ip, String userAgent) {
        enforceResetConfirmLimit("reset-confirm:ip:" + safe(ip), null);

        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        UserToken token = userTokenService.findAnyForEmail(email, request.token(), UserTokenType.RESET)
                .orElseThrow(this::invalidResetCode);
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(Instant.now())) {
            userTokenService.markUsed(token);
            passwordResetSessionService.invalidateForUser(token.getUser());
            throw expiredResetCode();
        }
        if (token.getUsedAt() != null) {
            throw expiredResetCode();
        }

        enforceResetConfirmLimit("reset-confirm:email:" + safe(token.getUser().getEmail()), token.getUser());

        PasswordResetSessionService.CreatedSession createdSession = passwordResetSessionService.create(token.getUser(), token.getExpiresAt(), ip, userAgent);
        PasswordResetSession session = createdSession.session();
        long ttlSeconds = Duration.between(Instant.now(), session.getExpiresAt()).getSeconds();
        if (ttlSeconds <= 0) {
            ttlSeconds = Math.max(passwordResetSessionService.getSessionTtl().getSeconds(), 60);
        }
        String sessionToken = jwtTokenProvider.generateResetSessionToken(
                token.getUser().getId(),
                token.getUser().getEmail(),
                createdSession.rawToken(),
                Math.max(ttlSeconds, 1),
                session.getIpHash(),
                session.getUserAgentHash()
        );
        userTokenService.markUsed(token);
        return new ResetSessionResponse(sessionToken, ttlSeconds);
    }

    public void resetPassword(ResetPasswordRequest request, String ip, String userAgent) {
        enforceRateLimit("reset:ip:" + safe(ip), resetLimit, resetWindowMs);
        JwtTokenProvider.ResetSessionClaims claims;
        try {
            claims = jwtTokenProvider.parseResetSessionToken(request.resetSessionToken());
        } catch (Exception e) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid reset session token", HttpStatus.BAD_REQUEST);
        }
        enforceRateLimit("reset:email:" + safe(claims.email()), resetLimit, resetWindowMs);
        if (claims.expiresAt() == null || claims.expiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid reset session token", HttpStatus.BAD_REQUEST);
        }

        PasswordResetSession session = passwordResetSessionService.findActive(claims.jti())
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid reset session token", HttpStatus.BAD_REQUEST));
        PasswordResetSessionService.ContextCheckResult context = passwordResetSessionService.evaluateContext(
                session,
                ip,
                userAgent,
                claims.ipHash(),
                claims.userAgentHash()
        );
        if (context.shouldReject()) {
            log.warn("Reset session context rejected for user={}, tampered={}, ipMatch={}, uaMatch={}, ipHash={}, uaHash={}",
                    maskEmail(session.getUser().getEmail()),
                    context.tampered(),
                    context.ipMatches(),
                    context.userAgentMatches(),
                    context.requestIpHash(),
                    context.requestUaHash());
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Reset session token context mismatch. Please request a new code.", HttpStatus.BAD_REQUEST);
        }
        if (context.isSoftMismatch()) {
            log.warn("Reset session context mismatch for user={}, ipMatch={}, uaMatch={}, ipHash={}, uaHash={}",
                    maskEmail(session.getUser().getEmail()),
                    context.ipMatches(),
                    context.userAgentMatches(),
                    context.requestIpHash(),
                    context.requestUaHash());
        }

        if (PasswordValidator.isWeak(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is too weak. Use a unique, strong password.", HttpStatus.BAD_REQUEST);
        }
        if (pwnedPasswordChecker.isPwned(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is compromised. Choose another password.", HttpStatus.BAD_REQUEST);
        }

        User user = session.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        passwordResetSessionService.consume(session);
        userTokenService.invalidateActive(user, UserTokenType.RESET);
        revokeUserSessions(user);
    }

    public AuthTokens verifyOtp(OtpVerifyRequest request, String ip) {
        if (!otpEnabled) {
            throw new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "OTP is not enabled", HttpStatus.BAD_REQUEST);
        }
        String email = request.email().trim().toLowerCase();
        enforceRateLimit("login-otp:email:" + email, loginOtpLimit, loginOtpWindowMs);
        enforceRateLimit("login-otp:ip:" + safe(ip), loginOtpLimit, loginOtpWindowMs);
        boolean ok = otpService.verify(email, request.code());
        if (!ok) {
            throw new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid or expired OTP code", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid or expired OTP code", HttpStatus.UNAUTHORIZED));
        if (requireEmailVerified && !user.isEmailVerified()) {
            throw new ApiException(
                    ErrorCodes.AUTH_EMAIL_NOT_VERIFIED,
                    "Email is not verified. Please check your email for the verification code.",
                    HttpStatus.FORBIDDEN
            );
        }
        return issueTokens(user);
    }

    private void enforceResetConfirmLimit(String key, User userToInvalidate) {
        RateLimiterService.Result res = rateLimiterService.check(key, resetConfirmLimit, resetConfirmWindowMs);
        if (!res.allowed()) {
            if (userToInvalidate != null) {
                userTokenService.invalidateActive(userToInvalidate, UserTokenType.RESET);
                passwordResetSessionService.invalidateForUser(userToInvalidate);
            }
            long retry = Math.max(1, Math.round(Math.ceil(res.retryAfterMs() / 1000.0)));
            throw new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Too many attempts. Request a new code.", HttpStatus.TOO_MANY_REQUESTS, retry);
        }
    }

    private ApiException invalidResetCode() {
        return new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Reset code is incorrect.", HttpStatus.BAD_REQUEST);
    }

    private ApiException expiredResetCode() {
        return new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Reset code expired. Please request a new one.", HttpStatus.BAD_REQUEST);
    }

    private void enforceRateLimit(String key, int limit, long windowMs) {
        RateLimiterService.Result res = rateLimiterService.check(key, limit, windowMs);
        if (!res.allowed()) {
            long retry = Math.max(1, Math.round(Math.ceil(res.retryAfterMs() / 1000.0)));
            throw new ApiException(ErrorCodes.RATE_LIMIT, "Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS, retry);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void revokeUserSessions(User user) {
        List<com.myname.finguard.auth.model.UserSession> sessions = userSessionService.revokeAll(user);
        sessions.forEach(session -> tokenBlacklistService.revoke(session.getJti(), session.getExpiresAt()));
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    public record LoginOutcome(AuthTokens tokens, boolean otpRequired, long otpExpiresInSeconds) {
    }

    public record RegistrationResult(AuthTokens tokens, boolean verificationRequired) {
    }
}
