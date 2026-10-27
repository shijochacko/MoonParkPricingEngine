package no.moonpark.pricing.configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import no.moonpark.pricing.configuration.properties.PricingProperties;

@Configuration
public class JacksonConfig {

    @Bean
    SimpleModule instantSerializationModule(PricingProperties pricingProperties) {
        ZoneId timeZone = pricingProperties.timeZone();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new JsonSerializer<>() {
            @Override
            public void serialize(Instant value, JsonGenerator generator, SerializerProvider serializers)
                    throws IOException {
                generator.writeString(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value.atZone(timeZone)));
            }
        });
        return module;
    }
}
