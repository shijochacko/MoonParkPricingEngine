package no.moonpark.pricing.model.domain;

/**
 * Result of a fee calculation.
 *
 * @param tariffVersion identifies the exact tariff definition used, for traceability
 */
public record FeeQuote(ZoneCode zone, ParkingPeriod period, Money fee, String tariffVersion) {
}
