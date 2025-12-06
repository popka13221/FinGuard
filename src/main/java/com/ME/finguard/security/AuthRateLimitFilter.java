package com.yourname.finguard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.common.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot",
            "/api/auth/reset/confirm",
            "/api/auth/reset",
            "/api/auth/reset/check",
            "/api/auth/refresh",
            "/api/auth/verify",
            "/api/auth/verify/request",
            "/api/auth/login/otp"
    );

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthRateLimitFilter.class);

    public AuthRateLimitFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (PROTECTED_PATHS.contains(path)) {
            String key = request.getRemoteAddr() + ":" + path;
            if (!rateLimiterService.allow(key)) {
                log.warn("Rate limit exceeded for ip={}, path={}", request.getRemoteAddr(), path);
                ApiError error = new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Try again later.");
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
