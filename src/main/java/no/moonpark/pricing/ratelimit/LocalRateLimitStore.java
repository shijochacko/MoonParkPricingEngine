package no.moonpark.pricing.ratelimit;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;

/**
 * In-memory buckets for one pod. Idle buckets are removed after 10 minutes, and the number of
 * buckets is capped, so many different callers (or IPs) cannot fill the memory.
 */
public class LocalRateLimitStore implements RateLimitStore {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @Override
    public RateLimitResult tryConsume(String key, RateLimitPlan plan, long tokens) {
        Bucket bucket = buckets.get(key + "|" + plan.name(), ignored -> createBucket(plan));
        return Buckets.toResult(bucket.tryConsumeAndReturnRemaining(tokens));
    }

    private Bucket createBucket(RateLimitPlan plan) {
        return Bucket.builder().addLimit(Buckets.limitFor(plan)).build();
    }
}
