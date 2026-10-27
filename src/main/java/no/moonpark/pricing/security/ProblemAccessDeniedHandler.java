package no.moonpark.pricing.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.exceptionhandler.ProblemResponses;

/**
 * Writes a 403 response as RFC 7807 problem JSON.
 */
@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        ProblemResponses.write(response, objectMapper, HttpStatus.FORBIDDEN, "forbidden",
                "You do not have permission to perform this action");
    }
}
