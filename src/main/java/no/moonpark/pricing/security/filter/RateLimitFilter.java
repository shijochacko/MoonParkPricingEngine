package no.moonpark.pricing.security.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.moonpark.pricing.configuration.properties.RateLimitProperties;
import no.moonpark.pricing.exceptionhandler.ProblemResponses;
import no.moonpark.pricing.ratelimit.RateLimitPlan;
import no.moonpark.pricing.ratelimit.RateLimitResult;
import no.moonpark.pricing.ratelimit.RateLimitStore;
import no.moonpark.pricing.security.ApiKeyAuthentication;

/**
 * Limits how many requests each caller can make (Task 5).
 * <p>
 * Runs after authentication, so we know who is calling:
 * <ul>
 *   <li>API client - limit from its plan (FREE / STANDARD / PARTNER), key = client id.</li>
 *   <li>Logged-in user - STANDARD, admins INTERNAL, key = username.</li>
 *   <li>Anonymous (e.g. login) - ANONYMOUS, key = IP address.</li>
 * </ul>
 * A batch request costs more tokens than a single calculation.
 * Every response gets {@code RateLimit-Limit} and {@code RateLimit-Remaining}; a rejected request gets
 * 429 with {@code Retry-After}.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    public static final String LIMIT_HEADER = "RateLimit-Limit";
    public static final String REMAINING_HEADER = "RateLimit-Remaining";
    public static final String RETRY_AFTER_HEADER = "Retry-After";

    private final RateLimitStore store;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public RateLimitFilter(RateLimitStore store, RateLimitProperties properties,
                           ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.store = store;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    /** Who is calling and which plan applies. */
    record Caller(String key, RateLimitPlan plan) {
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.enabled()
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Caller caller = findCaller(SecurityContextHolder.getContext().getAuthentication(), request);
        long cost = requestCost(request);
        RateLimitResult result = store.tryConsume(caller.key(), caller.plan(), cost);

        response.setHeader(LIMIT_HEADER, String.valueOf(caller.plan().requestsPerMinute()));
        response.setHeader(REMAINING_HEADER, String.valueOf(result.remaining()));

        if (result.allowed()) {
            chain.doFilter(request, response);
            return;
        }
        meterRegistry.counter("moonpark.ratelimit.rejected", "plan", caller.plan().name()).increment();
        response.setHeader(RETRY_AFTER_HEADER, String.valueOf(result.retryAfterSeconds()));
        ProblemResponses.write(response, objectMapper, HttpStatus.TOO_MANY_REQUESTS, "rate-limit-exceeded",
                "Too many requests. Try again in " + result.retryAfterSeconds() + " seconds.");
    }

    static Caller findCaller(Authentication authentication, HttpServletRequest request) {
        return switch (authentication) {
            case ApiKeyAuthentication apiKey -> new Caller(
                    "client:" + apiKey.getClient().id(), RateLimitPlan.fromCode(apiKey.getClient().planCode()));
            case JwtAuthenticationToken jwt when isAdmin(jwt) ->
                    new Caller("user:" + jwt.getName(), RateLimitPlan.INTERNAL);
            case JwtAuthenticationToken jwt -> new Caller("user:" + jwt.getName(), RateLimitPlan.STANDARD);
            case null -> new Caller("ip:" + request.getRemoteAddr(), RateLimitPlan.ANONYMOUS);
            default -> new Caller("ip:" + request.getRemoteAddr(), RateLimitPlan.ANONYMOUS);
        };
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private long requestCost(HttpServletRequest request) {
        if (request.getRequestURI().endsWith("/takst/batch")) {
            return properties.batchRequestCost();
        }
        return 1;
    }
}
