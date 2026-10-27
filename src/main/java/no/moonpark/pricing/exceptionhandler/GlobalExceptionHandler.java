package no.moonpark.pricing.exceptionhandler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import no.moonpark.pricing.exceptionhandler.exception.BusinessRuleException;
import no.moonpark.pricing.exceptionhandler.exception.ConflictException;
import no.moonpark.pricing.exceptionhandler.exception.InvalidParkingPeriodException;
import no.moonpark.pricing.exceptionhandler.exception.NotFoundException;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;

/**
 * Turns exceptions into RFC 7807 problem responses. Internal details (stack traces, SQL) are never returned.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidParkingPeriodException.class)
    ProblemDetail handleInvalidPeriod(InvalidParkingPeriodException e) {
        return ProblemResponses.create(HttpStatus.BAD_REQUEST, "invalid-parking-period", e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail handleBadParameter(Exception e) {
        return ProblemResponses.create(HttpStatus.BAD_REQUEST, "invalid-request", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(HttpMessageNotReadableException e) {
        return ProblemResponses.create(HttpStatus.BAD_REQUEST, "invalid-request", "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemResponses.create(HttpStatus.BAD_REQUEST, "validation-failed", "Request has invalid fields");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    ProblemDetail handleParameterValidation(Exception e) {
        return ProblemResponses.create(HttpStatus.BAD_REQUEST, "validation-failed", "Request has invalid parameters");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return ProblemResponses.create(HttpStatus.BAD_REQUEST, "invalid-request", e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException e) {
        return ProblemResponses.create(HttpStatus.UNAUTHORIZED, "unauthorized", "Invalid username or password");
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    ProblemDetail handleAccessDenied(Exception e) {
        return ProblemResponses.create(HttpStatus.FORBIDDEN, "forbidden", "You do not have permission to do this");
    }

    @ExceptionHandler({UnknownZoneException.class, NotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException e) {
        return ProblemResponses.create(HttpStatus.NOT_FOUND, "not-found", e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResource(NoResourceFoundException e) {
        return ProblemResponses.create(HttpStatus.NOT_FOUND, "not-found", "No such endpoint");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ProblemResponses.create(HttpStatus.METHOD_NOT_ALLOWED, "method-not-allowed", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException e) {
        return ProblemResponses.create(HttpStatus.CONFLICT, "conflict", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLock(OptimisticLockingFailureException e) {
        return ProblemResponses.create(HttpStatus.CONFLICT, "conflict", "The data was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return ProblemResponses.create(HttpStatus.CONFLICT, "conflict", "The change conflicts with existing data");
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRule(BusinessRuleException e) {
        return ProblemResponses.create(HttpStatus.UNPROCESSABLE_ENTITY, "business-rule-violation", e.getMessage());
    }

    @ExceptionHandler(BulkheadFullException.class)
    ProblemDetail handleOverload(BulkheadFullException e) {
        ProblemDetail problem = ProblemResponses.create(HttpStatus.SERVICE_UNAVAILABLE, "overloaded",
                "The service is busy. Please retry shortly.");
        problem.setProperty("retryAfterSeconds", 1);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ProblemResponses.create(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "An unexpected error occurred");
    }
}
