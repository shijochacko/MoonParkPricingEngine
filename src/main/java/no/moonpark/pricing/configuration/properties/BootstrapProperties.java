package no.moonpark.pricing.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The first super admin, created at startup if no super admin exists yet.
 * Leave empty to skip. Remove the password from the environment after the first start.
 */
@ConfigurationProperties("security.bootstrap")
public record BootstrapProperties(String superAdminUsername, String superAdminEmail, String superAdminPassword) {

    public boolean isConfigured() {
        return hasText(superAdminUsername) && hasText(superAdminEmail) && hasText(superAdminPassword);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
