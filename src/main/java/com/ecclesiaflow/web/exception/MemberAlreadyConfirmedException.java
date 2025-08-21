package com.ecclesiaflow.web.exception;

/**
 * Exception lancée lorsqu'on tente de confirmer un membre déjà confirmé
 */
public class MemberAlreadyConfirmedException extends RuntimeException {
    
    public MemberAlreadyConfirmedException(String message) {
        super(message);
    }
    
    public MemberAlreadyConfirmedException(String message, Throwable cause) {
        super(message, cause);
    }
}
