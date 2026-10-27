package no.moonpark.pricing.mapper;

import org.mapstruct.Mapper;
import no.moonpark.pricing.model.dto.audit.AuditEventResponse;
import no.moonpark.pricing.model.entity.AuditEventEntity;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditEventResponse toResponse(AuditEventEntity event);
}
