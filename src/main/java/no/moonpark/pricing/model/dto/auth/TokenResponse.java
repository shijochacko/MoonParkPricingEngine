package no.moonpark.pricing.model.dto.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
