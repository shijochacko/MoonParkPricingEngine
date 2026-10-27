package no.moonpark.pricing.model.dto.client;

/**
 * Returned only once, when a client is created or its key is rotated. The plain key is never stored.
 */
public record ApiClientKeyResponse(ApiClientResponse client, String apiKey) {

    @Override
    public String toString() {
        return "ApiClientKeyResponse[client=" + client + ", apiKey=***]";
    }
}
