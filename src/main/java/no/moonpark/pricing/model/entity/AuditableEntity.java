package no.moonpark.pricing.model.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Common audit columns (created_by, created_date, modified_by, modified_date).
 * Filled automatically by Spring Data JPA auditing - see {@code JpaAuditingConfig}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditableEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 150)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedBy
    @Column(name = "modified_by", length = 150)
    private String modifiedBy;

    @LastModifiedDate
    @Column(name = "modified_date")
    private Instant modifiedDate;
}
