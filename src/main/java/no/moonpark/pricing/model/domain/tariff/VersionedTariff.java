package no.moonpark.pricing.model.domain.tariff;


/**
 * A tariff together with the identifier of the definition it was built from.
 */
public record VersionedTariff(Tariff tariff, String version) {
}
