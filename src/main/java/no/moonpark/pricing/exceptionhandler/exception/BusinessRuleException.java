package no.moonpark.pricing.exceptionhandler.exception;

/** The request is well-formed but breaks a business rule (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
