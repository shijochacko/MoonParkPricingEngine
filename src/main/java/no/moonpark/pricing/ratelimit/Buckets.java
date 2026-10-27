package no.moonpark.pricing.ratelimit;

import java.time.Duration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;

/**
 * Small helpers shared by the bucket stores.
 */
final class Buckets {

    private Buckets() {
    }

    static Bandwidth limitFor(RateLimitPlan plan) {
        return Bandwidth.builder()
                .capacity(plan.burst())
                .refillGreedy(plan.requestsPerMinute(), Duration.ofMinutes(1))
                .build();
    }

    static BucketConfiguration configurationFor(RateLimitPlan plan) {
        return BucketConfiguration.builder().addLimit(limitFor(plan)).build();
    }

    static RateLimitResult toResult(ConsumptionProbe probe) {
        if (probe.isConsumed()) {
            return RateLimitResult.allowed(probe.getRemainingTokens());
        }
        long waitSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
        return RateLimitResult.rejected(waitSeconds);
    }
}
