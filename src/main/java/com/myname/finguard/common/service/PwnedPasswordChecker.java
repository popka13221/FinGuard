package com.myname.finguard.common.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PwnedPasswordChecker {

    private final boolean enabled;
    private final String baseUrl;
    private final Duration timeout;
    private final HttpClient httpClient;

    @Autowired
    public PwnedPasswordChecker(
            @Value("${app.security.pwned-check.enabled:true}") boolean enabled,
            @Value("${app.security.pwned-check.base-url:https://api.pwnedpasswords.com/range/}") String baseUrl,
            @Value("${app.security.pwned-check.timeout-ms:1500}") long timeoutMs
    ) {
        this(enabled, baseUrl, timeoutMs, null);
    }

    static PwnedPasswordChecker createForTest(
            boolean enabled,
            String baseUrl,
            long timeoutMs,
            HttpClient httpClient
    ) {
        return new PwnedPasswordChecker(enabled, baseUrl, timeoutMs, httpClient);
    }

    private PwnedPasswordChecker(
            boolean enabled,
            String baseUrl,
            long timeoutMs,
            HttpClient httpClient
    ) {
        this.enabled = enabled;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.timeout = Duration.ofMillis(timeoutMs);
        this.httpClient = httpClient != null
                ? httpClient
                : HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    public boolean isPwned(String password) {
        if (!enabled || password == null || password.isBlank()) {
            return false;
        }
        try {
            String hash = sha1(password);
            String prefix = hash.substring(0, 5);
            String suffix = hash.substring(5);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + prefix))
                    .timeout(timeout)
                    .header("User-Agent", "FinGuard")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return false;
            }
            String body = response.body();
            if (body == null) {
                return false;
            }
            for (String line : body.split("\\r?\\n")) {
                String[] parts = line.split(":");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(suffix)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
