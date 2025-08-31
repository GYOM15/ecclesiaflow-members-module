package com.ecclesiaflow.web.exception;

/**
 * Exception levée lors d'une requête contenant des paramètres ou des données invalides.
 * <p>
 * Cette exception générique est utilisée pour signaler des erreurs de validation
 * métier ou des incohérences dans les données de requête qui ne relèvent pas
 * des validations automatiques de Spring (Bean Validation).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception métier - Validation des requêtes</p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Paramètres de requête incohérents ou contradictoires</li>
 *   <li>Violations de règles métier complexes</li>
 *   <li>Données de requête corrompues ou malformées</li>
 *   <li>Tentatives d'opérations non autorisées selon le contexte</li>
 *   <li>Validation cross-field échouée</li>
 * </ul>
 * 
 * <p><strong>Différence avec les autres exceptions :</strong></p>
 * <ul>
 *   <li>InvalidRequestException : Erreurs générales de validation métier</li>
 *   <li>MethodArgumentNotValidException : Erreurs de validation Bean Validation</li>
 *   <li>InvalidConfirmationCodeException : Erreurs spécifiques aux codes</li>
 * </ul>
 * 
 * <p><strong>Usage recommandé :</strong> Utiliser cette exception pour des validations
 * métier complexes qui ne peuvent pas être exprimées par des annotations de validation.</p>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 400 (Bad Request) avec message d'erreur détaillé.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public class InvalidRequestException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur décrivant la validation échouée
     */
    public InvalidRequestException(String message) {
        super(message);
    }
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant la validation échouée
     * @param cause la cause sous-jacente de l'exception
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
