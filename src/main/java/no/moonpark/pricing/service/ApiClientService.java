package no.moonpark.pricing.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import no.moonpark.pricing.dao.ApiClientRepository;
import no.moonpark.pricing.dao.RoleRepository;
import no.moonpark.pricing.exceptionhandler.exception.ConflictException;
import no.moonpark.pricing.exceptionhandler.exception.NotFoundException;
import no.moonpark.pricing.mapper.ApiClientMapper;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.client.ApiClientKeyResponse;
import no.moonpark.pricing.model.dto.client.ApiClientResponse;
import no.moonpark.pricing.model.dto.client.CreateApiClientRequest;
import no.moonpark.pricing.model.entity.ApiClientEntity;
import no.moonpark.pricing.security.ApiClientPrincipal;
import no.moonpark.pricing.security.ApiKeys;
import no.moonpark.pricing.model.enums.RoleName;


/**
 * Manages third-party API clients and checks their API keys.
 * <p>
 * Key checks are cached for 60 seconds (also "not found" results), so a flood of requests with
 * a wrong key does not reach the database. A disabled or rotated key therefore stops working within
 * 60 seconds on every pod (immediately on the pod that handled the change).
 */
@Service
public class ApiClientService {

    static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final ApiClientRepository apiClientRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final ApiClientMapper apiClientMapper;
    private final Clock clock;
    private final Cache<String, Optional<ApiClientPrincipal>> keyCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(CACHE_TTL)
            .build();

    public ApiClientService(ApiClientRepository apiClientRepository,
                            RoleRepository roleRepository,
                            AuditService auditService,
                            ApiClientMapper apiClientMapper,
                            Clock clock) {
        this.apiClientRepository = apiClientRepository;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
        this.apiClientMapper = apiClientMapper;
        this.clock = clock;
    }

    /** Returns the client for a valid, enabled, not expired key. */
    @Transactional(readOnly = true)
    public Optional<ApiClientPrincipal> findActiveClient(String apiKey) {
        if (!ApiKeys.hasValidFormat(apiKey)) {
            return Optional.empty();
        }
        String hash = ApiKeys.hash(apiKey);
        return keyCache.get(hash, this::loadActiveClient);
    }

    private Optional<ApiClientPrincipal> loadActiveClient(String keyHash) {
        Instant now = clock.instant();
        return apiClientRepository.findByKeyHash(keyHash)
                .filter(client -> client.isActiveAt(now))
                .map(client -> new ApiClientPrincipal(client.getId(), client.getName(), client.getPlanCode(),
                        client.roleNames(), client.permissionNames()));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApiClientResponse> listClients(Pageable pageable) {
        return PageResponse.from(apiClientRepository.findAll(pageable).map(apiClientMapper::toResponse));
    }

    @Transactional
    public ApiClientKeyResponse createClient(CreateApiClientRequest request, String actor) {
        String name = request.name().trim();
        if (apiClientRepository.existsByName(name)) {
            throw new ConflictException("API client name already exists");
        }
        String apiKey = ApiKeys.generate();
        ApiClientEntity client = new ApiClientEntity();
        client.setName(name);
        client.setContactEmail(request.contactEmail());
        client.setPlanCode(request.plan());
        client.setExpiresAt(request.expiresAt());
        client.setEnabled(true);
        client.setKeyPrefix(ApiKeys.prefix(apiKey));
        client.setKeyHash(ApiKeys.hash(apiKey));
        client.getRoles().addAll(roleRepository.findByNameIn(List.of(RoleName.EXTERNAL_CLIENT.name())));
        apiClientRepository.saveAndFlush(client);
        auditService.record(actor, "API_CLIENT_CREATED", name + " plan=" + request.plan());
        return new ApiClientKeyResponse(apiClientMapper.toResponse(client), apiKey);
    }

    @Transactional
    public ApiClientKeyResponse rotateKey(Long clientId, String actor) {
        ApiClientEntity client = findById(clientId);
        String oldHash = client.getKeyHash();
        String apiKey = ApiKeys.generate();
        client.setKeyPrefix(ApiKeys.prefix(apiKey));
        client.setKeyHash(ApiKeys.hash(apiKey));
        client.setKeyRotatedAt(clock.instant());
        apiClientRepository.flush();
        keyCache.invalidate(oldHash);
        auditService.record(actor, "API_CLIENT_KEY_ROTATED", client.getName());
        return new ApiClientKeyResponse(apiClientMapper.toResponse(client), apiKey);
    }

    @Transactional
    public ApiClientResponse disableClient(Long clientId, String actor) {
        ApiClientEntity client = findById(clientId);
        client.setEnabled(false);
        apiClientRepository.flush();
        keyCache.invalidate(client.getKeyHash());
        auditService.record(actor, "API_CLIENT_DISABLED", client.getName());
        return apiClientMapper.toResponse(client);
    }

    @Transactional
    public ApiClientResponse enableClient(Long clientId, String actor) {
        ApiClientEntity client = findById(clientId);
        client.setEnabled(true);
        apiClientRepository.flush();
        keyCache.invalidate(client.getKeyHash());
        auditService.record(actor, "API_CLIENT_ENABLED", client.getName());
        return apiClientMapper.toResponse(client);
    }

    private ApiClientEntity findById(Long id) {
        return apiClientRepository.findById(id).orElseThrow(() -> new NotFoundException("API client not found"));
    }
}
