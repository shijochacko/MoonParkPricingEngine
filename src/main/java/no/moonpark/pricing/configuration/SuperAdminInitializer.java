package no.moonpark.pricing.configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.moonpark.pricing.configuration.properties.BootstrapProperties;
import no.moonpark.pricing.dao.RoleRepository;
import no.moonpark.pricing.dao.UserRepository;
import no.moonpark.pricing.model.entity.UserEntity;
import no.moonpark.pricing.model.enums.RoleName;

/**
 * Creates the first SUPER_ADMIN from environment variables.
 * Safe when several pods start at the same time: the unique username constraint lets only one insert succeed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements ApplicationRunner {

    static final int MIN_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isConfigured()) {
            log.info("No bootstrap super admin configured");
            return;
        }
        if (userRepository.existsByRolesName(RoleName.SUPER_ADMIN.name())) {
            return;
        }
        if (properties.superAdminPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("SUPER_ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        UserEntity admin = new UserEntity();
        admin.setUsername(properties.superAdminUsername().trim().toLowerCase(Locale.ROOT));
        admin.setEmail(properties.superAdminEmail().trim().toLowerCase(Locale.ROOT));
        admin.setFullName("Super Admin");
        admin.setPasswordHash(passwordEncoder.encode(properties.superAdminPassword()));
        admin.setEnabled(true);
        admin.setRoles(new HashSet<>(roleRepository.findByNameIn(
                List.of(RoleName.SUPER_ADMIN.name(), RoleName.USER.name()))));
        try {
            userRepository.saveAndFlush(admin);
            log.info("Bootstrap super admin '{}' created", admin.getUsername());
        } catch (DataIntegrityViolationException e) {
            log.info("Bootstrap super admin already created by another instance");
        }
    }
}
