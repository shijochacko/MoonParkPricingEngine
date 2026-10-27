package no.moonpark.pricing.model.domain.tariff;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import no.moonpark.pricing.model.domain.Money;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;


/**
 * Zone M1: a price per hour, charged for every started minute.
 * Example: 60 kr/hour, 61 minutes = 61 kr.
 */
public record PerStartedMinuteTariff(ZoneCode zone, BigDecimal pricePerHour) implements Tariff {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    public PerStartedMinuteTariff {
        Objects.requireNonNull(zone, "zone is required");
        TariffRules.requireNotNegative(pricePerHour, "pricePerHour");
    }

    @Override
    public Money calculate(ParkingPeriod period) {
        BigDecimal minutes = BigDecimal.valueOf(period.startedMinutes());
        BigDecimal total = pricePerHour.multiply(minutes).divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
        return Money.nok(total);
    }
}
