package no.moonpark.pricing.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.moonpark.pricing.configuration.properties.PricingProperties;

class JacksonConfigTest {

    @Test
    void serializesInstantWithDefaultConfiguredTimeZone() throws Exception {
        assertSerialization(ZoneId.of("Europe/Oslo"),
                "\"2026-10-23T21:38:15.093746+02:00[Europe/Oslo]\"");
    }

    @Test
    void serializesInstantWithOverriddenConfiguredTimeZone() throws Exception {
        assertSerialization(ZoneId.of("America/New_York"),
                "\"2026-10-23T15:38:15.093746-04:00[America/New_York]\"");
    }

    private void assertSerialization(ZoneId zoneId, String expected) throws Exception {
        PricingProperties properties = new PricingProperties(Duration.ofDays(31), Duration.ofSeconds(60), zoneId);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JacksonConfig().instantSerializationModule(properties));

        assertThat(objectMapper.writeValueAsString(Instant.parse("2026-10-23T19:38:15.093746Z")))
                .isEqualTo(expected);
    }
}
