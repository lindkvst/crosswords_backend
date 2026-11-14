package org.example.crosswords_backend.exception;

public class CrosswordApiException extends RuntimeException {
    public CrosswordApiException(String message) {
        super(message);
    }
}
