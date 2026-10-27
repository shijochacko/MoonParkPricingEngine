package no.moonpark.pricing.service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.stereotype.Component;
import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.domain.tariff.DaytimeFreeHourTariff;
import no.moonpark.pricing.model.domain.tariff.HourlyWeekdayWeekendTariff;
import no.moonpark.pricing.model.domain.tariff.PerStartedMinuteTariff;
import no.moonpark.pricing.model.domain.tariff.Tariff;
import no.moonpark.pricing.model.enums.RuleType;


/**
 * Builds a domain {@link Tariff} from a rule type and its JSON parameters.
 * Throws {@link IllegalArgumentException} with a clear message when a parameter is missing or invalid.
 */
@Component
public class TariffFactory {

    private final ZoneId timeZone;

    public TariffFactory(PricingProperties properties) {
        this.timeZone = properties.timeZone();
    }

    public Tariff createTariff(String zoneCode, RuleType ruleType, Map<String, Object> parameters) {
        if (ruleType == null) {
            throw new IllegalArgumentException("ruleType is required");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters are required");
        }
        ZoneCode zone = new ZoneCode(zoneCode);
        return switch (ruleType) {
            case PER_STARTED_MINUTE -> new PerStartedMinuteTariff(zone, readDecimal(parameters, "hourlyRate"));
            case HOURLY_WEEKDAY_WEEKEND -> new HourlyWeekdayWeekendTariff(zone,
                    readDecimal(parameters, "weekdayRate"),
                    readDecimal(parameters, "weekendRate"),
                    timeZone);
            case DAYTIME_FREE_HOUR -> new DaytimeFreeHourTariff(zone, timeZone,
                    readTime(parameters, "dayStart"),
                    readTime(parameters, "dayEnd"),
                    readLong(parameters, "freeMinutes"),
                    readDecimal(parameters, "dayRate"),
                    readDecimal(parameters, "nightRate"));
        };
    }

    private BigDecimal readDecimal(Map<String, Object> parameters, String name) {
        String value = readText(parameters, name);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tariff parameter '" + name + "' must be a number");
        }
    }

    private long readLong(Map<String, Object> parameters, String name) {
        String value = readText(parameters, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tariff parameter '" + name + "' must be a whole number");
        }
    }

    private LocalTime readTime(Map<String, Object> parameters, String name) {
        String value = readText(parameters, name);
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Tariff parameter '" + name + "' must be a time like 08:00");
        }
    }

    private String readText(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Tariff parameter '" + name + "' is required");
        }
        return value.toString().trim();
    }
}
