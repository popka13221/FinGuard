package com.myname.finguard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
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
    private final ClientIpResolver clientIpResolver;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthRateLimitFilter.class);

    public AuthRateLimitFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper, ClientIpResolver clientIpResolver) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (PROTECTED_PATHS.contains(path)) {
            String ip = clientIpResolver.resolve(request);
            String key = ip + ":" + path;
            RateLimiterService.Result res = rateLimiterService.check(key);
            if (!res.allowed()) {
                log.warn("Rate limit exceeded for ip={}, path={}", ip, path);
                long retryAfter = Math.max(1, Math.round(Math.ceil(res.retryAfterMs() / 1000.0)));
                ApiError error = new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Please try again later.", retryAfter);
                response.setStatus(429);
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
