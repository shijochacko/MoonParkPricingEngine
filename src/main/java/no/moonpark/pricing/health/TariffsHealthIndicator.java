package no.moonpark.pricing.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.service.CachedTariffRegistry;

/**
 * The pod is only ready for traffic after the tariffs are loaded into memory.
 * Used in the Kubernetes readiness probe ({@code /actuator/health/readiness}).
 */
@Component("tariffs")
@RequiredArgsConstructor
public class TariffsHealthIndicator implements HealthIndicator {

    private final CachedTariffRegistry tariffRegistry;

    @Override
    public Health health() {
        if (tariffRegistry.isLoaded()) {
            return Health.up().build();
        }
        return Health.outOfService().withDetail("reason", "Tariffs not loaded yet").build();
    }
}
