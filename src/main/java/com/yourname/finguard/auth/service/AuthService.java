package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.dto.AuthResponse;
import com.yourname.finguard.auth.dto.LoginRequest;
import com.yourname.finguard.auth.dto.RegisterRequest;
import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.repository.UserRepository;
import com.yourname.finguard.common.model.Role;
import com.yourname.finguard.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        Object principal = authentication.getPrincipal();
        Long userId = null;
        String email = authentication.getName();
        if (principal instanceof com.yourname.finguard.security.UserPrincipal userPrincipal) {
            userId = userPrincipal.getId();
            email = userPrincipal.getUsername();
        } else {
            userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"))
                    .getId();
        }
        String token = jwtTokenProvider.generateToken(userId, email);
        return new AuthResponse(token);
    }
}
