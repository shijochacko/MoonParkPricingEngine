package no.moonpark.pricing.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.audit.AuditEventResponse;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.AuditService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin - audit log")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/api/v1/admin/audit-events")
    @PreAuthorize("hasAuthority('" + Permissions.AUDIT_READ + "')")
    public PageResponse<AuditEventResponse> listEvents(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 50, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return auditService.listEvents(actor, action, pageable);
    }
}
