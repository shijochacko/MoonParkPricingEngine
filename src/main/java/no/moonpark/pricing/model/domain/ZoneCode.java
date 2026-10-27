package no.moonpark.pricing.model.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Identifier of a rate zone, e.g. {@code M1}. Always upper case, 1-10 letters/digits.
 */
public record ZoneCode(String value) {

    private static final Pattern VALID_ZONE = Pattern.compile("^[A-Z0-9]{1,10}$");

    public ZoneCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("zone is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!VALID_ZONE.matcher(value).matches()) {
            throw new IllegalArgumentException("zone must be 1-10 letters or digits");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
