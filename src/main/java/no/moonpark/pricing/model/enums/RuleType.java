package no.moonpark.pricing.model.enums;

/**
 * The pricing rule types the engine knows. Each value maps to one {@code Tariff} implementation.
 * Adding a value here without handling it in {@code TariffFactory} is a compile error.
 */
public enum RuleType {
    /** Price per hour, charged per started minute (M1). Parameters: hourlyRate. */
    PER_STARTED_MINUTE,
    /** Price per started hour, weekday/weekend (M2). Parameters: weekdayRate, weekendRate. */
    HOURLY_WEEKDAY_WEEKEND,
    /** Daytime free first hour, per-minute prices, Sunday free (M3).
     *  Parameters: dayStart, dayEnd, freeMinutes, dayRate, nightRate. */
    DAYTIME_FREE_HOUR
}
