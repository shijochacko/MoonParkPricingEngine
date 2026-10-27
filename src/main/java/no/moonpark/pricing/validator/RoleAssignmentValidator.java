package no.moonpark.pricing.validator;

import java.util.Set;

import no.moonpark.pricing.exceptionhandler.exception.BusinessRuleException;
import no.moonpark.pricing.model.entity.UserEntity;
import no.moonpark.pricing.model.enums.RoleName;

/**
 * Rules for giving roles to people.
 */
public final class RoleAssignmentValidator {

    private RoleAssignmentValidator() {
    }

    /** EXTERNAL_CLIENT is only for third-party API clients, never for a person. */
    public static void checkNoExternalClientRole(Set<String> roleNames) {
        if (roleNames.contains(RoleName.EXTERNAL_CLIENT.name())) {
            throw new BusinessRuleException("EXTERNAL_CLIENT role can only be given to API clients");
        }
    }

    /** Admins cannot change their own roles or disable themselves (prevents accidental lock-out). */
    public static void checkNotSelf(UserEntity user, String actor, String message) {
        if (user.getUsername().equals(actor)) {
            throw new BusinessRuleException(message);
        }
    }
}
