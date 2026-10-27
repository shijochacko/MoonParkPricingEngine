package no.moonpark.pricing.configuration.properties;

import java.time.Duration;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Pricing settings.
 *
 * @param maxPeriod     longest parking period a client may ask about (protects the service from huge loops)
 * @param tariffRefresh how often each instance reloads tariff definitions from the database
 * @param timeZone      local time zone used by all tariff rules (weekday, daytime, ...)
 */
@ConfigurationProperties("pricing")
public record PricingProperties(@DefaultValue("P31D") Duration maxPeriod,
                                @DefaultValue("PT60S") Duration tariffRefresh,
                                @DefaultValue("Europe/Oslo") ZoneId timeZone) {
}
