package no.moonpark.pricing.ratelimit;

/**
 * Result of one rate limit check.
 *
 * @param allowed           true if the request may continue
 * @param remaining         tokens left in the bucket after this request
 * @param retryAfterSeconds when rejected: seconds until enough tokens are available again
 */
public record RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {

    public static RateLimitResult allowed(long remaining) {
        return new RateLimitResult(true, remaining, 0);
    }

    public static RateLimitResult rejected(long retryAfterSeconds) {
        return new RateLimitResult(false, 0, Math.max(1, retryAfterSeconds));
    }
}
