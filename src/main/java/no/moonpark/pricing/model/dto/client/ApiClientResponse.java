package no.moonpark.pricing.model.dto.client;

import java.time.Instant;
import java.util.Set;

public record ApiClientResponse(Long id,
                                String name,
                                String contactEmail,
                                String keyPrefix,
                                String planCode,
                                boolean enabled,
                                Instant expiresAt,
                                Set<String> roles,
                                Instant createdDate,
                                String createdBy) {
}
