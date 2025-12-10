package com.yourname.finguard.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final String FORBIDDEN_PAGE = "/app/forbidden.html";
    private static final Logger log = LoggerFactory.getLogger(AppAccessDeniedHandler.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.warn("Auth entry point triggered: uri={}, message={}", request.getRequestURI(), authException == null ? "n/a" : authException.getMessage());
        if (isApi(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        } else {
            response.sendRedirect(FORBIDDEN_PAGE);
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        log.warn("Access denied: uri={}, message={}", request.getRequestURI(), accessDeniedException == null ? "n/a" : accessDeniedException.getMessage());
        if (isApi(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        } else {
            response.sendRedirect(FORBIDDEN_PAGE);
        }
    }

    private boolean isApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }
}
