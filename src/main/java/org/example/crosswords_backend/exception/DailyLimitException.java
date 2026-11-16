package org.example.crosswords_backend.exception;

public class DailyLimitException extends RuntimeException {
    public DailyLimitException(String message) {
        super(message);
    }
}
