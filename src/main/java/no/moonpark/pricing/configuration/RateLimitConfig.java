package no.moonpark.pricing.configuration;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisURI;
import no.moonpark.pricing.configuration.properties.RateLimitProperties;
import no.moonpark.pricing.ratelimit.LocalRateLimitStore;
import no.moonpark.pricing.ratelimit.RateLimitStore;
import no.moonpark.pricing.ratelimit.RedisRateLimitStore;

/**
 * Chooses the bucket store with {@code ratelimit.store}: {@code redis} in production, {@code local} otherwise.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    @ConditionalOnProperty(name = "ratelimit.store", havingValue = "redis")
    RateLimitStore redisRateLimitStore(RedisConnectionDetails connectionDetails, RateLimitProperties properties, Clock clock) {
        RedisConnectionDetails.Standalone standalone = connectionDetails.getStandalone();
        RedisURI.Builder uri = RedisURI.builder()
                .withHost(standalone.getHost())
                .withPort(standalone.getPort())
                .withDatabase(standalone.getDatabase());
        if (connectionDetails.getPassword() != null) {
            if (connectionDetails.getUsername() != null) {
                uri.withAuthentication(connectionDetails.getUsername(), connectionDetails.getPassword());
            } else {
                uri.withPassword(connectionDetails.getPassword().toCharArray());
            }
        }
        return new RedisRateLimitStore(uri.build(), properties.redisKeyPrefix(), new LocalRateLimitStore(), clock);
    }

    @Bean
    @ConditionalOnProperty(name = "ratelimit.store", havingValue = "local", matchIfMissing = true)
    RateLimitStore localRateLimitStore() {
        return new LocalRateLimitStore();
    }
}
