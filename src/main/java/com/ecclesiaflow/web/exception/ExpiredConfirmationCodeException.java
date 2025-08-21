package com.ecclesiaflow.web.exception;

/**
 * Exception lancée lorsque le code de confirmation a expiré
 */
public class ExpiredConfirmationCodeException extends RuntimeException {
    
    public ExpiredConfirmationCodeException(String message) {
        super(message);
    }
    
    public ExpiredConfirmationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
