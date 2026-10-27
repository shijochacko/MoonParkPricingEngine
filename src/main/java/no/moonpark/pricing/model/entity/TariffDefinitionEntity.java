package no.moonpark.pricing.model.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.moonpark.pricing.model.enums.RuleType;

/**
 * One version of the tariff for a zone. Versions are effective-dated ({@code validFrom} inclusive,
 * {@code validTo} exclusive, {@code null} = open-ended) and never updated in place, which gives full price history.
 */
@Entity
@Table(schema = Schemas.PRICING_DATA, name = "tariff_definition")
@Getter
@Setter
@NoArgsConstructor
public class TariffDefinitionEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tariff_id")
    private Long id;

    @Column(name = "zone_code", nullable = false, length = 10)
    private String zoneCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 40)
    private RuleType ruleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> parameters = new HashMap<>();

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
