package no.moonpark.pricing.model.domain.tariff;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import no.moonpark.pricing.model.domain.Money;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;


/**
 * Zone M3:
 * <ul>
 *   <li>Monday-Saturday, daytime (08:00-16:00): the first hour is free, then a price per started minute.</li>
 *   <li>Monday-Saturday, outside daytime: a price per started minute.</li>
 *   <li>Sunday: free.</li>
 * </ul>
 * Each started minute is priced by the time it starts. The free minutes are per parking session and are only
 * used up by daytime minutes.
 */
public record DaytimeFreeHourTariff(ZoneCode zone,
                                   ZoneId timeZone,
                                   LocalTime daytimeStart,
                                   LocalTime daytimeEnd,
                                   long freeMinutes,
                                   BigDecimal daytimePricePerMinute,
                                   BigDecimal otherPricePerMinute) implements Tariff {

    public DaytimeFreeHourTariff {
        Objects.requireNonNull(zone, "zone is required");
        Objects.requireNonNull(timeZone, "timeZone is required");
        Objects.requireNonNull(daytimeStart, "daytimeStart is required");
        Objects.requireNonNull(daytimeEnd, "daytimeEnd is required");
        if (!daytimeStart.isBefore(daytimeEnd)) {
            throw new IllegalArgumentException("daytimeStart must be before daytimeEnd");
        }
        if (freeMinutes < 0) {
            throw new IllegalArgumentException("freeMinutes must not be negative");
        }
        TariffRules.requireNotNegative(daytimePricePerMinute, "daytimePricePerMinute");
        TariffRules.requireNotNegative(otherPricePerMinute, "otherPricePerMinute");
    }

    @Override
    public Money calculate(ParkingPeriod period) {
        BigDecimal total = BigDecimal.ZERO;
        long freeMinutesLeft = freeMinutes;

        for (long minute = 0; minute < period.startedMinutes(); minute++) {
            ZonedDateTime minuteStart = period.start().plusSeconds(minute * 60).atZone(timeZone);

            if (isSunday(minuteStart)) {
                continue;
            }
            if (!isDaytime(minuteStart)) {
                total = total.add(otherPricePerMinute);
            } else if (freeMinutesLeft > 0) {
                freeMinutesLeft--;
            } else {
                total = total.add(daytimePricePerMinute);
            }
        }
        return Money.nok(total);
    }

    private boolean isSunday(ZonedDateTime time) {
        return time.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean isDaytime(ZonedDateTime time) {
        LocalTime clock = time.toLocalTime();
        return !clock.isBefore(daytimeStart) && clock.isBefore(daytimeEnd);
    }
}
