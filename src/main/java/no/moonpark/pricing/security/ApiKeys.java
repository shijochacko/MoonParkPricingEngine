package no.moonpark.pricing.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Helpers for API keys. Keys look like {@code mp_<43 url-safe characters>} (256 random bits).
 * Only the SHA-256 hash is stored; the raw key is shown once when created.
 */
public final class ApiKeys {

    public static final String PREFIX = "mp_";
    public static final int KEY_LENGTH = PREFIX.length() + 43;

    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiKeys() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Cheap check done before any database lookup, so random garbage is rejected immediately. */
    public static boolean hasValidFormat(String rawKey) {
        return rawKey != null && rawKey.length() == KEY_LENGTH && rawKey.startsWith(PREFIX);
    }

    public static String hash(String rawKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** First characters of the key, safe to show in admin screens to recognise a key. */
    public static String prefix(String rawKey) {
        return rawKey.substring(0, Math.min(12, rawKey.length()));
    }
}
