package org.example.crosswords_backend.exception;

public class InvalidCrosswordRequestException extends RuntimeException {
    public InvalidCrosswordRequestException(String message) {
        super(message);
    }
}
