package com.query.test.exception;

import java.time.LocalDateTime;

public class ApiError {

    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
