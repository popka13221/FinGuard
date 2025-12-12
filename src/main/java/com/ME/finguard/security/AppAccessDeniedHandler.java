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

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final String FORBIDDEN_PAGE = "/app/forbidden.html";
    private static final String LOGIN_PAGE = "/app/login.html";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        if (isApi(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        } else {
            response.sendRedirect(LOGIN_PAGE);
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
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
