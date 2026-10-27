package no.moonpark.pricing.model.domain.tariff;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;

@DisplayName("M1 - 60 kr per hour, charged per started minute")
class PerStartedMinuteTariffTest {

    private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");
    private final PerStartedMinuteTariff m1 = new PerStartedMinuteTariff(new ZoneCode("M1"), new BigDecimal("60"));

    @ParameterizedTest(name = "{0} -> {1} costs {2} kr")
    @CsvSource({
            "2026-10-22T08:00:00, 2026-10-22T08:00:00, 0.00",
            "2026-10-22T08:00:00, 2026-10-22T08:00:01, 1.00",
            "2026-10-22T08:00:00, 2026-10-22T08:01:00, 1.00",
            "2026-10-22T08:00:00, 2026-10-22T08:01:01, 2.00",
            "2026-10-22T08:00:00, 2026-10-22T09:00:00, 60.00",
            "2026-10-22T08:00:00, 2026-10-22T09:01:00, 61.00",
            "2026-10-22T08:00:00, 2026-10-23T08:00:00, 1440.00"
    })
    void chargesEveryStartedMinute(String start, String end, String expected) {
        ParkingPeriod period = period(start, end);

        assertThat(m1.calculate(period).amount()).isEqualByComparingTo(expected);
    }

    @Test
    void usesRealElapsedTimeAcrossDaylightSavingChange() {
        // 25 Oct 2026: clocks go back at 03:00, so 00:00-06:00 local is 7 real hours
        ParkingPeriod period = period("2026-10-25T00:00:00", "2026-10-25T06:00:00");

        assertThat(m1.calculate(period).amount()).isEqualByComparingTo("420.00");
    }

    static ParkingPeriod period(String start, String end) {
        return new ParkingPeriod(ZonedDateTime.of(LocalDateTime.parse(start), OSLO).toInstant(),
                ZonedDateTime.of(LocalDateTime.parse(end), OSLO).toInstant());
    }
}
