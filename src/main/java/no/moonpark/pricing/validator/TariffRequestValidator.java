package no.moonpark.pricing.validator;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.exceptionhandler.exception.BusinessRuleException;
import no.moonpark.pricing.model.dto.tariff.CreateTariffRequest;
import no.moonpark.pricing.model.entity.TariffDefinitionEntity;
import no.moonpark.pricing.service.TariffFactory;

/**
 * Business checks for a new tariff version.
 * <ul>
 *   <li>The rule parameters must be complete and valid for the rule type.</li>
 *   <li>validFrom cannot be in the past (a small clock skew of 60 seconds is allowed).</li>
 *   <li>validFrom must be after the start of the current open version of the zone.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class TariffRequestValidator {

    static final long ALLOWED_CLOCK_SKEW_SECONDS = 60;

    private final TariffFactory tariffFactory;
    private final Clock clock;

    public void checkParameters(String zone, CreateTariffRequest request) {
        try {
            tariffFactory.createTariff(zone, request.ruleType(), request.parameters());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }
    }

    public void checkValidFrom(Instant validFrom, Optional<TariffDefinitionEntity> openVersion) {
        if (validFrom.isBefore(clock.instant().minusSeconds(ALLOWED_CLOCK_SKEW_SECONDS))) {
            throw new BusinessRuleException("validFrom cannot be in the past; price history must not change");
        }
        if (openVersion.isPresent() && !validFrom.isAfter(openVersion.get().getValidFrom())) {
            throw new BusinessRuleException("validFrom must be after the start of the current version ("
                    + openVersion.get().getValidFrom() + ")");
        }
    }
}
