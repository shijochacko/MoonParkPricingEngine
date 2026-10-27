package no.moonpark.pricing.model.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A role is a named bundle of permissions. Master data managed by Liquibase.
 */
@Entity
@Table(schema = Schemas.MASTER_DATA, name = "role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    // Small reference table: loading permissions eagerly keeps the code simple
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(schema = Schemas.MASTER_DATA, name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();

    public RoleEntity(String name, Set<PermissionEntity> permissions) {
        this.name = name;
        this.permissions = new HashSet<>(permissions);
    }

    public Set<String> permissionNames() {
        return permissions.stream().map(PermissionEntity::getName).collect(Collectors.toCollection(TreeSet::new));
    }
}
