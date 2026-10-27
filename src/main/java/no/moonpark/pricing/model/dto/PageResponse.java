package no.moonpark.pricing.model.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable page format for list endpoints (does not depend on Spring's internal Page JSON).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
