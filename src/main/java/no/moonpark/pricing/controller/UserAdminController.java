package no.moonpark.pricing.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.user.ChangeRolesRequest;
import no.moonpark.pricing.model.dto.user.CreateUserRequest;
import no.moonpark.pricing.model.dto.user.RoleResponse;
import no.moonpark.pricing.model.dto.user.UserResponse;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.UserService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - users and roles")
public class UserAdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('" + Permissions.USER_READ + "')")
    @Operation(summary = "List users. Filter by role; sort by username, email or createdDate")
    public PageResponse<UserResponse> listUsers(
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.listUsers(role, pageable);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('" + Permissions.USER_READ + "')")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('" + Permissions.USER_MANAGE + "')")
    @Operation(summary = "Create a user with one or more roles")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        return userService.createUser(request, authentication.getName());
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('" + Permissions.USER_MANAGE + "')")
    @Operation(summary = "Replace the roles of a user")
    public UserResponse changeRoles(@PathVariable Long userId, @Valid @RequestBody ChangeRolesRequest request,
                                    Authentication authentication) {
        return userService.changeRoles(userId, request, authentication.getName());
    }

    @PostMapping("/users/{userId}/disable")
    @PreAuthorize("hasAuthority('" + Permissions.USER_MANAGE + "')")
    public UserResponse disableUser(@PathVariable Long userId, Authentication authentication) {
        return userService.disableUser(userId, authentication.getName());
    }

    @PostMapping("/users/{userId}/enable")
    @PreAuthorize("hasAuthority('" + Permissions.USER_MANAGE + "')")
    public UserResponse enableUser(@PathVariable Long userId, Authentication authentication) {
        return userService.enableUser(userId, authentication.getName());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('" + Permissions.USER_READ + "')")
    @Operation(summary = "List roles and their permissions")
    public List<RoleResponse> listRoles() {
        return userService.listRoles();
    }
}
