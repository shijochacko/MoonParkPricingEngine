package no.moonpark.pricing.service;

import java.time.Instant;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.domain.ZoneCode;


/**
 * Inbound port: calculate the parking fee for one zone and period.
 */
public interface CalculateFeeUseCase {

    FeeQuote calculate(ZoneCode zone, Instant start, Instant end);
}
