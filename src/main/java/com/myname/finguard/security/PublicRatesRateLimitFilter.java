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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PublicRatesRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PATHS = Set.of(
            "/api/fx/rates",
            "/api/crypto/rates"
    );

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;
    private final ClientIpResolver clientIpResolver;
    private final int limit;
    private final long windowMs;

    public PublicRatesRateLimitFilter(
            RateLimiterService rateLimiterService,
            ObjectMapper objectMapper,
            ClientIpResolver clientIpResolver,
            @Value("${app.security.rate-limit.public-rates.limit:120}") int limit,
            @Value("${app.security.rate-limit.public-rates.window-ms:60000}") long windowMs
    ) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
        this.clientIpResolver = clientIpResolver;
        this.limit = limit;
        this.windowMs = windowMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!"GET".equalsIgnoreCase(request.getMethod()) || !PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String ip = clientIpResolver.resolve(request);
        RateLimiterService.Result res = rateLimiterService.check("public-rates:" + path + ":" + ip, limit, windowMs);
        if (res.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }
        long retryAfter = Math.max(1, Math.round(Math.ceil(res.retryAfterMs() / 1000.0)));
        ApiError error = new ApiError(ErrorCodes.RATE_LIMIT, "Too many requests. Please try again later.", retryAfter);
        response.setStatus(429);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

