package com.myname.finguard.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

    private final boolean trustProxyHeaders;

    public ClientIpResolver(@Value("${app.security.trust-proxy-headers:false}") boolean trustProxyHeaders) {
        this.trustProxyHeaders = trustProxyHeaders;
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        if (trustProxyHeaders) {
            String forwarded = firstForwardedIp(request.getHeader("X-Forwarded-For"));
            if (StringUtils.hasText(forwarded)) {
                return forwarded;
            }
            String realIp = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(realIp)) {
                return realIp.trim();
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "" : remote;
    }

    private String firstForwardedIp(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        for (String part : header.split(",")) {
            String candidate = part.trim();
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }
}
