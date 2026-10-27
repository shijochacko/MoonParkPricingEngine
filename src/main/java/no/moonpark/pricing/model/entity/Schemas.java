package no.moonpark.pricing.model.entity;

/**
 * Database schema names. Master data = slowly changing reference data, pricing data = transactional data.
 */
public final class Schemas {

    public static final String MASTER_DATA = "moonpark_master_data";
    public static final String PRICING_DATA = "moonpark_pricing_data";

    private Schemas() {
    }
}
