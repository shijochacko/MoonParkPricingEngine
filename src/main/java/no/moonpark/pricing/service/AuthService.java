package no.moonpark.pricing.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.dao.UserRepository;
import no.moonpark.pricing.model.dto.auth.LoginRequest;
import no.moonpark.pricing.model.dto.auth.TokenResponse;
import no.moonpark.pricing.model.entity.UserEntity;
import no.moonpark.pricing.security.TokenService;

/**
 * Username/password login. Always gives the same error for unknown user, wrong password and disabled user,
 * and always checks a password hash, so attackers cannot find valid usernames by timing.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private volatile String dummyHash;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyHash());
            throw invalidLogin();
        }
        boolean passwordMatches = passwordEncoder.matches(request.password(), user.get().getPasswordHash());
        if (!passwordMatches || !user.get().isEnabled()) {
            throw invalidLogin();
        }
        return tokenService.createToken(user.get());
    }

    private BadCredentialsException invalidLogin() {
        return new BadCredentialsException("Invalid username or password");
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("dummy-password-for-timing");
        }
        return dummyHash;
    }
}
