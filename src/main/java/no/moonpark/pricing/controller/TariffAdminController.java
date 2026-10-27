package no.moonpark.pricing.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import no.moonpark.pricing.model.dto.tariff.CreateTariffRequest;
import no.moonpark.pricing.model.dto.tariff.TariffResponse;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.TariffAdminService;

@RestController
@RequestMapping("/api/v1/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariffs", description = "Rate zones and their price rules")
public class TariffAdminController {

    private final TariffAdminService tariffAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.TARIFF_READ + "')")
    @Operation(summary = "List tariff versions, optionally for one zone",
            description = "Unknown zones return 404; malformed zone codes return 400.")
    public PageResponse<TariffResponse> listTariffs(
            @RequestParam(required = false) String zone,
            @PageableDefault(size = 20, sort = "validFrom", direction = Sort.Direction.DESC) Pageable pageable) {
        return tariffAdminService.listTariffs(zone, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('" + Permissions.TARIFF_WRITE + "')")
    @Operation(summary = "Add a new zone or a new price version for an existing zone")
    public TariffResponse createTariff(@Valid @RequestBody CreateTariffRequest request, Authentication authentication) {
        return tariffAdminService.createTariffVersion(request, authentication.getName());
    }

    @PostMapping("/{tariffId}/deactivate")
    @PreAuthorize("hasAuthority('" + Permissions.TARIFF_WRITE + "')")
    @Operation(summary = "Deactivate a tariff version (the zone is no longer priced by it)")
    public TariffResponse deactivateTariff(@PathVariable Long tariffId, Authentication authentication) {
        return tariffAdminService.deactivateTariff(tariffId, authentication.getName());
    }
}
