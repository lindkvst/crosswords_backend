package org.example.crosswords_backend.exception;

// Claude answered, but the answer could not be used.
public class ClaudeAnswerException extends RuntimeException {
    public ClaudeAnswerException(String message) {
        super(message);
    }
}
