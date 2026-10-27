package no.moonpark.pricing.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.exceptionhandler.ProblemResponses;

/**
 * Writes a 401 response as RFC 7807 problem JSON. Never reveals why authentication failed.
 */
@Component
@RequiredArgsConstructor
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        ProblemResponses.write(response, objectMapper, HttpStatus.UNAUTHORIZED, "unauthorized",
                "Missing or invalid credentials");
    }
}
