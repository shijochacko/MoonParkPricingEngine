package no.moonpark.pricing.exceptionhandler.exception;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import no.moonpark.pricing.model.domain.ZoneCode;

/**
 * No tariff can be used: either the zone does not exist, or it had no tariff yet when parking started.
 */
public class UnknownZoneException extends RuntimeException {

    public UnknownZoneException(ZoneCode zone) {
        super("Unknown rate zone: " + zone.value());
    }

    public UnknownZoneException(ZoneCode zone, ZonedDateTime start) {
        super("Rate zone " + zone.value() + " had no tariff in effect at "
                + DateTimeFormatter.ISO_ZONED_DATE_TIME.format(start));
    }
}
