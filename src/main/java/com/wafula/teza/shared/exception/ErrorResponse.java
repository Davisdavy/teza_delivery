package com.wafula.teza.shared.exception;

import java.time.Instant;

/**
 * The single JSON error shape returned by the API.
 *
 * @param status    HTTP status code.
 * @param error     short machine-readable code (e.g. {@code "bad_request"}).
 * @param message   human-readable detail.
 * @param timestamp when the error was produced.
 */
public record ErrorResponse(int status, String error, String message, Instant timestamp) {

    public static ErrorResponse of(org.springframework.http.HttpStatus status, String message) {
        return new ErrorResponse(
                status.value(),
                status.name().toLowerCase().replace(' ', '_'),
                message,
                Instant.now());
    }
}
