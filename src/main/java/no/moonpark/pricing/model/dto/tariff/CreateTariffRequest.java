package no.moonpark.pricing.model.dto.tariff;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.moonpark.pricing.model.enums.RuleType;

/**
 * A new tariff version for a zone. {@code validFrom} empty = from now.
 * The currently open version of the zone is closed at {@code validFrom}.
 */
public record CreateTariffRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{1,10}$") String zone,
        @NotNull RuleType ruleType,
        @NotEmpty Map<String, Object> parameters,
        Instant validFrom) {
}
