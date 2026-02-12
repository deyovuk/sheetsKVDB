package com.example.sheetkv.exception;

import org.springframework.http.HttpStatus;

public class BackendException extends ApiException {
    public BackendException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public BackendException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
