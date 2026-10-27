package no.moonpark.pricing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.dao.RoleRepository;
import no.moonpark.pricing.dao.UserRepository;
import no.moonpark.pricing.exceptionhandler.exception.BusinessRuleException;
import no.moonpark.pricing.exceptionhandler.exception.ConflictException;
import no.moonpark.pricing.exceptionhandler.exception.NotFoundException;
import no.moonpark.pricing.mapper.UserMapper;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.user.ChangeRolesRequest;
import no.moonpark.pricing.model.dto.user.CreateUserRequest;
import no.moonpark.pricing.model.dto.user.ProfileResponse;
import no.moonpark.pricing.model.dto.user.RoleResponse;
import no.moonpark.pricing.model.dto.user.UserResponse;
import no.moonpark.pricing.model.entity.RoleEntity;
import no.moonpark.pricing.model.entity.UserEntity;
import no.moonpark.pricing.model.enums.RoleName;
import no.moonpark.pricing.validator.RoleAssignmentValidator;
import no.moonpark.pricing.validator.SortFieldValidator;

/**
 * User management. Business rules:
 * <ul>
 *   <li>EXTERNAL_CLIENT can only be given to API clients, never to people.</li>
 *   <li>A user cannot change their own roles or disable themselves.</li>
 *   <li>At least one active SUPER_ADMIN must always remain.</li>
 *   <li>Every change is written to the audit log in the same transaction.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    static final Set<String> SORTABLE_FIELDS = Set.of("username", "email", "createdDate");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        return userMapper.toProfileResponse(findByUsername(username));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String role, Pageable pageable) {
        SortFieldValidator.checkSortFields(pageable, SORTABLE_FIELDS);
        Page<UserEntity> users;
        if (role == null || role.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByRolesName(role.trim().toUpperCase(Locale.ROOT), pageable);
        }
        return PageResponse.from(users.map(userMapper::toUserResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return userMapper.toUserResponse(findById(userId));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAllByOrderByNameAsc().stream().map(userMapper::toRoleResponse).toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String actor) {
        String username = normalize(request.username());
        String email = normalize(request.email());
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(findRoles(request.roles()));
        userRepository.saveAndFlush(user);
        auditService.record(actor, "USER_CREATED", username + " " + user.roleNames());
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse changeRoles(Long userId, ChangeRolesRequest request, String actor) {
        UserEntity user = findById(userId);
        RoleAssignmentValidator.checkNotSelf(user, actor, "You cannot change your own roles");
        Set<RoleEntity> newRoles = findRoles(request.roles());
        boolean removesSuperAdmin = user.hasRole(RoleName.SUPER_ADMIN) && !containsRole(newRoles, RoleName.SUPER_ADMIN);
        if (removesSuperAdmin) {
            checkNotLastSuperAdmin();
        }
        user.setRoles(newRoles);
        userRepository.flush();
        auditService.record(actor, "USER_ROLES_CHANGED", user.getUsername() + " " + user.roleNames());
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse disableUser(Long userId, String actor) {
        UserEntity user = findById(userId);
        RoleAssignmentValidator.checkNotSelf(user, actor, "You cannot disable yourself");
        if (user.isEnabled() && user.hasRole(RoleName.SUPER_ADMIN)) {
            checkNotLastSuperAdmin();
        }
        user.setEnabled(false);
        userRepository.flush();
        auditService.record(actor, "USER_DISABLED", user.getUsername());
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse enableUser(Long userId, String actor) {
        UserEntity user = findById(userId);
        user.setEnabled(true);
        userRepository.flush();
        auditService.record(actor, "USER_ENABLED", user.getUsername());
        return userMapper.toUserResponse(user);
    }

    private Set<RoleEntity> findRoles(Set<String> requestedNames) {
        Set<String> names = requestedNames.stream()
                .map(name -> name.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        RoleAssignmentValidator.checkNoExternalClientRole(names);
        List<RoleEntity> roles = roleRepository.findByNameIn(names);
        if (roles.size() != names.size()) {
            throw new BusinessRuleException("Unknown role in " + names);
        }
        return new HashSet<>(roles);
    }

    private boolean containsRole(Set<RoleEntity> roles, RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName.name()));
    }

    private void checkNotLastSuperAdmin() {
        if (userRepository.lockActiveSuperAdmins().size() <= 1) {
            throw new BusinessRuleException("At least one active SUPER_ADMIN must remain");
        }
    }

    private UserEntity findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
