package com.ecclesiaflow.business.exceptions;

/**
 * Exception levée lorsqu'une tentative d'utilisation d'un email déjà existant est détectée.
 * <p>
 * Typiquement utilisée lors de l'inscription ou de la mise à jour du profil membre.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}

