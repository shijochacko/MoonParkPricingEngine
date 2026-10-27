package no.moonpark.pricing.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.configuration.properties.JwtProperties;
import no.moonpark.pricing.model.dto.auth.TokenResponse;
import no.moonpark.pricing.model.entity.UserEntity;

/**
 * Creates signed access tokens. The token carries the user's roles and the combined permissions of all roles.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String ROLES_CLAIM = "roles";
    public static final String PERMISSIONS_CLAIM = "permissions";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public TokenResponse createToken(UserEntity user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(properties.tokenLifetime()))
                .claim(ROLES_CLAIM, List.copyOf(user.roleNames()))
                .claim(PERMISSIONS_CLAIM, List.copyOf(user.permissionNames()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", properties.tokenLifetime().toSeconds());
    }
}
