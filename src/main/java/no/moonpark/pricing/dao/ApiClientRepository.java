package no.moonpark.pricing.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import no.moonpark.pricing.model.entity.ApiClientEntity;

public interface ApiClientRepository extends JpaRepository<ApiClientEntity, Long> {

    Optional<ApiClientEntity> findByKeyHash(String keyHash);

    boolean existsByName(String name);
}
