package org.example.crosswords_backend.exception;

public class ModelNotAllowedException extends RuntimeException {
    public ModelNotAllowedException(String message) {
        super(message);
    }
}
