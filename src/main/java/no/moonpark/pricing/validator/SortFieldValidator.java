package no.moonpark.pricing.validator;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import no.moonpark.pricing.exceptionhandler.exception.BusinessRuleException;

/**
 * Only allows sorting on known fields. Stops clients from sorting on unindexed or internal columns.
 */
public final class SortFieldValidator {

    private SortFieldValidator() {
    }

    public static void checkSortFields(Pageable pageable, Set<String> allowedFields) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedFields.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "Cannot sort by '" + order.getProperty() + "'. Allowed: " + allowedFields);
            }
        }
    }
}
