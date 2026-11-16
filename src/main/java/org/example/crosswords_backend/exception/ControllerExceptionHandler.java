package org.example.crosswords_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ExceptionHandler(ModelNotAllowedException.class)
    public ResponseEntity<ErrorMessage> modelNotAllowed(ModelNotAllowedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // 429 Too Many Requests: the request is fine, but the user has used today's share.
    @ExceptionHandler(DailyLimitException.class)
    public ResponseEntity<ErrorMessage> dailyLimit(DailyLimitException ex, WebRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorMessage> invalidLogin(InvalidLoginException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NotOwnerException.class)
    public ResponseEntity<ErrorMessage> notOwner(NotOwnerException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCrosswordRequestException.class)
    public ResponseEntity<ErrorMessage> invalidCrosswordRequest(InvalidCrosswordRequestException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<ErrorMessage> invalidRegistration(InvalidRegistrationException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorMessage> usernameTaken(UsernameTakenException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailTakenException.class)
    public ResponseEntity<ErrorMessage> emailTaken(EmailTakenException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
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

    // Missing or broken JSON body. Without this it would end as a 500 from everythingElse.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> unreadableBody(HttpMessageNotReadableException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "The request body is missing or not valid JSON", request);
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

    // An unknown URL. Without this, Spring's NoResourceFoundException ended in everythingElse as a 500.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> noSuchPath(NoResourceFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "No such endpoint", request);
    }

    // E.g. a POST with Content-Type text/plain where JSON is expected. Was a 500 before.
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorMessage> unsupportedMediaType(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Send the body as application/json", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorMessage> methodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method " + ex.getMethod() + " is not allowed here", request);
    }

    // Everything we did not foresee. The real message can contain table names, SQL or other internals,
    // so it goes to our log (with the stack trace) and the client only gets a neutral text.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> everythingElse(Exception ex, WebRequest request) {
        log.error("Unexpected error on {}", request.getDescription(false), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ErrorMessage> build(HttpStatus status, String message, WebRequest request) {
        ErrorMessage body = new ErrorMessage(status.value(), LocalDateTime.now(), message, request.getDescription(false));
        return ResponseEntity.status(status).body(body);
    }
}
