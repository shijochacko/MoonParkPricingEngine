package no.moonpark.pricing.model.dto.fee;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BatchFeeRequest(@NotEmpty @Size(max = 100) List<@Valid FeeRequestItem> items) {
}
