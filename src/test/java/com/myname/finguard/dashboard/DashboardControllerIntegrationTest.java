package com.myname.finguard.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.model.WalletInsight;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.crypto.repository.WalletInsightRepository;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.dashboard.overview.cache-ttl-ms=0"
})
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoWalletRepository cryptoWalletRepository;

    @Autowired
    private WalletInsightRepository walletInsightRepository;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void returnsDashboardOverviewSnapshotForAuthenticatedUser() throws Exception {
        String email = "overview-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");

        String response = mockMvc.perform(get("/api/dashboard/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        assertThat(payload.has("asOf")).isTrue();
        assertThat(payload.has("dataFreshness")).isTrue();
        assertThat(payload.has("hero")).isTrue();
        assertThat(payload.has("stats")).isTrue();
        assertThat(payload.has("getStarted")).isTrue();
        assertThat(payload.has("transactionsPreview")).isTrue();
        assertThat(payload.has("walletsPreview")).isTrue();
        assertThat(payload.has("upcomingPaymentsPreview")).isTrue();
        assertThat(payload.has("walletIntelligence")).isTrue();

        JsonNode hero = payload.get("hero");
        assertThat(hero.has("netWorth")).isTrue();
        assertThat(hero.has("baseCurrency")).isTrue();
        assertThat(hero.has("delta7dPct")).isTrue();
        assertThat(hero.has("hasMeaningfulData")).isTrue();
    }

    @Test
    @Transactional
    void returnsUpcomingPaymentsFromPersistedWalletInsights() throws Exception {
        String email = "upcoming-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");
        User user = userRepository.findByEmail(email).orElseThrow();

        CryptoWallet wallet = new CryptoWallet();
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);
        wallet = cryptoWalletRepository.save(wallet);

        WalletInsight recurring = new WalletInsight();
        recurring.setUser(user);
        recurring.setWallet(wallet);
        recurring.setInsightType("RECURRING_SPEND");
        recurring.setTitle("Recurring spend");
        recurring.setValue(new BigDecimal("42.50"));
        recurring.setUnit("BASE_CURRENCY");
        recurring.setCurrency("USD");
        recurring.setLabel("Streaming");
        recurring.setAvgAmount(new BigDecimal("10.62"));
        recurring.setNextEstimatedChargeAt(Instant.now().plusSeconds(3 * 24 * 3600));
        recurring.setConfidence(new BigDecimal("0.91"));
        recurring.setSynthetic(false);
        recurring.setSource("LIVE");
        recurring.setAsOf(Instant.now());
        walletInsightRepository.save(recurring);

        String response = mockMvc.perform(get("/api/dashboard/upcoming-payments?limit=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode items = objectMapper.readTree(response);
        assertThat(items.isArray()).isTrue();
        assertThat(items.size()).isGreaterThanOrEqualTo(1);
        JsonNode first = items.get(0);
        assertThat(first.get("title").asText()).isEqualTo("Streaming");
        assertThat(first.get("currency").asText()).isEqualTo("USD");
        assertThat(first.get("source").asText()).isEqualTo("LIVE");
        assertThat(first.get("amount").decimalValue()).isEqualByComparingTo("-42.50");
    }

    @Test
    @Transactional
    void normalizesUnknownUpcomingInsightSourceToEstimated() throws Exception {
        String email = "upcoming-source-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");
        User user = userRepository.findByEmail(email).orElseThrow();

        CryptoWallet wallet = new CryptoWallet();
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);
        wallet = cryptoWalletRepository.save(wallet);

        WalletInsight recurring = new WalletInsight();
        recurring.setUser(user);
        recurring.setWallet(wallet);
        recurring.setInsightType("RECURRING_SPEND");
        recurring.setTitle("Recurring spend");
        recurring.setValue(new BigDecimal("19.99"));
        recurring.setUnit("BASE_CURRENCY");
        recurring.setCurrency("USD");
        recurring.setLabel("Subscription");
        recurring.setNextEstimatedChargeAt(Instant.now().plusSeconds(5 * 24 * 3600));
        recurring.setConfidence(new BigDecimal("0.77"));
        recurring.setSynthetic(false);
        recurring.setSource("UNKNOWN_SOURCE");
        recurring.setAsOf(Instant.now());
        walletInsightRepository.save(recurring);

        String response = mockMvc.perform(get("/api/dashboard/upcoming-payments?limit=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode items = objectMapper.readTree(response);
        assertThat(items.isArray()).isTrue();
        assertThat(items.size()).isGreaterThanOrEqualTo(1);
        JsonNode first = items.get(0);
        assertThat(first.get("title").asText()).isEqualTo("Subscription");
        assertThat(first.get("source").asText()).isEqualTo("ESTIMATED");
        assertThat(first.get("amount").decimalValue()).isEqualByComparingTo("-19.99");
    }

    private String registerVerifyAndLogin(String email, String password, String baseCurrency) throws Exception {
        String registerPayload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "Test User",
                  "baseCurrency": "%s"
                }
                """.formatted(email, password, baseCurrency);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String verifyToken = extractCode(msg.body());
        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyToken)))
                .andExpect(status().isOk());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginResponse);
        return loginNode.get("token").asText();
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), java.nio.charset.StandardCharsets.UTF_8);
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
}
