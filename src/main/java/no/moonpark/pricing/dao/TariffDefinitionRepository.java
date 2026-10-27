package no.moonpark.pricing.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import no.moonpark.pricing.model.entity.TariffDefinitionEntity;

public interface TariffDefinitionRepository extends JpaRepository<TariffDefinitionEntity, Long> {

    List<TariffDefinitionEntity> findByActiveTrue();

    Page<TariffDefinitionEntity> findByZoneCode(String zoneCode, Pageable pageable);

    Optional<TariffDefinitionEntity> findByZoneCodeAndActiveTrueAndValidToIsNull(String zoneCode);
}
