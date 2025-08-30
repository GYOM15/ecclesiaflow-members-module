package com.ecclesiaflow.web.exception;

public class WelcomeEmailException extends EmailSendingException {
    public WelcomeEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
