package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.common.service.MailService;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountBalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void returnsOnlyCurrentUserAccountsAndAggregatesTotals() throws Exception {
        // register + verify + login main user
        String email = "balance@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!");
        User current = userRepository.findByEmail(email).orElseThrow();

        // create accounts for current user
        accountRepository.save(account(current, "Main", "USD", new BigDecimal("10.50"), false));
        accountRepository.save(account(current, "Archived", "USD", new BigDecimal("5.00"), true));
        accountRepository.save(account(current, "Euro", "EUR", null, false));

        // create account for another user to ensure isolation
        User other = userRepository.save(newUser("other@example.com"));
        accountRepository.save(account(other, "Other", "USD", new BigDecimal("999.00"), false));

        String response = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("accounts")).hasSize(3); // includes archived but only current user's

        JsonNode totals = root.get("totalsByCurrency");
        assertThat(totals).hasSize(2);
        // order is case-insensitive sort, expect EUR then USD
        assertThat(totals.get(0).get("currency").asText()).isEqualTo("EUR");
        assertThat(totals.get(0).get("total").asText()).isEqualTo("0");
        assertThat(totals.get(1).get("currency").asText()).isEqualTo("USD");
        assertThat(totals.get(1).get("total").decimalValue()).isEqualByComparingTo("10.50");
    }

    @Test
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts/balance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void otherUserSeesOnlyOwnEmptyAccounts() throws Exception {
        String otherToken = registerVerifyAndLogin("other-view@example.com", "StrongPass1!");
        // current user has no accounts yet
        String response = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        assertThat(root.get("accounts")).isEmpty();
        assertThat(root.get("totalsByCurrency")).isEmpty();
    }

    @Test
    @Transactional
    void createsAccountAndReflectsInBalance() throws Exception {
        String email = "create-account@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!");

        String createResponse = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {"name":"Visa","currency":"USD","initialBalance":123.45}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        assertThat(created.get("id").asLong()).isPositive();
        assertThat(created.get("name").asText()).isEqualTo("Visa");
        assertThat(created.get("currency").asText()).isEqualTo("USD");
        assertThat(created.get("balance").decimalValue()).isEqualByComparingTo("123.45");
        assertThat(created.get("archived").asBoolean()).isFalse();

        String balanceResponse = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(balanceResponse);
        assertThat(root.get("accounts")).hasSize(1);
        assertThat(root.get("accounts").get(0).get("name").asText()).isEqualTo("Visa");
        assertThat(root.get("totalsByCurrency")).hasSize(1);
        assertThat(root.get("totalsByCurrency").get(0).get("currency").asText()).isEqualTo("USD");
        assertThat(root.get("totalsByCurrency").get(0).get("total").decimalValue()).isEqualByComparingTo("123.45");
    }

    @Test
    @Transactional
    void createAccountRejectsUnsupportedCurrency() throws Exception {
        String token = registerVerifyAndLogin("bad-currency@example.com", "StrongPass1!");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {"name":"Test","currency":"ZZZ","initialBalance":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    private Account account(User user, String name, String currency, BigDecimal balance, boolean archived) {
        Account a = new Account();
        a.setUser(user);
        a.setName(name);
        a.setCurrency(currency);
        a.setCurrentBalance(balance == null ? BigDecimal.ZERO : balance);
        a.setArchived(archived);
        return a;
    }

    private User newUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setRole(Role.USER);
        u.setEmailVerified(true);
        return u;
    }

    private String registerVerifyAndLogin(String email, String password) throws Exception {
        String registerPayload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "Test User",
                  "baseCurrency": "USD"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        // confirm email via code from mail
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
}
