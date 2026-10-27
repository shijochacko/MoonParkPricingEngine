package no.moonpark.pricing.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Turns the {@code permissions} and {@code roles} claims of a JWT into Spring Security authorities.
 * Permissions are used as-is (e.g. {@code FEE_CALCULATE}); roles get the {@code ROLE_} prefix.
 */
public class PermissionJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : readList(jwt, TokenService.PERMISSIONS_CLAIM)) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        for (String role : readList(jwt, TokenService.ROLES_CLAIM)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private List<String> readList(Jwt jwt, String claimName) {
        List<String> values = jwt.getClaimAsStringList(claimName);
        return values == null ? List.of() : values;
    }
}
