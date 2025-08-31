package com.ecclesiaflow.web.exception;

/**
 * Exception levée lorsqu'un code de confirmation est invalide ou expiré.
 * <p>
 * Cette exception est utilisée lors du processus de confirmation des comptes membres
 * lorsque le code fourni ne correspond pas au code attendu, est expiré, ou a déjà
 * été utilisé. Elle fait partie du système de sécurité pour prévenir les tentatives
 * de confirmation frauduleuses.
 * </p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Code de confirmation incorrect saisi par l'utilisateur</li>
 *   <li>Code expiré (dépassement de la durée de validité)</li>
 *   <li>Code déjà utilisé pour une confirmation précédente</li>
 *   <li>Tentatives d'attaque par force brute sur les codes</li>
 *   <li>Code inexistant en base de données</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong> Les messages d'erreur sont volontairement génériques
 * pour ne pas révéler d'informations sensibles aux attaquants.</p>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 400 avec message d'erreur standardisé.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 * @see com.ecclesiaflow.business.services.MemberConfirmationService
 */
public class InvalidConfirmationCodeException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur décrivant le problème de validation
     */
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant le problème de validation
     * @param cause la cause sous-jacente de l'exception
     */
    public InvalidConfirmationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
