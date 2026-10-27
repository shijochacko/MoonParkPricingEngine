package no.moonpark.pricing.security;

import java.util.Set;

/**
 * What we know about an authenticated third-party system. Kept small so it can be cached.
 */
public record ApiClientPrincipal(Long id,
                                 String name,
                                 String planCode,
                                 Set<String> roles,
                                 Set<String> permissions) {

    public ApiClientPrincipal {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }
}
