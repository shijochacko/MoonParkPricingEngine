package no.moonpark.pricing.model.dto.tariff;

import java.time.Instant;
import java.util.Map;
import no.moonpark.pricing.model.enums.RuleType;


public record TariffResponse(Long id,
                             String zoneCode,
                             RuleType ruleType,
                             Map<String, Object> parameters,
                             Instant validFrom,
                             Instant validTo,
                             boolean active,
                             String createdBy,
                             Instant createdDate,
                             long version) {
}
