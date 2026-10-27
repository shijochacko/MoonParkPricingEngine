package no.moonpark.pricing.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.dao.ParkingZoneRepository;
import no.moonpark.pricing.dao.TariffDefinitionRepository;
import no.moonpark.pricing.exceptionhandler.exception.NotFoundException;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;
import no.moonpark.pricing.mapper.TariffMapper;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.tariff.CreateTariffRequest;
import no.moonpark.pricing.model.dto.tariff.TariffResponse;
import no.moonpark.pricing.model.entity.ParkingZoneEntity;
import no.moonpark.pricing.model.entity.TariffDefinitionEntity;
import no.moonpark.pricing.model.event.TariffChangedEvent;
import no.moonpark.pricing.validator.TariffRequestValidator;

/**
 * Changes tariffs without a new deployment. Tariff rows are never edited: a price change creates a new
 * version and closes the old one, so old fees can always be explained.
 * <p>
 * Adding a new zone (e.g. M4) with an existing rule type is just new rows (zone + tariff) - no code change.
 */
@Service
@RequiredArgsConstructor
public class TariffAdminService {

    private final TariffDefinitionRepository repository;
    private final ParkingZoneRepository parkingZoneRepository;
    private final TariffRequestValidator tariffRequestValidator;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final TariffMapper tariffMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<TariffResponse> listTariffs(String zone, Pageable pageable) {
        Page<TariffDefinitionEntity> tariffs;
        if (zone == null) {
            tariffs = repository.findAll(pageable);
        } else {
            ZoneCode zoneCode = new ZoneCode(zone);
            if (!parkingZoneRepository.existsById(zoneCode.value())) {
                throw new UnknownZoneException(zoneCode);
            }
            tariffs = repository.findByZoneCode(zoneCode.value(), pageable);
        }
        return PageResponse.from(tariffs.map(tariffMapper::toResponse));
    }

    @Transactional
    public TariffResponse createTariffVersion(CreateTariffRequest request, String actor) {
        String zone = request.zone().trim().toUpperCase(Locale.ROOT);
        tariffRequestValidator.checkParameters(zone, request);
        Instant validFrom = request.validFrom() == null ? clock.instant() : request.validFrom();
        Optional<TariffDefinitionEntity> openVersion = repository.findByZoneCodeAndActiveTrueAndValidToIsNull(zone);
        tariffRequestValidator.checkValidFrom(validFrom, openVersion);
        registerZoneIfNew(zone);

        if (openVersion.isPresent()) {
            openVersion.get().setValidTo(validFrom);
            repository.flush(); // optimistic lock check on the old version happens here
        }

        TariffDefinitionEntity tariff = new TariffDefinitionEntity();
        tariff.setZoneCode(zone);
        tariff.setRuleType(request.ruleType());
        tariff.setParameters(new HashMap<>(request.parameters()));
        tariff.setValidFrom(validFrom);
        tariff.setActive(true);
        repository.saveAndFlush(tariff);

        auditService.record(actor, "TARIFF_CREATED", zone + " " + request.ruleType() + " " + request.parameters()
                + " from " + validFrom);
        eventPublisher.publishEvent(new TariffChangedEvent(zone, "created"));
        return tariffMapper.toResponse(tariff);
    }

    @Transactional
    public TariffResponse deactivateTariff(Long tariffId, String actor) {
        TariffDefinitionEntity tariff = repository.findById(tariffId)
                .orElseThrow(() -> new NotFoundException("Tariff not found"));
        tariff.setActive(false);
        repository.flush();
        auditService.record(actor, "TARIFF_DEACTIVATED", tariff.getZoneCode() + " " + tariffId);
        eventPublisher.publishEvent(new TariffChangedEvent(tariff.getZoneCode(), "deactivated"));
        return tariffMapper.toResponse(tariff);
    }

    /** A tariff must point to a known zone (foreign key), so the first tariff of a new zone also creates the zone. */
    private void registerZoneIfNew(String zone) {
        if (!parkingZoneRepository.existsById(zone)) {
            parkingZoneRepository.save(new ParkingZoneEntity(zone, "Rate zone " + zone));
        }
    }
}
