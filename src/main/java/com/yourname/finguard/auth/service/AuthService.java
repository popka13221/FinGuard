package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.dto.UserProfileResponse;
import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.repository.UserRepository;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.model.Role;
import com.yourname.finguard.common.util.PasswordValidator;
import com.yourname.finguard.common.exception.ApiException;
import com.yourname.finguard.security.JwtTokenProvider;
import com.yourname.finguard.security.LoginAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCodes.AUTH_EMAIL_EXISTS, "Email is already registered", HttpStatus.BAD_REQUEST);
        }
        if (PasswordValidator.isWeak(request.password())) {
            throw new ApiException(ErrorCodes.AUTH_WEAK_PASSWORD, "Password is too weak. Use a unique, strong password.", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setBaseCurrency(request.baseCurrency());
        user.setRole(Role.USER);
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        if (loginAttemptService.isLocked(email)) {
            throw new ApiException(ErrorCodes.AUTH_LOCKED, "Account temporarily locked after failed attempts. Try again later.", HttpStatus.TOO_MANY_REQUESTS);
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
            String token = jwtTokenProvider.generateToken(userId, email);
            return new AuthResponse(token);
        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailure(email);
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
}
