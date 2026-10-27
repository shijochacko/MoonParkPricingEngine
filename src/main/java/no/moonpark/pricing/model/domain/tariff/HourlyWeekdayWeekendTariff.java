package no.moonpark.pricing.model.domain.tariff;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import no.moonpark.pricing.model.domain.Money;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;


/**
 * Zone M2: a price for every started hour. Hours that start on Saturday or Sunday use the weekend price.
 * Example: 100 kr/hour on weekdays, 200 kr/hour on weekends.
 */
public record HourlyWeekdayWeekendTariff(ZoneCode zone,
                                         BigDecimal weekdayPricePerHour,
                                         BigDecimal weekendPricePerHour,
                                         ZoneId timeZone) implements Tariff {

    public HourlyWeekdayWeekendTariff {
        Objects.requireNonNull(zone, "zone is required");
        Objects.requireNonNull(timeZone, "timeZone is required");
        TariffRules.requireNotNegative(weekdayPricePerHour, "weekdayPricePerHour");
        TariffRules.requireNotNegative(weekendPricePerHour, "weekendPricePerHour");
    }

    @Override
    public Money calculate(ParkingPeriod period) {
        BigDecimal total = BigDecimal.ZERO;
        for (long hour = 0; hour < period.startedHours(); hour++) {
            ZonedDateTime hourStart = period.start().plus(Duration.ofHours(hour)).atZone(timeZone);
            total = total.add(isWeekend(hourStart) ? weekendPricePerHour : weekdayPricePerHour);
        }
        return Money.nok(total);
    }

    private boolean isWeekend(ZonedDateTime time) {
        DayOfWeek day = time.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
