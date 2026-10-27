package no.moonpark.pricing.model.dto.audit;

import java.time.Instant;

public record AuditEventResponse(Long id,
                                 String actor,
                                 String action,
                                 String details,
                                 String correlationId,
                                 Instant createdDate) {
}
