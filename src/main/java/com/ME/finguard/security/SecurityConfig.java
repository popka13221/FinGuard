package com.yourname.finguard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final boolean csrfEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthRateLimitFilter authRateLimitFilter,
                          AppAccessDeniedHandler appAccessDeniedHandler,
                          @Value("${app.security.csrf.enabled:true}") boolean csrfEnabled) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authRateLimitFilter = authRateLimitFilter;
        this.accessDeniedHandler = appAccessDeniedHandler;
        this.authenticationEntryPoint = appAccessDeniedHandler;
        this.csrfEnabled = csrfEnabled;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                            "/health",
                            "/actuator/health",
                            "/actuator/health/**"
                    )
            );
        } else {
            http.csrf(csrf -> csrf.disable());
        }
        http
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                        "/health",
                        "/actuator/health",
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/login/otp",
                        "/api/auth/csrf",
                        "/api/auth/forgot",
                        "/api/auth/reset/confirm",
                        "/api/auth/reset/check",
                        "/api/auth/reset",
                        "/api/auth/verify",
                        "/api/auth/verify/request",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/currencies",
                        "/favicon.ico",
                        "/favicon.svg",
                        "/playground",
                        "/playground/**",
                        "/app/login.html",
                        "/app/forgot.html",
                        "/app/reset.html",
                        "/app/verify.html",
                        "/app/auth.js",
                        "/app/recover.js",
                        "/app/verify.js",
                        "/app/api.js",
                        "/app/theme.js",
                        "/app/styles.css",
                        "/app/forbidden.html",
                        "/app/assets/**",
                        "/index.html",
                        "/"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
