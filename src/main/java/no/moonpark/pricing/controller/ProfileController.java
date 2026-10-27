package no.moonpark.pricing.controller;

import java.util.TreeSet;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.model.dto.user.ProfileResponse;
import no.moonpark.pricing.security.ApiClientPrincipal;
import no.moonpark.pricing.security.ApiKeyAuthentication;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.UserService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Profile")
public class ProfileController {

    private final UserService userService;

    @GetMapping("/api/v1/me")
    @PreAuthorize("hasAuthority('" + Permissions.PROFILE_READ + "')")
    @Operation(summary = "Who am I? Shows roles and permissions of the caller")
    public ProfileResponse getMyProfile(Authentication authentication) {
        return switch (authentication) {
            case ApiKeyAuthentication apiKey -> clientProfile(apiKey.getClient());
            default -> userService.getProfile(authentication.getName());
        };
    }

    private ProfileResponse clientProfile(ApiClientPrincipal client) {
        return new ProfileResponse("client:" + client.name(), null, client.name(),
                new TreeSet<>(client.roles()), new TreeSet<>(client.permissions()));
    }
}
