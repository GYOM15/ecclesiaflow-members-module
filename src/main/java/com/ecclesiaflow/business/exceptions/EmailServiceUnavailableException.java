package com.ecclesiaflow.business.exceptions;

/**
 * Exception thrown when email service is temporarily unavailable.
 */
public class EmailServiceUnavailableException extends RuntimeException {

    private final String serviceName;

    public EmailServiceUnavailableException(String serviceName, Throwable cause) {
        super(String.format("Service '%s' is temporarily unavailable. Please try again later.", serviceName), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
