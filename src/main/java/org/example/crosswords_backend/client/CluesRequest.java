package org.example.crosswords_backend.client;

import java.util.List;

public record CluesRequest(List<String> words, String language) {
}
