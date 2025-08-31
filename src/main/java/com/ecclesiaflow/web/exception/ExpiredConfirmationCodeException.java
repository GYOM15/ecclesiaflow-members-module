package com.ecclesiaflow.web.exception;

/**
 * Exception levée lorsqu'un code de confirmation a dépassé sa durée de validité.
 * <p>
 * Cette exception est spécifiquement utilisée lors du processus de confirmation des comptes
 * membres lorsque le code fourni est correct mais a expiré selon les règles temporelles
 * définies dans l'application (typiquement 24h pour les codes initiaux, 5 min pour les renvois).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception métier - Gestion de l'expiration temporelle</p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Code de confirmation initial expiré après 24h</li>
 *   <li>Code de renvoi expiré après 5 minutes</li>
 *   <li>Tentative de confirmation tardive par l'utilisateur</li>
 *   <li>Validation temporelle lors du processus de confirmation</li>
 * </ul>
 * 
 * <p><strong>Différence avec InvalidConfirmationCodeException :</strong></p>
 * <ul>
 *   <li>ExpiredConfirmationCodeException : Code valide mais expiré</li>
 *   <li>InvalidConfirmationCodeException : Code incorrect ou inexistant</li>
 * </ul>
 * 
 * <p><strong>Comportement attendu :</strong> L'utilisateur doit demander un nouveau
 * code de confirmation via l'endpoint de renvoi.</p>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 400 avec message d'erreur spécifique à l'expiration.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.InvalidConfirmationCodeException
 * @see com.ecclesiaflow.business.services.MemberConfirmationService
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public class ExpiredConfirmationCodeException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur décrivant l'expiration du code
     */
    public ExpiredConfirmationCodeException(String message) {
        super(message);
    }
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant l'expiration du code
     * @param cause la cause sous-jacente de l'exception
     */
    public ExpiredConfirmationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
