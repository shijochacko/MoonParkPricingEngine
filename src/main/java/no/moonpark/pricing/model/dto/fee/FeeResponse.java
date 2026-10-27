package no.moonpark.pricing.model.dto.fee;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Calculated parking fee")
public record FeeResponse(
        @Schema(example = "M1") String zone,
        @Schema(example = "2026-10-22T08:00:00+02:00") OffsetDateTime start,
        @Schema(example = "2026-10-22T09:01:00+02:00") OffsetDateTime end,
        @Schema(description = "Parking time in started minutes", example = "61") long durationMinutes,
        @Schema(example = "61.00") BigDecimal amount,
        @Schema(example = "NOK") String currency,
        @Schema(description = "Id of the tariff version used") String tariffVersion) {
}
