package no.moonpark.pricing.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.moonpark.pricing.dao.TariffDefinitionRepository;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.domain.tariff.VersionedTariff;
import no.moonpark.pricing.model.entity.TariffDefinitionEntity;
import no.moonpark.pricing.model.event.TariffChangedEvent;

/**
 * Keeps all active tariffs in memory, so fee calculation never touches the database.
 * <ul>
 *   <li>Loaded at startup and refreshed by {@code TariffRefreshScheduler} (other pods pick up changes within the refresh interval).</li>
 *   <li>Refreshed immediately on this instance after a tariff change is committed.</li>
 *   <li>If the database is down, the last loaded tariffs keep being used.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CachedTariffRegistry implements TariffRegistry {

    private record Entry(Instant validFrom, Instant validTo, VersionedTariff tariff) {

        boolean isValidAt(Instant at) {
            return !at.isBefore(validFrom) && (validTo == null || at.isBefore(validTo));
        }
    }

    private final TariffDefinitionRepository repository;
    private final TariffFactory tariffFactory;
    private final CacheManager cacheManager;

    private volatile Map<ZoneCode, List<Entry>> tariffsByZone = Map.of();
    private volatile boolean loaded;

    @Override
    public Optional<VersionedTariff> findEffective(ZoneCode zone, Instant at) {
        for (Entry entry : tariffsByZone.getOrDefault(zone, List.of())) {
            if (entry.isValidAt(at)) {
                return Optional.of(entry.tariff());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean hasZone(ZoneCode zone) {
        return tariffsByZone.containsKey(zone);
    }

    public boolean isLoaded() {
        return loaded;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadAtStartup() {
        refresh();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTariffChanged(TariffChangedEvent event) {
        log.info("Tariff {} for zone {}; reloading tariffs", event.action(), event.zoneCode());
        refresh();
    }

    public synchronized void refresh() {
        try {
            Map<ZoneCode, List<Entry>> newTariffs = loadTariffs();
            boolean changed = loaded && !sameVersions(tariffsByZone, newTariffs);
            tariffsByZone = newTariffs;
            loaded = true;
            if (changed) {
                clearFeeCache();
            }
            log.debug("Loaded tariffs for {} zones", newTariffs.size());
        } catch (RuntimeException e) {
            log.error("Could not reload tariffs; keeping the last loaded tariffs", e);
        }
    }

    private Map<ZoneCode, List<Entry>> loadTariffs() {
        Map<ZoneCode, List<Entry>> result = new HashMap<>();
        for (TariffDefinitionEntity definition : repository.findByActiveTrue()) {
            try {
                ZoneCode zone = new ZoneCode(definition.getZoneCode());
                VersionedTariff tariff = new VersionedTariff(
                        tariffFactory.createTariff(definition.getZoneCode(), definition.getRuleType(), definition.getParameters()),
                        definition.getId() + ":" + definition.getVersion());
                result.computeIfAbsent(zone, key -> new ArrayList<>())
                        .add(new Entry(definition.getValidFrom(), definition.getValidTo(), tariff));
            } catch (IllegalArgumentException e) {
                log.error("Skipping invalid tariff definition {}: {}", definition.getId(), e.getMessage());
            }
        }
        Map<ZoneCode, List<Entry>> immutable = new HashMap<>();
        result.forEach((zone, entries) -> immutable.put(zone, List.copyOf(entries)));
        return Map.copyOf(immutable);
    }

    private boolean sameVersions(Map<ZoneCode, List<Entry>> oldTariffs, Map<ZoneCode, List<Entry>> newTariffs) {
        return versionsOf(oldTariffs).equals(versionsOf(newTariffs));
    }

    private Map<ZoneCode, List<String>> versionsOf(Map<ZoneCode, List<Entry>> tariffs) {
        Map<ZoneCode, List<String>> versions = new HashMap<>();
        tariffs.forEach((zone, entries) -> versions.put(zone,
                entries.stream().map(entry -> entry.tariff().version()).sorted().toList()));
        return versions;
    }

    private void clearFeeCache() {
        Cache cache = cacheManager.getCache(FeeCalculationService.FEE_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }
}
