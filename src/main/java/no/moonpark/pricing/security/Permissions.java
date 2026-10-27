package no.moonpark.pricing.security;

/**
 * All permission names. Endpoints check permissions (not role names) with
 * {@code @PreAuthorize("hasAuthority('...')")}. Roles are bundles of permissions stored in the database.
 * <p>
 * Keep this list in sync with the Liquibase seed data; a test verifies it.
 */
public final class Permissions {

    public static final String FEE_CALCULATE = "FEE_CALCULATE";
    public static final String FEE_CALCULATE_BATCH = "FEE_CALCULATE_BATCH";
    public static final String PROFILE_READ = "PROFILE_READ";
    public static final String TARIFF_READ = "TARIFF_READ";
    public static final String TARIFF_WRITE = "TARIFF_WRITE";
    public static final String CLIENT_MANAGE = "CLIENT_MANAGE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String AUDIT_READ = "AUDIT_READ";

    private Permissions() {
    }
}
