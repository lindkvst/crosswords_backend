package org.example.crosswords_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.crosswords_backend.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

// Runs when a request needs login and has no valid credentials.
// Spring's default for HTTP Basic adds "WWW-Authenticate: Basic", which makes the browser show its own
// login popup. We leave that header out and answer with JSON, so the frontend shows its own login form.
// It runs in the security filter, before any controller, so ControllerExceptionHandler never sees it.
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // The same text for unknown user and wrong password, so nobody can probe which usernames exist.
        ErrorMessage body = new ErrorMessage(HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now(),
                "Login required", "uri=" + request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
