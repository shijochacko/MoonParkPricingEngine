/**
 * Outgoing HTTP clients to other systems (Spring {@code RestClient}).
 * <p>
 * The pricing engine has no outgoing calls today. Future integrations (for example a payment provider,
 * a holiday calendar service or a partner webhook) go here, one class per external system, and must use:
 * <ul>
 *   <li>connect and read timeouts,</li>
 *   <li>a Resilience4j circuit breaker and retry with backoff,</li>
 *   <li>a separate bulkhead, so a slow partner cannot block fee calculations.</li>
 * </ul>
 * See {@code docs/10-future-expansion.md}.
 */
package no.moonpark.pricing.restclient;
