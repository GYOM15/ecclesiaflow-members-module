package com.ecclesiaflow.web.exception;

public class ConfirmationEmailException extends EmailSendingException {
    public ConfirmationEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
