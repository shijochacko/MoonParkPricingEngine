package no.moonpark.pricing.model.dto.fee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeeRequestItem(@NotBlank @Size(max = 10) String zone,
                             @NotBlank @Size(max = 40) String start,
                             @NotBlank @Size(max = 40) String end) {
}
