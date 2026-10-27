package no.moonpark.pricing.configuration;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Fills created_by / created_date / modified_by / modified_date on every {@code AuditableEntity}.
 * The "who" is the logged-in user or API client; background work (startup, schedulers) is recorded as "system".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "currentAuditor", dateTimeProviderRef = "auditDateTimeProvider")
public class JpaAuditingConfig {

    public static final String SYSTEM_USER = "system";

    @Bean
    public AuditorAware<String> currentAuditor() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
                return Optional.of(SYSTEM_USER);
            }
            return Optional.of(authentication.getName());
        };
    }

    // Uses the same Clock as the rest of the application, so tests can control time
    @Bean
    public DateTimeProvider auditDateTimeProvider(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
