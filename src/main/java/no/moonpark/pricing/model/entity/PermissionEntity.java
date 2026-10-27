package no.moonpark.pricing.model.entity;

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
 * A single permission, e.g. {@code FEE_CALCULATE}. Master data managed by Liquibase.
 */
@Entity
@Table(schema = Schemas.MASTER_DATA, name = "permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PermissionEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Column(name = "permission_name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    public PermissionEntity(String name) {
        this.name = name;
    }
}
