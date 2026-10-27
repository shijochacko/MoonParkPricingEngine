package no.moonpark.pricing.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import no.moonpark.pricing.model.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByNameIn(Collection<String> names);

    List<RoleEntity> findAllByOrderByNameAsc();
}
