package com.ecclesiaflow.business.exceptions;

/**
 * Exception levée lorsqu'un utilisateur tente d'accéder à une ressource
 * sans avoir les permissions (scopes) nécessaires.
 * <p>
 * Cette exception correspond au code HTTP 403 Forbidden.
 * </p>
 * 
 * <p><strong>Cas d'usage :</strong></p>
 * <ul>
 *   <li>L'utilisateur est authentifié mais n'a pas les scopes requis</li>
 *   <li>Tentative d'accès à une ressource avec des permissions insuffisantes</li>
 *   <li>Validation de scopes échouée (AND ou OR)</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.business.security.ScopeValidator
 * @see com.ecclesiaflow.business.security.RequireScopes
 */
public class InsufficientPermissionsException extends RuntimeException {

    /**
     * Construit une nouvelle exception avec un message détaillé.
     * 
     * @param message message d'erreur décrivant les permissions manquantes
     */
    public InsufficientPermissionsException(String message) {
        super(message);
    }
}
