package com.myname.finguard.staticui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myname.finguard.common.service.MailService;
import jakarta.servlet.http.Cookie;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaticUiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MailService mailService;

    @AfterEach
    void cleanup() {
        mailService.clearOutbox();
    }

    @Test
    void publicStaticPagesAreAccessible() throws Exception {
        List<String> paths = List.of(
                "/app/login.html",
                "/app/forgot.html",
                "/app/reset.html",
                "/app/verify.html",
                "/app/forbidden.html",
                "/app/auth.js",
                "/app/recover.js",
                "/app/verify.js",
                "/app/i18n.js",
                "/app/api.js",
                "/app/theme.js",
                "/app/landing.js",
                "/app/styles.css",
                "/app/assets/white-big-logo.svg"
        );
        for (String path : paths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void registerLoginAllowsDashboardAndAssets() throws Exception {
        String email = "static-flow-" + UUID.randomUUID() + "@example.com";
        String password = "StrongPass1!";

        postJson("/api/auth/register", """
                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                """.formatted(email, password))
                .andExpect(status().isCreated());

        String code = extractCode(latestMailBody());
        assertThat(code).isNotBlank();

        postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, code))
                .andExpect(status().isOk());

        MvcResult login = postJson("/api/auth/login", """
                {"email":"%s","password":"%s"}
                """.formatted(email, password))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> cookieMap = cookies(login);
        String access = cookieMap.get("FG_AUTH");
        assertThat(access).isNotBlank();

        mockMvc.perform(get("/app/dashboard.html").cookie(new Cookie("FG_AUTH", access)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    assertThat(html).contains("wallet-intelligence-link");
                    assertThat(html).contains("balanceMetricSelect");
                    assertThat(html).doesNotContain("FX Radar");
                    assertThat(html).doesNotContain("Курсы монет");
                });

        mockMvc.perform(get("/app/dashboard.js").cookie(new Cookie("FG_AUTH", access)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/app/dashboard.css").cookie(new Cookie("FG_AUTH", access)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions postJson(String url, String payload) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String latestMailBody() {
        List<MailService.MailMessage> outbox = mailService.getOutbox();
        return outbox.isEmpty() ? "" : outbox.get(outbox.size() - 1).body();
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), StandardCharsets.UTF_8);
        }
        Matcher codeLine = Pattern.compile("(?i)code:\\s*([A-Za-z0-9-]{6,})").matcher(body);
        if (codeLine.find()) {
            return codeLine.group(1).trim();
        }
        Matcher digits = Pattern.compile("\\b\\d{6}\\b").matcher(body);
        if (digits.find()) {
            return digits.group().trim();
        }
        Matcher hex = Pattern.compile("\\b(?=[0-9a-fA-F-]*\\d)[0-9a-fA-F-]{6,}\\b").matcher(body);
        return hex.find() ? hex.group().trim() : "";
    }

    private Map<String, String> cookies(MvcResult result) {
        Map<String, String> map = new HashMap<>();
        for (String header : result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)) {
            for (HttpCookie httpCookie : HttpCookie.parse(header)) {
                map.put(httpCookie.getName(), httpCookie.getValue());
            }
        }
        return map;
    }
}
