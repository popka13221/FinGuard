package com.myname.finguard.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportedCurrenciesContainsCny() throws Exception {
        String response = mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode arrayNode = objectMapper.readTree(response);
        assertThat(arrayNode.isArray()).isTrue();

        boolean hasCny = false;
        for (JsonNode node : arrayNode) {
            if ("CNY".equalsIgnoreCase(node.path("code").asText())) {
                hasCny = true;
                assertThat(node.path("name").asText()).isEqualTo("Chinese Yuan");
                break;
            }
        }

        assertThat(hasCny).isTrue();
    }
}
