package no.moonpark.pricing.ratelimit;

import java.util.Locale;

/**
 * Request limits per caller type. {@code burst} is the bucket size (how many requests can come at once),
 * {@code requestsPerMinute} is how fast the bucket refills.
 */
public enum RateLimitPlan {

    ANONYMOUS(10, 10),
    FREE(60, 20),
    STANDARD(600, 100),
    PARTNER(6_000, 500),
    INTERNAL(6_000, 500);

    private final long requestsPerMinute;
    private final long burst;

    RateLimitPlan(long requestsPerMinute, long burst) {
        this.requestsPerMinute = requestsPerMinute;
        this.burst = burst;
    }

    public long requestsPerMinute() {
        return requestsPerMinute;
    }

    public long burst() {
        return burst;
    }

    /** Unknown or missing plan codes get the smallest paid plan, never more. */
    public static RateLimitPlan fromCode(String code) {
        if (code == null || code.isBlank()) {
            return FREE;
        }
        try {
            return valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return FREE;
        }
    }
}
