package org.example.crosswords_backend.service;

import java.util.function.Supplier;

// A result and how long the call took. The backend measures the time itself, so it can be trusted
// (unlike a time sent by the browser). Used for the calls to texs and Claude.
record Timed<T>(T result, int ms) {

    static <T> Timed<T> measure(Supplier<T> call) {
        long start = System.currentTimeMillis();
        T result = call.get();
        return new Timed<>(result, (int) (System.currentTimeMillis() - start));
    }
}
