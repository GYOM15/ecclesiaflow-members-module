package com.ecclesiaflow.business.exceptions;

/**
 * Exception levée lorsque la mise à jour de l'email ne respecte pas le contrat métier.
 * <p>
 * Exemple : nouvel email identique à l'email actuel.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public class InvalidEmailUpdateException extends RuntimeException {

    public InvalidEmailUpdateException(String message) {
        super(message);
    }
}
