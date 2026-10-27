package no.moonpark.pricing.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;


/**
 * Reads date-times from the query string.
 * <ul>
 *   <li>With offset: {@code 2026-10-22T08:00:00+02:00} or {@code 2026-10-22T06:00:00Z} - used as given.</li>
 *   <li>Without offset: {@code 2026-10-22T08:00} - read as local time in the garage time zone (Europe/Oslo).</li>
 * </ul>
 * For a local time that happens twice (autumn DST change) the earlier one is used; a local time that does
 * not exist (spring DST change) is moved forward by the length of the gap.
 */
public final class DateTimeParser {

    private DateTimeParser() {
    }

    public static Instant parse(String value, String fieldName, ZoneId timeZone) {
        if (value == null || value.isBlank()) {
            throw new InvalidParkingPeriodException(fieldName + " is required");
        }
        String text = value.trim();
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // no offset given, try local time below
        }
        try {
            return LocalDateTime.parse(text).atZone(timeZone).toInstant();
        } catch (DateTimeParseException e) {
            throw new InvalidParkingPeriodException(
                    fieldName + " must be an ISO-8601 date-time, e.g. 2026-10-22T08:00 or 2026-10-22T08:00:00+02:00");
        }
    }
}
