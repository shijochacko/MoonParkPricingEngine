package no.moonpark.pricing.model.dto.user;

import java.util.Set;

public record RoleResponse(String name, String description, Set<String> permissions) {
}
