package no.moonpark.pricing.service;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.dao.AuditEventRepository;
import no.moonpark.pricing.mapper.AuditMapper;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.audit.AuditEventResponse;
import no.moonpark.pricing.model.entity.AuditEventEntity;

/**
 * Writes audit events. {@link Propagation#MANDATORY} makes sure the audit row is saved in the
 * same transaction as the change itself: either both are saved or neither is.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 1000;

    private final AuditEventRepository auditEventRepository;
    private final AuditMapper auditMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String details) {
        String safeDetails = details;
        if (safeDetails != null && safeDetails.length() > MAX_DETAILS_LENGTH) {
            safeDetails = safeDetails.substring(0, MAX_DETAILS_LENGTH);
        }
        auditEventRepository.save(new AuditEventEntity(actor, action, safeDetails, MDC.get("traceId")));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> listEvents(String actor, String action, Pageable pageable) {
        Page<AuditEventEntity> events;
        if (actor != null && !actor.isBlank()) {
            events = auditEventRepository.findByActor(actor, pageable);
        } else if (action != null && !action.isBlank()) {
            events = auditEventRepository.findByAction(action, pageable);
        } else {
            events = auditEventRepository.findAll(pageable);
        }
        return PageResponse.from(events.map(auditMapper::toResponse));
    }
}
