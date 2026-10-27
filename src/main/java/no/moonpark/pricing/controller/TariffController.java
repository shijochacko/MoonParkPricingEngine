package no.moonpark.pricing.controller;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import no.moonpark.pricing.common.util.DateTimeParser;
import no.moonpark.pricing.configuration.properties.PricingProperties;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;
import no.moonpark.pricing.mapper.FeeResponseMapper;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.domain.ZoneCode;
import no.moonpark.pricing.model.dto.fee.BatchFeeRequest;
import no.moonpark.pricing.model.dto.fee.BatchFeeResponse;
import no.moonpark.pricing.model.dto.fee.FeeRequestItem;
import no.moonpark.pricing.model.dto.fee.FeeResponse;
import no.moonpark.pricing.security.Permissions;
import no.moonpark.pricing.service.CalculateFeeUseCase;

/**
 * Task 3: the fee calculator as a web service.
 * {@code GET /takst} is the path from the task; {@code /api/v1/takst} is the same endpoint under the versioned API.
 */
@RestController
@Validated
@Tag(name = "Fee calculation", description = "Calculate parking fees (takst)")
public class TariffController {

    private final CalculateFeeUseCase calculateFee;
    private final FeeResponseMapper feeResponseMapper;
    private final ZoneId timeZone;

    public TariffController(CalculateFeeUseCase calculateFee, FeeResponseMapper feeResponseMapper,
                            PricingProperties properties) {
        this.calculateFee = calculateFee;
        this.feeResponseMapper = feeResponseMapper;
        this.timeZone = properties.timeZone();
    }

    @GetMapping({"/takst", "/api/v1/takst"})
    @Operation(summary = "Calculate the parking fee for one parking period")
    public ResponseEntity<FeeResponse> calculateFee(
            @Parameter(description = "Rate zone", example = "M1") @RequestParam String zone,
            @Parameter(description = "Start, ISO-8601. Without offset = Europe/Oslo time", example = "2026-10-22T08:00")
            @RequestParam String start,
            @Parameter(description = "End, ISO-8601. Without offset = Europe/Oslo time", example = "2026-10-22T09:01")
            @RequestParam String end) {
        FeeResponse response = calculate(new FeeRequestItem(zone, start, end));
        // Same input always gives the same answer, so clients and proxies may cache it briefly
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePrivate())
                .body(response);
    }

    @PostMapping("/api/v1/takst/batch")
    @PreAuthorize("hasAuthority('" + Permissions.FEE_CALCULATE_BATCH + "')")
    @Operation(summary = "Calculate up to 100 fees in one request",
            description = "Invalid items get an error message; the other items are still calculated.")
    public BatchFeeResponse calculateFees(@Valid @RequestBody BatchFeeRequest request) {
        List<BatchFeeResponse.Result> results = new ArrayList<>();
        for (int index = 0; index < request.items().size(); index++) {
            try {
                results.add(BatchFeeResponse.Result.success(index, calculate(request.items().get(index))));
            } catch (InvalidParkingPeriodException | UnknownZoneException | IllegalArgumentException e) {
                results.add(BatchFeeResponse.Result.failure(index, e.getMessage()));
            }
        }
        return new BatchFeeResponse(results);
    }

    private FeeResponse calculate(FeeRequestItem item) {
        ZoneCode zone = new ZoneCode(item.zone());
        Instant start = DateTimeParser.parse(item.start(), "start", timeZone);
        Instant end = DateTimeParser.parse(item.end(), "end", timeZone);
        FeeQuote quote = calculateFee.calculate(zone, start, end);
        return feeResponseMapper.toResponse(quote, timeZone);
    }
}
