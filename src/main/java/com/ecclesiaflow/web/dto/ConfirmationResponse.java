package com.ecclesiaflow.web.dto;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO représentant la réponse après confirmation réussie d'un compte membre.
 * <p>
 * Cette classe encapsule les données de réponse envoyées au client après
 * une confirmation de compte réussie. Contient le message de succès et
 * le token temporaire permettant de définir le mot de passe initial.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Réponse de confirmation</p>
 * 
 * <p><strong>Contenu de la réponse :</strong></p>
 * <ul>
 *   <li>Message de confirmation pour l'utilisateur</li>
 *   <li>Token temporaire pour accéder à la définition du mot de passe</li>
 *   <li>Durée de validité du token en secondes</li>
 *   <li>URL de redirection vers le module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Flux de génération :</strong></p>
 * <ol>
 *   <li>Service métier génère {@link MembershipConfirmationResult}</li>
 *   <li>Mapper convertit en ConfirmationResponse</li>
 *   <li>Contrôleur retourne la réponse au client</li>
 * </ol>
 * 
 * <p><strong>Utilisation côté client :</strong></p>
 * <ul>
 *   <li>Affichage du message de succès à l'utilisateur</li>
 *   <li>Redirection vers la page de définition du mot de passe</li>
 *   <li>Gestion de l'expiration du token temporaire</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, construction flexible, données cohérentes.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipConfirmationResult
 * @see com.ecclesiaflow.web.controller.MembersConfirmationController
 */
@Getter
@Builder
public class ConfirmationResponse {
    /**
     * Message de confirmation pour l'utilisateur.
     * <p>
     * Message informatif confirmant le succès de la validation du code
     * et guidant l'utilisateur vers les prochaines étapes (définition du mot de passe).
     * </p>
     */
    private String message;
    
    /**
     * Token temporaire pour définir le mot de passe.
     * <p>
     * Token sécurisé généré par le module d'authentification permettant
     * au membre nouvellement confirmé de définir son mot de passe initial.
     * Doit être utilisé dans les requêtes vers l'endpoint de définition de mot de passe.
     * </p>
     */
    private String temporaryToken;
    
    /**
     * Durée de validité du token temporaire en secondes.
     * <p>
     * Nombre de secondes pendant lesquelles le token temporaire reste valide.
     * Permet au client de calculer l'heure d'expiration et d'afficher
     * un compte à rebours à l'utilisateur.
     * </p>
     */
    private long expiresIn;
    
    /**
     * Endpoint pour définir le mot de passe.
     * <p>
     * URL complète vers l'endpoint du module d'authentification permettant
     * de définir le mot de passe. Utilisé pour les appels API directs
     * avec le token temporaire dans l'en-tête Authorization.
     * </p>
     */
    private String passwordEndpoint;
}
