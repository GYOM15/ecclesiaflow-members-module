package com.ecclesiaflow.business.exceptions;

/**
 * Exception levée lors d'une tentative de confirmation d'un membre déjà confirmé.
 * <p>
 * Cette exception est utilisée pour prévenir les tentatives de double confirmation
 * d'un compte membre. Elle fait partie du système de sécurité pour maintenir
 * l'intégrité des états de confirmation et éviter les opérations redondantes.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception métier - Gestion des états de confirmation</p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Tentative de confirmation avec un code valide sur un compte déjà confirmé</li>
 *   <li>Utilisation multiple du même lien de confirmation</li>
 *   <li>Tentatives de re-confirmation par l'utilisateur</li>
 *   <li>Protection contre les attaques de replay</li>
 * </ul>
 * 
 * <p><strong>Logique métier :</strong></p>
 * <ul>
 *   <li>Vérification de l'état {@code confirmed} du membre avant traitement</li>
 *   <li>Prévention des modifications d'état incohérentes</li>
 *   <li>Maintien de l'idempotence des opérations de confirmation</li>
 * </ul>
 * 
 * <p><strong>Comportement attendu :</strong> L'utilisateur est informé que son compte
 * est déjà confirmé et peut procéder à la connexion.</p>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 409 (Conflict) avec message d'information.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.business.services.MemberConfirmationService
 * @see com.ecclesiaflow.io.entities.Member#isConfirmed()
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public class MemberAlreadyConfirmedException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur décrivant la tentative de double confirmation
     */
    public MemberAlreadyConfirmedException(String message) {
        super(message);
    }
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant la tentative de double confirmation
     * @param cause la cause sous-jacente de l'exception
     */
    public MemberAlreadyConfirmedException(String message, Throwable cause) {
        super(message, cause);
    }
}
