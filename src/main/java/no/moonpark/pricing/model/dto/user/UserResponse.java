package no.moonpark.pricing.model.dto.user;

import java.time.Instant;
import java.util.Set;

public record UserResponse(Long id,
                           String username,
                           String email,
                           String fullName,
                           boolean enabled,
                           Set<String> roles,
                           Instant createdDate) {
}
