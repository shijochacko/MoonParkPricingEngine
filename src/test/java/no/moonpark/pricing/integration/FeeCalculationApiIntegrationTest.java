package no.moonpark.pricing.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeeCalculationApiIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void calculatesM1FeeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/takst")
                        .param("zone", "M1")
                        .param("start", "2026-10-22T08:00")
                        .param("end", "2026-10-22T09:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(61.00))
                .andExpect(jsonPath("$.currency").value("NOK"));
    }

    @Test
    void calculatesM2WeekdayFeeThroughVersionedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/takst")
                        .param("zone", "M2")
                        .param("start", "2026-10-22T08:00")
                        .param("end", "2026-10-22T09:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.currency").value("NOK"));
    }

    @Test
    void calculatesM3FreeHourThenChargesDayRate() throws Exception {
        mockMvc.perform(get("/takst")
                        .param("zone", "M3")
                        .param("start", "2026-10-22T09:00")
                        .param("end", "2026-10-22T10:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(2.00))
                .andExpect(jsonPath("$.currency").value("NOK"));
    }

    @Test
    void returnsNotFoundForUnknownZone() throws Exception {
        mockMvc.perform(get("/takst")
                        .param("zone", "M8")
                        .param("start", "2026-10-22T08:00")
                        .param("end", "2026-10-22T09:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Unknown rate zone: M8"));
    }
}
