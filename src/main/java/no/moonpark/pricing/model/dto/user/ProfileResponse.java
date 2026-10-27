package no.moonpark.pricing.model.dto.user;

import java.util.Set;

public record ProfileResponse(String username,
                              String email,
                              String fullName,
                              Set<String> roles,
                              Set<String> permissions) {
}
