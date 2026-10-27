package no.moonpark.pricing.model.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import no.moonpark.pricing.model.enums.RoleName;

/**
 * A person who logs in with username and password. Can have several roles.
 */
@Entity
@Table(schema = Schemas.PRICING_DATA, name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ManyToMany
    @BatchSize(size = 50) // avoids N+1 queries when listing users page by page
    @JoinTable(schema = Schemas.PRICING_DATA, name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public boolean hasRole(RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName.name()));
    }

    public Set<String> roleNames() {
        return roles.stream().map(RoleEntity::getName).collect(Collectors.toCollection(TreeSet::new));
    }

    /** Combined permissions of all roles. */
    public Set<String> permissionNames() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
