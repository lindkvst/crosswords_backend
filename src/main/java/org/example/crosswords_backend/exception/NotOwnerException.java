package org.example.crosswords_backend.exception;

// A logged-in user asked for something that belongs to another user.
public class NotOwnerException extends RuntimeException {
    public NotOwnerException(String message) {
        super(message);
    }
}
