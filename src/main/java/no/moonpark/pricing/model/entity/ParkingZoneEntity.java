package no.moonpark.pricing.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A rate zone such as M1. Master data: every tariff version must belong to a known zone.
 */
@Entity
@Table(schema = Schemas.MASTER_DATA, name = "parking_zone")
@Getter
@Setter
@NoArgsConstructor
public class ParkingZoneEntity extends AuditableEntity {

    @Id
    @Column(name = "zone_code", length = 10)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    public ParkingZoneEntity(String zoneCode, String zoneName) {
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
        this.active = true;
    }
}
