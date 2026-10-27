package no.moonpark.pricing.ratelimit;

/**
 * Stores token buckets. Two implementations:
 * <ul>
 *   <li>{@link LocalRateLimitStore} - in memory, per pod. Good for local runs and as a fallback.</li>
 *   <li>{@link RedisRateLimitStore} - shared by all pods, so limits are exact when scaled out.</li>
 * </ul>
 */
public interface RateLimitStore {

    RateLimitResult tryConsume(String key, RateLimitPlan plan, long tokens);
}
