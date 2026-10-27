package no.moonpark.pricing.model.domain.tariff;

import java.math.BigDecimal;

final class TariffRules {

    private TariffRules() {
    }

    static void requireNotNegative(BigDecimal value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
