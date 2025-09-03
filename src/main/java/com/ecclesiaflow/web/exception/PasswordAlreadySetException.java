package com.ecclesiaflow.web.exception;

/**
 * Exception levée lorsqu'un utilisateur tente de redéfinir un mot de passe déjà configuré.
 * <p>
 * Cette exception empêche la réutilisation des tokens temporaires pour modifier
 * un mot de passe qui a déjà été défini, renforçant ainsi la sécurité du système.
 * </p>
 */
public class PasswordAlreadySetException extends RuntimeException {
    
    public PasswordAlreadySetException(String message) {
        super(message);
    }
    
    public PasswordAlreadySetException(String message, Throwable cause) {
        super(message, cause);
    }
}
