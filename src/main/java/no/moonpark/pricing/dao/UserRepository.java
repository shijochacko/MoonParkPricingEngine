package no.moonpark.pricing.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import no.moonpark.pricing.model.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRolesName(String roleName);

    Page<UserEntity> findByRolesName(String roleName, Pageable pageable);

    /**
     * Locks all active super admins, so two concurrent demotions cannot remove the last one.
     * The fixed order prevents deadlocks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u from UserEntity u join u.roles r
            where r.name = 'SUPER_ADMIN' and u.enabled = true
            order by u.id
            """)
    List<UserEntity> lockActiveSuperAdmins();
}
