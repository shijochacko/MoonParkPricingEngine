package no.moonpark.pricing.security.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.moonpark.pricing.configuration.SecurityConfig;
import no.moonpark.pricing.security.ApiClientPrincipal;
import no.moonpark.pricing.security.ApiKeyAuthentication;
import no.moonpark.pricing.service.ApiClientService;

/**
 * Authenticates third-party systems with the {@code X-API-Key} header.
 * <ul>
 *   <li>No header: does nothing (a Bearer token may still authenticate the request).</li>
 *   <li>Header and a Bearer token together: 401 (ambiguous identity).</li>
 *   <li>Unknown, disabled or expired key: 401.</li>
 * </ul>
 * Created in {@link SecurityConfig}, not a Spring bean, so it only runs inside the security filter chain.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final ApiClientService apiClientService;
    private final AuthenticationEntryPoint entryPoint;

    public ApiKeyAuthenticationFilter(ApiClientService apiClientService, AuthenticationEntryPoint entryPoint) {
        this.apiClientService = apiClientService;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            reject(request, response, "Send either X-API-Key or a Bearer token, not both");
            return;
        }
        Optional<ApiClientPrincipal> client = apiClientService.findActiveClient(apiKey.trim());
        if (client.isEmpty()) {
            reject(request, response, "Invalid API key");
            return;
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new ApiKeyAuthentication(client.get()));
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String reason)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        entryPoint.commence(request, response, new BadCredentialsException(reason));
    }
}
