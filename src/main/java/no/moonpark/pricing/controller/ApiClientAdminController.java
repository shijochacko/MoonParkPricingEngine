package no.moonpark.pricing.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.model.dto.PageResponse;
import no.moonpark.pricing.model.dto.client.ApiClientKeyResponse;
import no.moonpark.pricing.model.dto.client.ApiClientResponse;
import no.moonpark.pricing.model.dto.client.CreateApiClientRequest;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.ApiClientService;

@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('" + Permissions.CLIENT_MANAGE + "')")
@Tag(name = "Admin - third-party API clients")
public class ApiClientAdminController {

    private final ApiClientService apiClientService;

    @GetMapping
    public PageResponse<ApiClientResponse> listClients(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return apiClientService.listClients(pageable);
    }

    @PostMapping
    @Operation(summary = "Register a third-party client. The API key is shown only in this response")
    public ResponseEntity<ApiClientKeyResponse> createClient(@Valid @RequestBody CreateApiClientRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(apiClientService.createClient(request, authentication.getName()));
    }

    @PostMapping("/{clientId}/rotate-key")
    @Operation(summary = "Create a new API key. The old key stops working")
    public ResponseEntity<ApiClientKeyResponse> rotateKey(@PathVariable Long clientId, Authentication authentication) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(apiClientService.rotateKey(clientId, authentication.getName()));
    }

    @PostMapping("/{clientId}/disable")
    public ApiClientResponse disableClient(@PathVariable Long clientId, Authentication authentication) {
        return apiClientService.disableClient(clientId, authentication.getName());
    }

    @PostMapping("/{clientId}/enable")
    public ApiClientResponse enableClient(@PathVariable Long clientId, Authentication authentication) {
        return apiClientService.enableClient(clientId, authentication.getName());
    }
}
