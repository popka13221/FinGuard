package com.myname.finguard.staticui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaticDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardRedirectsToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/app/dashboard.html"))
                .andExpect(status().isFound())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Location")).isEqualTo("/app/login.html"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void dashboardHtmlContainsLiveOnlyDashboardSections() throws Exception {
        String html = mockMvc.perform(get("/app/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("wallet-intelligence-link");
        assertThat(html).contains("upcomingSection");
        assertThat(html).contains("balancePeriodTabs");
        assertThat(html).contains("balanceMetricSelect");
        assertThat(html).doesNotContain("FX Radar");
        assertThat(html).doesNotContain("Курсы монет");
    }
}
