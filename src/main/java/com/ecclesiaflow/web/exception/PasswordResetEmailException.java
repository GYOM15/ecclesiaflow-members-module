package com.ecclesiaflow.web.exception;

public class PasswordResetEmailException extends EmailSendingException {
    public PasswordResetEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
