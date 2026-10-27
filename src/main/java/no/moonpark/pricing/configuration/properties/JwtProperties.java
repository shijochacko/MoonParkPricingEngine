package no.moonpark.pricing.configuration.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * JWT settings for tokens issued by this service.
 *
 * @param secret        HMAC key, at least 32 characters. Provide via the JWT_SECRET environment variable.
 * @param issuer        value of the {@code iss} claim; tokens from other issuers are rejected
 * @param tokenLifetime how long an access token is valid
 */
@ConfigurationProperties("security.jwt")
public record JwtProperties(String secret,
                            @DefaultValue("moonpark-pricing") String issuer,
                            @DefaultValue("PT30M") Duration tokenLifetime) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("security.jwt.secret (JWT_SECRET) must be at least 32 characters");
        }
    }
}
