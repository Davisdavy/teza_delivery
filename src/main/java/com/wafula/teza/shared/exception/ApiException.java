package com.wafula.teza.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for expected, client-facing errors that carry the HTTP status they
 * should map to. Thrown by application services and translated to a consistent
 * JSON body by {@link GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
