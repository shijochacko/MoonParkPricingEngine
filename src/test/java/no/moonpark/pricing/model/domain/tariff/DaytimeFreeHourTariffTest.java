package no.moonpark.pricing.model.domain.tariff;

import static no.moonpark.pricing.model.domain.tariff.PerStartedMinuteTariffTest.period;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import no.moonpark.pricing.model.domain.ZoneCode;

@DisplayName("M3 - Mon-Sat 08-16 first hour free then 2 kr/min, otherwise 3 kr/min, Sunday free")
class DaytimeFreeHourTariffTest {

    private final DaytimeFreeHourTariff m3 = new DaytimeFreeHourTariff(new ZoneCode("M3"), ZoneId.of("Europe/Oslo"),
            LocalTime.of(8, 0), LocalTime.of(16, 0), 60, new BigDecimal("2"), new BigDecimal("3"));

    // 2026-10-22 is a Thursday, 2026-10-24 a Saturday, 2026-10-25 a Sunday
    @ParameterizedTest(name = "{3}: {0} -> {1} costs {2} kr")
    @CsvSource({
            "2026-10-22T09:00:00, 2026-10-22T10:00:00, 0,   first daytime hour is free",
            "2026-10-22T09:00:00, 2026-10-22T10:01:00, 2,   one paid daytime minute after free hour",
            "2026-10-22T09:00:00, 2026-10-22T11:00:00, 120, second daytime hour at 2 kr/min",
            "2026-10-22T16:00:00, 2026-10-22T17:00:00, 180, evening at 3 kr/min",
            "2026-10-22T06:00:00, 2026-10-22T07:00:00, 180, early morning at 3 kr/min",
            "2026-10-22T07:30:00, 2026-10-22T09:30:00, 150, morning: 30 min x 3 then free hour then 30 min x 2",
            "2026-10-22T15:30:00, 2026-10-22T16:30:00, 90,  free minutes do not cover evening minutes",
            "2026-10-24T09:00:00, 2026-10-24T11:00:00, 120, Saturday daytime is priced like weekdays",
            "2026-10-25T10:00:00, 2026-10-25T12:00:00, 0,   Sunday is free",
            "2026-10-24T23:00:00, 2026-10-25T01:00:00, 180, Saturday night paid then Sunday free",
            "2026-10-25T23:00:00, 2026-10-26T01:00:00, 180, Sunday free then Monday night paid",
            "2026-10-22T08:00:00, 2026-10-22T08:00:30, 0,   a started minute inside the free hour"
    })
    void calculatesM3Fees(String start, String end, String expected, String description) {
        assertThat(m3.calculate(period(start, end)).amount()).isEqualByComparingTo(expected);
    }

    @Test
    void freeHourIsUsedOnlyOncePerParking() {
        // Thursday 09:00 -> Friday 10:00: one free hour Thursday, Friday daytime fully paid
        // Thu 10-16 = 360 min x 2 = 720, Thu 16-24 = 480 x 3 = 1440, Fri 00-08 = 480 x 3 = 1440, Fri 08-10 = 120 x 2 = 240
        assertThat(m3.calculate(period("2026-10-22T09:00:00", "2026-10-23T10:00:00")).amount())
                .isEqualByComparingTo("3840");
    }


}
