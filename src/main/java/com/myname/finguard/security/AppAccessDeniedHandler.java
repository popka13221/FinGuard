package com.myname.finguard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.dto.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final String FORBIDDEN_PAGE = "/app/forbidden.html";
    private static final String LOGIN_PAGE = "/app/login.html";

    private final ObjectMapper objectMapper;

    public AppAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        if (isApi(request)) {
            writeForbidden(response);
        } else {
            response.sendRedirect(LOGIN_PAGE);
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        if (isApi(request)) {
            writeForbidden(response);
        } else {
            response.sendRedirect(FORBIDDEN_PAGE);
        }
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ApiError(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Forbidden", null));
    }

    private boolean isApi(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String uri = request.getRequestURI();
        String path = request.getServletPath();
        Object original = request.getAttribute("jakarta.servlet.error.request_uri");
        Object legacyOriginal = request.getAttribute("javax.servlet.error.request_uri");
        String orig = original != null ? original.toString() : legacyOriginal != null ? legacyOriginal.toString() : "";
        return (uri != null && uri.contains("/api/"))
                || (path != null && path.contains("/api/"))
                || (orig != null && orig.contains("/api/"));
    }
}
