package com.ecclesiaflow.business.exceptions;

/**
 * Exception levée lorsqu'un membre demandé n'existe pas dans le système.
 * <p>
 * Cette exception est utilisée dans les opérations de recherche, mise à jour,
 * suppression ou confirmation de membres lorsque l'identifiant fourni ne
 * correspond à aucun membre en base de données.
 * </p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Recherche d'un membre par ID inexistant</li>
 *   <li>Tentative de mise à jour d'un membre supprimé</li>
 *   <li>Processus de confirmation sur un membre inexistant</li>
 *   <li>Opérations administratives sur des comptes invalides</li>
 * </ul>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 404 avec message d'erreur standardisé.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public class MemberNotFoundException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur détaillant le membre non trouvé
     */
    public MemberNotFoundException(String message) {
        super(message);
    }
}
