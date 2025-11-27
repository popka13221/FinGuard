package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.AuthTokens;
import com.yourname.finguard.auth.dto.ForgotPasswordRequest;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.dto.ResetPasswordRequest;
import com.yourname.finguard.auth.dto.UserProfileResponse;
import com.yourname.finguard.auth.dto.VerifyRequest;
import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.model.UserToken;
import com.yourname.finguard.auth.model.UserTokenType;
import com.yourname.finguard.auth.repository.UserRepository;
import com.yourname.finguard.auth.service.UserTokenService;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.model.Role;
import com.yourname.finguard.common.util.PasswordValidator;
import com.yourname.finguard.common.service.CurrencyService;
import com.yourname.finguard.common.service.MailService;
import com.yourname.finguard.common.service.PwnedPasswordChecker;
import com.yourname.finguard.common.exception.ApiException;
import com.yourname.finguard.security.JwtTokenProvider;
import com.yourname.finguard.security.LoginAttemptService;
import com.yourname.finguard.security.TokenBlacklistService;
import com.yourname.finguard.auth.service.UserSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
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
                       MailService mailService) {
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
    }

    @Transactional
    public AuthTokens register(RegisterRequest request) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        String fullName = request.fullName() == null ? "" : request.fullName().trim();
        String baseCurrency = currencyService.normalize(request.baseCurrency());

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
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(fullName);
        user.setBaseCurrency(baseCurrency);
        user.setRole(Role.USER);
        user.setEmailVerified(false);
        User saved = userRepository.save(user);
        // issue verification token (логируем/отправляем вне текущего слоя)
        userTokenService.issue(saved, UserTokenType.VERIFY);
        return issueTokens(saved.getId(), saved.getEmail());
    }

    public AuthTokens login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        if (loginAttemptService.isLocked(email)) {
            log.warn("Login blocked due to lockout for email={}", email);
            throw new ApiException(ErrorCodes.AUTH_LOCKED, "Too many attempts. Try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            Object principal = authentication.getPrincipal();
            Long userId;
            if (principal instanceof com.yourname.finguard.security.UserPrincipal userPrincipal) {
                userId = userPrincipal.getId();
                email = userPrincipal.getUsername();
            } else {
                userId = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User not found after authentication"))
                        .getId();
            }
            loginAttemptService.recordSuccess(email);
            return issueTokens(userId, email);
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
        // revoke the old refresh token on rotation
        tokenBlacklistService.revoke(jti, jwtTokenProvider.getExpiry(refreshToken).toInstant());
        userSessionService.revoke(jti);
        return issueTokens(userId, email);
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

    private AuthTokens issueTokens(Long userId, String email) {
        String access = jwtTokenProvider.generateAccessToken(userId, email);
        String refresh = jwtTokenProvider.generateRefreshToken(userId, email);
        String jtiRefresh = jwtTokenProvider.getJti(refresh);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found", HttpStatus.UNAUTHORIZED));
        userSessionService.register(user, jtiRefresh, jwtTokenProvider.getExpiry(refresh).toInstant());
        return new AuthTokens(access, refresh);
    }

    public void requestVerification(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                userTokenService.issue(user, UserTokenType.VERIFY);
            }
        });
    }

    public void verify(VerifyRequest request) {
        userTokenService.findValid(request.token(), UserTokenType.VERIFY)
                .ifPresent(token -> {
                    User user = token.getUser();
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    userTokenService.markUsed(token);
                });
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = userTokenService.issue(user, UserTokenType.RESET);
            mailService.sendResetEmail(user.getEmail(), token, userTokenService.getResetTtl());
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        UserToken token = userTokenService.findValid(request.token(), UserTokenType.RESET)
                .orElseThrow(() -> new ApiException(ErrorCodes.AUTH_REFRESH_INVALID, "Invalid reset token", HttpStatus.BAD_REQUEST));
        if (PasswordValidator.isWeak(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is too weak. Use a unique, strong password.", HttpStatus.BAD_REQUEST);
        }
        if (pwnedPasswordChecker.isPwned(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is compromised. Choose another password.", HttpStatus.BAD_REQUEST);
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        userTokenService.markUsed(token);
    }
}
