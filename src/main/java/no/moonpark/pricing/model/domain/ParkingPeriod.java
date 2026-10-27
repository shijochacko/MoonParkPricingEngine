package no.moonpark.pricing.model.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;


/**
 * A parking session from {@code start} (inclusive) to {@code end} (exclusive), as absolute instants.
 * Using {@link Instant} means the duration is the real elapsed time, also across daylight-saving changes.
 */
public record ParkingPeriod(Instant start, Instant end) {

    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MILLIS_PER_HOUR = 3_600_000L;

    public ParkingPeriod {
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(end, "end is required");
        if (end.isBefore(start)) {
            throw new InvalidParkingPeriodException("end must not be before start");
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /** Number of started minutes. 0 seconds = 0, 1 second = 1, 60 seconds = 1, 61 seconds = 2. */
    public long startedMinutes() {
        return Math.ceilDiv(duration().toMillis(), MILLIS_PER_MINUTE);
    }

    /** Number of started hours. 1 minute = 1, 60 minutes = 1, 61 minutes = 2. */
    public long startedHours() {
        return Math.ceilDiv(duration().toMillis(), MILLIS_PER_HOUR);
    }
}
