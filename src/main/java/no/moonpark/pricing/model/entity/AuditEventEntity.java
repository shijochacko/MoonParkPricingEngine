package no.moonpark.pricing.model.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One administrative change. Append-only: a database trigger rejects UPDATE and DELETE,
 * so it does not extend {@link AuditableEntity} (there is never a "modified" value).
 */
@Entity
@Table(schema = Schemas.PRICING_DATA, name = "audit_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_event_id")
    private Long id;

    @Column(name = "actor", nullable = false, length = 150)
    private String actor;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    public AuditEventEntity(String actor, String action, String details, String correlationId) {
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.correlationId = correlationId;
    }
}
