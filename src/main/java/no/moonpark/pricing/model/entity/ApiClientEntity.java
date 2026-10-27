package no.moonpark.pricing.model.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A third-party system. Only the SHA-256 hash of its API key is stored, never the key itself.
 */
@Entity
@Table(schema = Schemas.PRICING_DATA, name = "api_client")
@Getter
@Setter
@NoArgsConstructor
public class ApiClientEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_client_id")
    private Long id;

    @Column(name = "client_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "plan_code", nullable = false, length = 20)
    private String planCode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "key_rotated_at")
    private Instant keyRotatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = Schemas.PRICING_DATA, name = "api_client_role",
            joinColumns = @JoinColumn(name = "api_client_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public boolean isActiveAt(Instant now) {
        return enabled && (expiresAt == null || expiresAt.isAfter(now));
    }

    public Set<String> roleNames() {
        return roles.stream().map(RoleEntity::getName).collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> permissionNames() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
