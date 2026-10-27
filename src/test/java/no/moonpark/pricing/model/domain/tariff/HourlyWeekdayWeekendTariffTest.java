package no.moonpark.pricing.model.domain.tariff;

import static no.moonpark.pricing.model.domain.tariff.PerStartedMinuteTariffTest.period;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import no.moonpark.pricing.model.domain.ZoneCode;

@DisplayName("M2 - 100 kr per started hour on weekdays, 200 kr on weekends")
class HourlyWeekdayWeekendTariffTest {

    private final HourlyWeekdayWeekendTariff m2 = new HourlyWeekdayWeekendTariff(new ZoneCode("M2"),
            new BigDecimal("100"), new BigDecimal("200"), ZoneId.of("Europe/Oslo"));

    // 2026-10-22 is a Thursday, 2026-10-24 a Saturday, 2026-10-25 a Sunday
    @ParameterizedTest(name = "{0} -> {1} costs {2} kr")
    @CsvSource({
            "2026-10-22T08:00:00, 2026-10-22T08:00:00, 0",
            "2026-10-22T08:00:00, 2026-10-22T08:00:01, 100",
            "2026-10-22T08:00:00, 2026-10-22T09:00:00, 100",
            "2026-10-22T08:00:00, 2026-10-22T09:01:00, 200",
            "2026-10-24T08:00:00, 2026-10-24T09:00:00, 200",
            "2026-10-24T08:00:00, 2026-10-24T10:30:00, 600",
            "2026-10-25T10:00:00, 2026-10-25T11:00:00, 200"
    })
    void chargesPerStartedHourByDayType(String start, String end, String expected) {
        assertThat(m2.calculate(period(start, end)).amount()).isEqualByComparingTo(expected);
    }

    @Test
    void eachHourIsPricedByTheDayItStartsOn() {
        // Friday 23:30 -> Saturday 00:45: hour 1 starts Friday (100), hour 2 starts Saturday 00:30 (200)
        assertThat(m2.calculate(period("2026-10-23T23:30:00", "2026-10-24T00:45:00")).amount())
                .isEqualByComparingTo("300");
    }


    @Test
    void countsRealHoursOnDaylightSavingNight() {
        // 01:00-04:00 local on DST night is 4 real hours, all on Sunday
        assertThat(m2.calculate(period("2026-10-25T01:00:00", "2026-10-25T04:00:00")).amount())
                .isEqualByComparingTo("800");
    }
}
