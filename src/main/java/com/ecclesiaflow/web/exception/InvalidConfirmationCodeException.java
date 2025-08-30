package com.ecclesiaflow.web.exception;

/**
 * Exception lancée lorsque le code de confirmation est invalide
 */
public class InvalidConfirmationCodeException extends RuntimeException {
    
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
    
    public InvalidConfirmationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
