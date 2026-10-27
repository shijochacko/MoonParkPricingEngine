package no.moonpark.pricing.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import no.moonpark.pricing.model.entity.ParkingZoneEntity;

public interface ParkingZoneRepository extends JpaRepository<ParkingZoneEntity, String> {
}
