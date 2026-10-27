package no.moonpark.pricing.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


/**
 * Authentication of a third-party system that sent a valid {@code X-API-Key}.
 */
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final ApiClientPrincipal client;

    public ApiKeyAuthentication(ApiClientPrincipal client) {
        super(toAuthorities(client));
        this.client = client;
        setAuthenticated(true);
    }

    public ApiClientPrincipal getClient() {
        return client;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return client;
    }

    @Override
    public String getName() {
        return "client:" + client.id();
    }

    private static List<GrantedAuthority> toAuthorities(ApiClientPrincipal client) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : client.permissions()) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        for (String role : client.roles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return authorities;
    }
}
