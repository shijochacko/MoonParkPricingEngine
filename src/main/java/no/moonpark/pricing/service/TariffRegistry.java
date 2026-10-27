package no.moonpark.pricing.service;

import java.time.Instant;
import java.util.Optional;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.domain.tariff.VersionedTariff;


/**
 * Outbound port: finds the tariff that was in effect for a zone at a given moment.
 */
public interface TariffRegistry {

    Optional<VersionedTariff> findEffective(ZoneCode zone, Instant at);

    /** True when the zone has at least one active tariff version (at any time). */
    boolean hasZone(ZoneCode zone);
}
