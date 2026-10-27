package no.moonpark.pricing.model.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;

class InputValidationTest {

    private static final Instant START = Instant.parse("2026-10-22T06:00:00Z");

    @Test
    void zoneCodeIsTrimmedAndUpperCased() {
        assertThat(new ZoneCode(" m1 ")).hasToString("M1");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"M-1", "ABCDEFGHIJK"})
    void invalidZoneCodeIsRejected(String value) {
        assertThatThrownBy(() -> new ZoneCode(value)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void endBeforeStartIsRejected() {
        assertThatThrownBy(() -> new ParkingPeriod(START, START.minusSeconds(1)))
                .isInstanceOf(InvalidParkingPeriodException.class)
                .hasMessage("end must not be before start");
    }
}
