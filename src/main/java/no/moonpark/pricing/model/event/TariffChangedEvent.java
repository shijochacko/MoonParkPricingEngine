package no.moonpark.pricing.model.event;

/**
 * Published after a tariff definition has been created or changed.
 * Listeners can react to it (refresh caches today; publish to Kafka in the future).
 */
public record TariffChangedEvent(String zoneCode, String action) {
}
