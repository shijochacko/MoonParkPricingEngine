package no.moonpark.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.domain.tariff.PerStartedMinuteTariff;
import no.moonpark.pricing.model.domain.tariff.VersionedTariff;
import no.moonpark.pricing.validator.ParkingPeriodValidator;

@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {

    private static final ZoneCode M1 = new ZoneCode("M1");
    private static final Instant START = Instant.parse("2026-10-22T06:00:00Z");

    @Mock
    private TariffRegistry tariffRegistry;

    @Spy
    private PricingProperties pricingProperties = new PricingProperties(
            Duration.ofDays(31), Duration.ofSeconds(60), ZoneId.of("Europe/Oslo"));

    @Spy
    private ParkingPeriodValidator parkingPeriodValidator = new ParkingPeriodValidator(
            new PricingProperties(Duration.ofDays(31), Duration.ofSeconds(60), ZoneId.of("Europe/Oslo")));

    @InjectMocks
    private FeeCalculationService service;

    @Test
    void calculatesFeeWithTheTariffInEffectAtStart() {
        VersionedTariff tariff = new VersionedTariff(new PerStartedMinuteTariff(M1, new BigDecimal("60")), "1:0");
        when(tariffRegistry.findEffective(M1, START)).thenReturn(Optional.of(tariff));

        FeeQuote quote = service.calculate(M1, START, START.plusSeconds(61 * 60));

        assertThat(quote.fee().amount()).isEqualByComparingTo("61");
        assertThat(quote.tariffVersion()).isEqualTo("1:0");
        assertThat(quote.zone()).isEqualTo(M1);
    }

    @Test
    void unknownZoneGivesUnknownZoneError() {
        when(tariffRegistry.findEffective(M1, START)).thenReturn(Optional.empty());
        when(tariffRegistry.hasZone(M1)).thenReturn(false);

        assertThatThrownBy(() -> service.calculate(M1, START, START.plusSeconds(60)))
                .isInstanceOf(UnknownZoneException.class)
                .hasMessage("Unknown rate zone: M1");
    }

    @Test
    void knownZoneWithoutTariffAtStartExplainsWhy() {
        when(tariffRegistry.findEffective(M1, START)).thenReturn(Optional.empty());
        when(tariffRegistry.hasZone(M1)).thenReturn(true);

        assertThatThrownBy(() -> service.calculate(M1, START, START.plusSeconds(60)))
                .isInstanceOf(UnknownZoneException.class)
                .hasMessage("Rate zone M1 had no tariff in effect at 2026-10-22T08:00:00+02:00[Europe/Oslo]");
    }

    @Test
    void rejectsPeriodLongerThanMaximumBeforeLookingUpTariff() {
        assertThatThrownBy(() -> service.calculate(M1, START, START.plus(Duration.ofDays(32))))
                .isInstanceOf(InvalidParkingPeriodException.class)
                .hasMessageContaining("cannot be longer than");
        verifyNoInteractions(tariffRegistry);
    }
}
