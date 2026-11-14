package org.example.crosswords_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> notFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CrosswordApiException.class)
    public ResponseEntity<ErrorMessage> crosswordApiFailed(CrosswordApiException ex, WebRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // The external service answered with an error status (wrong key, quota used up, ...).
    // 502 Bad Gateway tells our client that the fault lies upstream, not in its request.
    // Only the status is passed on, never the upstream body or our request headers.
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorMessage> externalCallFailed(WebClientResponseException ex, WebRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "External API answered with status " + ex.getStatusCode().value(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> everythingElse(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorMessage> build(HttpStatus status, String message, WebRequest request) {
        ErrorMessage body = new ErrorMessage(status.value(), LocalDateTime.now(), message, request.getDescription(false));
        return ResponseEntity.status(status).body(body);
    }
}
