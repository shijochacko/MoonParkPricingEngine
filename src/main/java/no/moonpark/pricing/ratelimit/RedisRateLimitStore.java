package no.moonpark.pricing.ratelimit;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.DisposableBean;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.extern.slf4j.Slf4j;

/**
 * Buckets stored in Redis, shared by all pods.
 * <p>
 * If Redis is down or slow, the request is <b>not</b> rejected. Instead the pod falls back to its own
 * {@link LocalRateLimitStore} for 30 seconds and then tries Redis again. This keeps the pricing API
 * available while still protecting each pod.
 */
@Slf4j
public class RedisRateLimitStore implements RateLimitStore, DisposableBean {

    static final Duration RETRY_REDIS_AFTER = Duration.ofSeconds(30);
    static final Duration REDIS_TIMEOUT = Duration.ofMillis(200);

    private final RedisURI redisUri;
    private final String keyPrefix;
    private final LocalRateLimitStore fallbackStore;
    private final Clock clock;

    private RedisClient redisClient;
    private ProxyManager<byte[]> proxyManager;
    private volatile Instant redisDisabledUntil = Instant.MIN;

    public RedisRateLimitStore(RedisURI redisUri, String keyPrefix, LocalRateLimitStore fallbackStore, Clock clock) {
        this.redisUri = redisUri;
        this.keyPrefix = keyPrefix;
        this.fallbackStore = fallbackStore;
        this.clock = clock;
    }

    @Override
    public RateLimitResult tryConsume(String key, RateLimitPlan plan, long tokens) {
        if (clock.instant().isBefore(redisDisabledUntil)) {
            return fallbackStore.tryConsume(key, plan, tokens);
        }
        try {
            byte[] redisKey = (keyPrefix + key + ":" + plan.name()).getBytes(StandardCharsets.UTF_8);
            var bucket = getProxyManager().builder().build(redisKey, () -> Buckets.configurationFor(plan));
            return Buckets.toResult(bucket.tryConsumeAndReturnRemaining(tokens));
        } catch (RuntimeException e) {
            log.warn("Redis rate limit store unavailable, using local limits for {}s: {}",
                    RETRY_REDIS_AFTER.toSeconds(), e.getMessage());
            redisDisabledUntil = clock.instant().plus(RETRY_REDIS_AFTER);
            return fallbackStore.tryConsume(key, plan, tokens);
        }
    }

    private synchronized ProxyManager<byte[]> getProxyManager() {
        if (proxyManager == null) {
            redisUri.setTimeout(REDIS_TIMEOUT);
            redisClient = RedisClient.create(redisUri);
            proxyManager = Bucket4jLettuce.casBasedBuilder(redisClient)
                    .requestTimeout(REDIS_TIMEOUT)
                    .expirationAfterWrite(ExpirationAfterWriteStrategy
                            .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                    .build();
        }
        return proxyManager;
    }

    @Override
    public synchronized void destroy() {
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }
}
