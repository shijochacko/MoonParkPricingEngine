package no.moonpark.pricing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.configuration.properties.RateLimitProperties;
import no.moonpark.pricing.ratelimit.RateLimitStore;
import no.moonpark.pricing.security.PermissionJwtConverter;
import no.moonpark.pricing.security.ProblemAccessDeniedHandler;
import no.moonpark.pricing.security.ProblemAuthenticationEntryPoint;
import no.moonpark.pricing.model.enums.RoleName;
import no.moonpark.pricing.security.filter.ApiKeyAuthenticationFilter;
import no.moonpark.pricing.security.filter.RateLimitFilter;
import no.moonpark.pricing.service.ApiClientService;

/**
 * Security rules.
 * <ol>
 *   <li>{@link ApiKeyAuthenticationFilter} - third parties with {@code X-API-Key}.</li>
 *   <li>Bearer JWT - people (USER, ADMIN, SUPER_ADMIN).</li>
 *   <li>{@link RateLimitFilter} - per client / user / IP, after the caller is known.</li>
 *   <li>{@code @PreAuthorize} on each endpoint - fine-grained permission checks.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiClientService apiClientService;
    private final ProblemAuthenticationEntryPoint entryPoint;
    private final ProblemAccessDeniedHandler accessDeniedHandler;
    private final RateLimitStore rateLimitStore;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/takst", "/api/v1/takst").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").hasRole(RoleName.SUPER_ADMIN.name())
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new PermissionJwtConverter()))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new ApiKeyAuthenticationFilter(apiClientService, entryPoint),
                        BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new RateLimitFilter(rateLimitStore, rateLimitProperties, objectMapper, meterRegistry),
                        BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // bcrypt by default; the {id} prefix allows upgrading the algorithm later
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
