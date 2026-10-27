package no.moonpark.pricing.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.micrometer.core.instrument.MeterRegistry;
import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.configuration.properties.RateLimitProperties;
import no.moonpark.pricing.controller.TariffController;
import no.moonpark.pricing.mapper.FeeResponseMapper;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.domain.Money;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.dto.fee.FeeResponse;
import no.moonpark.pricing.ratelimit.RateLimitPlan;
import no.moonpark.pricing.ratelimit.RateLimitResult;
import no.moonpark.pricing.ratelimit.RateLimitStore;
import no.moonpark.pricing.security.ProblemAccessDeniedHandler;
import no.moonpark.pricing.security.ProblemAuthenticationEntryPoint;
import no.moonpark.pricing.service.ApiClientService;
import no.moonpark.pricing.service.CalculateFeeUseCase;

@WebMvcTest(TariffController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, PublicFeeEndpointSecurityTest.RateLimitTestConfiguration.class})
class PublicFeeEndpointSecurityTest {

    private static final Instant START = Instant.parse("2026-10-22T06:00:00Z");
    private static final Instant END = START.plusSeconds(60);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiClientService apiClientService;

    @MockitoBean
    private ProblemAuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private ProblemAccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private RateLimitStore rateLimitStore;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CalculateFeeUseCase calculateFee;

    @MockitoBean
    private FeeResponseMapper feeResponseMapper;

    @ParameterizedTest
    @ValueSource(strings = {"/takst", "/api/v1/takst"})
    void allowsAnonymousFeeCalculationAndStillAppliesRateLimit(String path) throws Exception {
        ZoneCode zone = new ZoneCode("M1");
        when(rateLimitStore.tryConsume(anyString(), any(RateLimitPlan.class), anyLong()))
                .thenReturn(RateLimitResult.allowed(9));
        when(calculateFee.calculate(any(ZoneCode.class), any(Instant.class), any(Instant.class)))
                .thenReturn(new FeeQuote(zone, new ParkingPeriod(START, END), Money.nok(new BigDecimal("1.00")), "1:0"));
        when(feeResponseMapper.toResponse(any(FeeQuote.class), any(ZoneId.class)))
                .thenReturn(new FeeResponse("M1", OffsetDateTime.parse("2026-10-22T08:00:00+02:00"),
                        OffsetDateTime.parse("2026-10-22T08:01:00+02:00"), 1,
                        new BigDecimal("1.00"), "NOK", "1:0"));

        mockMvc.perform(get(path)
                        .param("zone", "M1")
                        .param("start", "2026-10-22T08:00")
                        .param("end", "2026-10-22T08:01"))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Limit", "10"))
                .andExpect(header().string("RateLimit-Remaining", "9"));

        verify(rateLimitStore).tryConsume(anyString(), org.mockito.ArgumentMatchers.eq(RateLimitPlan.ANONYMOUS), anyLong());
    }

    @TestConfiguration
    static class RateLimitTestConfiguration {

        @Bean
        RateLimitProperties rateLimitProperties() {
            return new RateLimitProperties(true, "local", "test:ratelimit:", 10);
        }

        @Bean
        PricingProperties pricingProperties() {
            return new PricingProperties(Duration.ofDays(31), Duration.ofSeconds(60), ZoneId.of("Europe/Oslo"));
        }
    }
}
