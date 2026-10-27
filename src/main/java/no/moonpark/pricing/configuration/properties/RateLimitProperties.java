package no.moonpark.pricing.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings under {@code ratelimit.*}.
 *
 * @param enabled          turn rate limiting on or off
 * @param store            {@code local} (per pod) or {@code redis} (shared by all pods)
 * @param redisKeyPrefix   prefix for Redis keys
 * @param batchRequestCost tokens used by one batch request (a batch does up to 100 calculations)
 */
@ConfigurationProperties(prefix = "ratelimit")
public record RateLimitProperties(@DefaultValue("true") boolean enabled,
                                  @DefaultValue("local") String store,
                                  @DefaultValue("moonpark:rl:") String redisKeyPrefix,
                                  @DefaultValue("10") long batchRequestCost) {
}
