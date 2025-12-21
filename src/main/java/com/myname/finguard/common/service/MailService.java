package com.myname.finguard.common.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final boolean enabled;
    private final String from;
    private final String resetSubject;
    private final String verifySubject;
    private final String otpSubject;
    private final String frontendBaseUrl;
    private final JavaMailSender mailSender;
    private final CopyOnWriteArrayList<MailMessage> outbox = new CopyOnWriteArrayList<>();

    public record MailMessage(String to, String subject, String body, Instant createdAt) {
    }

    public MailService(@Value("${app.mail.enabled:false}") boolean enabled,
                       @Value("${app.mail.from:no-reply@finguard.local}") String from,
                       @Value("${app.mail.reset-subject:Сброс пароля FinGuard}") String resetSubject,
                       @Value("${app.mail.verify-subject:Подтверждение email FinGuard}") String verifySubject,
                       @Value("${app.mail.otp-subject:Код входа FinGuard}") String otpSubject,
                       @Value("${app.frontend.base-url:http://localhost:8080}") String frontendBaseUrl,
                       @Autowired(required = false) JavaMailSender mailSender) {
        this.enabled = enabled;
        this.from = from;
        this.resetSubject = resetSubject;
        this.verifySubject = verifySubject;
        this.otpSubject = otpSubject;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String to, String token, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(token)) {
            return;
        }
        String link = frontendBaseUrl + "/app/reset.html?token=" + urlEncode(token) + "&email=" + urlEncode(to);
        String body = """
                Привет!

                Вы запросили смену пароля в FinGuard.
                Код для ввода: %s
                Ссылка: %s
                Код действует примерно %s минут. Он одноразовый.

                Если запрос сделали не вы — просто игнорируйте письмо.
                """.formatted(token, link, ttl == null ? "60" : String.valueOf(ttl.toMinutes()));

        MailMessage message = new MailMessage(to, resetSubject, body, Instant.now());
        outbox.add(message);

        if (!enabled || mailSender == null) {
            log.info("Mail disabled, would send reset email to {} with code {}", maskEmail(to), maskCode(token));
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(to);
            mail.setSubject(resetSubject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            log.warn("Failed to send reset email to {}", maskEmail(to), e);
        }
    }

    public void sendOtpEmail(String to, String code, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(code)) {
            return;
        }
        String body = """
                Привет!

                Ваш код для входа в FinGuard: %s
                Код действует примерно %s минут.

                Если запрос сделали не вы — игнорируйте письмо.
                """.formatted(code, ttl == null ? "5" : String.valueOf(ttl.toMinutes()));
        MailMessage message = new MailMessage(to, otpSubject, body, Instant.now());
        outbox.add(message);

        if (!enabled || mailSender == null) {
            log.info("Mail disabled, would send OTP email to {} with code {}", maskEmail(to), maskCode(code));
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(to);
            mail.setSubject(otpSubject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}", maskEmail(to), e);
        }
    }

    public void sendVerifyEmail(String to, String token, Duration ttl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(token)) {
            return;
        }
        String link = frontendBaseUrl + "/app/verify.html?token=" + urlEncode(token) + "&email=" + urlEncode(to);
        String body = """
                Привет!

                Подтвердите ваш email для FinGuard.
                Код: %s
                Ссылка: %s
                Код действует примерно %s минут.

                Если запрос сделали не вы — игнорируйте письмо.
                """.formatted(token, link, ttl == null ? "60" : String.valueOf(ttl.toMinutes()));

        MailMessage message = new MailMessage(to, verifySubject, body, Instant.now());
        outbox.add(message);

        if (!enabled || mailSender == null) {
            log.info("Mail disabled, would send verify email to {} with token {}", maskEmail(to), maskCode(token));
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(to);
            mail.setSubject(verifySubject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            log.warn("Failed to send verify email to {}", maskEmail(to), e);
        }
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
