package no.moonpark.pricing.validator;

import java.time.Duration;

import org.springframework.stereotype.Component;

import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;
import no.moonpark.pricing.model.domain.ParkingPeriod;

/**
 * Checks that a parking period is not longer than allowed (default 31 days).
 * Protects the service from very expensive calculations.
 */
@Component
public class ParkingPeriodValidator {

    private final Duration maxPeriod;

    public ParkingPeriodValidator(PricingProperties properties) {
        this.maxPeriod = properties.maxPeriod();
    }

    public void checkMaxDuration(ParkingPeriod period) {
        if (period.duration().compareTo(maxPeriod) > 0) {
            throw new InvalidParkingPeriodException("Parking period cannot be longer than " + maxPeriod);
        }
    }
}
