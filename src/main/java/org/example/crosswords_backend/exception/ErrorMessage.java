package org.example.crosswords_backend.exception;

import java.time.LocalDateTime;

public record ErrorMessage(int statusCode, LocalDateTime timeStamp, String message, String description) {
}
