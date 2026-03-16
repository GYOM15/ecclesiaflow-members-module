package com.ecclesiaflow.business.exceptions;

import com.ecclesiaflow.application.logging.SecurityMaskingUtils;

/**
 * Exception thrown on email service failures.
 */
public class EmailServiceException extends RuntimeException {

    private final String emailAddress;
    private final EmailOperation operation;

    public EmailServiceException(String message, String emailAddress, EmailOperation operation) {
        super(message);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    public EmailServiceException(String message, String emailAddress, EmailOperation operation, Throwable cause) {
        super(message, cause);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    public String getMaskedEmailAddress() {
        return SecurityMaskingUtils.maskEmail(emailAddress);
    }

    public EmailOperation getOperation() {
        return operation;
    }

    public enum EmailOperation {
        CONFIRMATION,
        WELCOME,
        EMAIL_CHANGED
    }
}
