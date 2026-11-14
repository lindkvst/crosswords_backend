package org.example.crosswords_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> notFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSubmissionException.class)
    public ResponseEntity<ErrorMessage> invalidSubmission(InvalidSubmissionException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // E.g. ?model=EXPENSIVE, which is not one of the enum values.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> invalidParameter(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> missingParameter(MissingServletRequestParameterException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing parameter '" + ex.getParameterName() + "'", request);
    }

    @ExceptionHandler(ClaudeAnswerException.class)
    public ResponseEntity<ErrorMessage> claudeAnswerUnusable(ClaudeAnswerException ex, WebRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
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
        // The upstream body explains the error (e.g. a wrong model name). Logged for us, not sent to the client.
        log.warn("External API answered {}: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
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
