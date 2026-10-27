package no.moonpark.pricing.model.dto.user;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ChangeRolesRequest(@NotEmpty Set<@NotBlank String> roles) {
}
