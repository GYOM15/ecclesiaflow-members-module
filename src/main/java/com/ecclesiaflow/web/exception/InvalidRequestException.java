package com.ecclesiaflow.web.exception;

/**
 * Thrown when a request contains invalid or inconsistent data that cannot
 * be caught by Bean Validation annotations. Maps to HTTP 400 Bad Request.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
