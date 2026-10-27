package no.moonpark.pricing.service;

import java.time.Instant;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.domain.tariff.VersionedTariff;
import no.moonpark.pricing.validator.ParkingPeriodValidator;

/**
 * Calculates parking fees.
 * <ul>
 *   <li>The tariff in effect at the <b>start</b> of parking is used.</li>
 *   <li>Results are cached for a short time: the same question always gives the same answer.</li>
 *   <li>A bulkhead limits concurrent calculations; when full, callers get 503 immediately (load shedding).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FeeCalculationService implements CalculateFeeUseCase {

    public static final String FEE_CACHE = "fees";

    private final TariffRegistry tariffRegistry;
    private final ParkingPeriodValidator parkingPeriodValidator;
    private final PricingProperties pricingProperties;

    @Override
    @Bulkhead(name = "feeCalculation")
    @Cacheable(cacheNames = FEE_CACHE, key = "#zone.value() + '|' + #start + '|' + #end")
    public FeeQuote calculate(ZoneCode zone, Instant start, Instant end) {
        ParkingPeriod period = new ParkingPeriod(start, end);
        parkingPeriodValidator.checkMaxDuration(period);
        VersionedTariff tariff = tariffRegistry.findEffective(zone, start)
                .orElseThrow(() -> tariffRegistry.hasZone(zone)
                        ? new UnknownZoneException(zone, start.atZone(pricingProperties.timeZone()))
                        : new UnknownZoneException(zone));
        return new FeeQuote(zone, period, tariff.tariff().calculate(period), tariff.version());
    }
}
