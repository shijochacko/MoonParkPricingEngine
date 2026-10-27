package no.moonpark.pricing.model.domain.tariff;

import no.moonpark.pricing.model.domain.Money;
import no.moonpark.pricing.model.domain.ParkingPeriod;
import no.moonpark.pricing.model.domain.ZoneCode;


/**
 * A pricing rule for one rate zone.
 * <p>
 * Each implementation is a rule <em>type</em>. Concrete zones (M1, M2, M3, ...) are configured in the
 * database with a rule type and its parameters, so a new zone that reuses an existing rule type needs no code change.
 */
public sealed interface Tariff permits PerStartedMinuteTariff, HourlyWeekdayWeekendTariff, DaytimeFreeHourTariff {

    ZoneCode zone();

    Money calculate(ParkingPeriod period);
}
