package no.moonpark.pricing.exceptionhandler.exception;

/** The request conflicts with existing data, e.g. a duplicate username (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
