package com.yourname.finguard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

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
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers(
                            "/api/currencies",
                            "/app/**",
                            "/playground/**",
                            "/crypto/**",
                            "/"
                    )
            );
        } else {
            http.csrf(csrf -> csrf.disable());
        }
        http
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; font-src 'self' data:; script-src 'self'; connect-src 'self'"))
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .permissionsPolicy(pp -> pp.policy("geolocation=(), microphone=(), camera=()"))
                )
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
                        "/app/landing.js",
                        "/app/styles.css",
                        "/app/forbidden.html",
                        "/app/assets/**",
                        "/app",
                        "/app/",
                        "/crypto/**",
                        "/index",
                        "/index.html",
                        "/"
                        ).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers("/health", "/actuator/health", "/actuator/health/**").hasRole("ADMIN")
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
