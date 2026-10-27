package no.moonpark.pricing.exceptionhandler;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Builds RFC 7807 "problem+json" error bodies. Used by the exception handler and by servlet filters.
 */
public final class ProblemResponses {

    public static final String TYPE_BASE = "https://api.moonpark.no/problems/";

    private ProblemResponses() {
    }

    public static ProblemDetail create(HttpStatus status, String type, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }

    public static void write(HttpServletResponse response, ObjectMapper objectMapper,
                             HttpStatus status, String type, String detail) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), create(status, type, detail));
    }
}
