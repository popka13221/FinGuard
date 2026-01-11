package com.myname.finguard.common.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final String resetSubject;
    private final String verifySubject;
    private final String otpSubject;
    private final String frontendBaseUrl;
    private final CopyOnWriteArrayList<MailMessage> outbox = new CopyOnWriteArrayList<>();

    public record MailMessage(String to, String subject, String body, Instant createdAt) {
    }

    public MailService(@Value("${app.mail.reset-subject:FinGuard password reset}") String resetSubject,
                       @Value("${app.mail.verify-subject:FinGuard email verification}") String verifySubject,
                       @Value("${app.mail.otp-subject:FinGuard sign-in code}") String otpSubject,
                       @Value("${app.frontend.base-url:http://localhost:8080}") String frontendBaseUrl) {
        this.resetSubject = resetSubject;
        this.verifySubject = verifySubject;
        this.otpSubject = otpSubject;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
    }

    public void sendResetEmail(String to, String token, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(token)) {
            return;
        }
        String link = frontendBaseUrl + "/app/reset.html?token=" + urlEncode(token) + "&email=" + urlEncode(to);
        String body = """
                Hi!

                You requested a password reset for FinGuard.
                Reset code: %s
                Link: %s
                The code is valid for about %s minutes and can be used once.

                If you did not request this, you can ignore this email.
                """.formatted(token, link, ttl == null ? "60" : String.valueOf(ttl.toMinutes()));

        MailMessage message = new MailMessage(to, resetSubject, body, Instant.now());
        outbox.add(message);
        log.info("Mail delivery disabled, would send reset email to {} with code {}", maskEmail(to), maskCode(token));
    }

    public void sendOtpEmail(String to, String code, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(code)) {
            return;
        }
        String body = """
                Hi!

                Your FinGuard sign-in code: %s
                The code is valid for about %s minutes.

                If you did not request this, you can ignore this email.
        """.formatted(code, ttl == null ? "5" : String.valueOf(ttl.toMinutes()));
        MailMessage message = new MailMessage(to, otpSubject, body, Instant.now());
        outbox.add(message);
        log.info("Mail delivery disabled, would send OTP email to {} with code {}", maskEmail(to), maskCode(code));
    }

    public void sendVerifyEmail(String to, String token, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(token)) {
            return;
        }
        String link = frontendBaseUrl + "/app/verify.html?token=" + urlEncode(token) + "&email=" + urlEncode(to);
        String body = """
                Hi!

                Please verify your email for FinGuard.
                Verification code: %s
                Link: %s
                The code is valid for about %s minutes.

                If you did not request this, you can ignore this email.
                """.formatted(token, link, ttl == null ? "60" : String.valueOf(ttl.toMinutes()));

        MailMessage message = new MailMessage(to, verifySubject, body, Instant.now());
        outbox.add(message);
        log.info("Mail delivery disabled, would send verify email to {} with token {}", maskEmail(to), maskCode(token));
    }

    public List<MailMessage> getOutbox() {
        return List.copyOf(outbox);
    }

    public void clearOutbox() {
        outbox.clear();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.replaceAll("/+$", "");
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String maskCode(String code) {
        if (!StringUtils.hasText(code)) {
            return "***";
        }
        int len = code.length();
        if (len <= 2) {
            return "***";
        }
        return "***" + code.substring(len - 2);
    }
}
