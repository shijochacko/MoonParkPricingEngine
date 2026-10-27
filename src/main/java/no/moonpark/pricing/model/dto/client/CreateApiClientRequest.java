package no.moonpark.pricing.model.dto.client;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApiClientRequest(
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[a-zA-Z0-9 ._-]+$") String name,
        @Email @Size(max = 255) String contactEmail,
        @NotBlank @Pattern(regexp = "^(FREE|STANDARD|PARTNER)$",
                message = "must be one of FREE, STANDARD, PARTNER") String plan,
        @Future Instant expiresAt) {
}
