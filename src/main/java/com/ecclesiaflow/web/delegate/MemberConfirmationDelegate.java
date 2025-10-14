package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.ResendConfirmationLink200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Délégué pour la gestion des confirmations de membres - Pattern Delegate avec OpenAPI Generator.
 * <p>
 * Ce délégué contient toute la logique métier pour le processus de confirmation des comptes membres,
 * séparant ainsi les responsabilités entre le contrôleur (gestion HTTP) et la logique applicative.
 * </p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <pre>
 * MembersConfirmationController (implémente MemberConfirmationApi)
 *    ↓ délègue à
 * MemberConfirmationDelegate ← Cette classe
 *    ↓ utilise
 * MemberConfirmationService (logique métier)
 * </pre>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Transformation des modèles OpenAPI vers les objets métier</li>
 *   <li>Orchestration des appels aux services de confirmation</li>
 *   <li>Transformation des résultats métier vers les modèles OpenAPI</li>
 *   <li>Gestion des codes de statut HTTP appropriés</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
public class MemberConfirmationDelegate {

    private final MemberConfirmationService confirmationService;
    private final OpenApiModelMapper openApiModelMapper;

    /**
     * Confirme le compte d'un membre avec le token de confirmation reçu par email.
     * <p>
     * Processus :
     * 1. Validation du token via le service métier
     * 2. Mise à jour du statut de confirmation
     * 3. Génération d'un token temporaire pour définir le mot de passe
     * 4. Suppression du token de confirmation (usage unique)
     * 5. Transformation de la réponse vers le modèle OpenAPI
     * </p>
     * 
     * @param token Token de confirmation UUID reçu par email
     * @return Réponse avec token temporaire et endpoint pour définir le mot de passe
     */
    public ResponseEntity<ConfirmationResponse> confirmMemberByToken(UUID token) {
        // Validation et confirmation via le service métier
        MembershipConfirmationResult result = confirmationService.confirmMemberByToken(token);
        
        // Transformation de la réponse vers le modèle OpenAPI
        ConfirmationResponse response = openApiModelMapper.createConfirmationResponse(result);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Renvoie un nouveau lien de confirmation par email via l'adresse email du membre.
     * <p>
     * Cette méthode permet de renvoyer un lien de confirmation si :
     * - Le token précédent a expiré
     * - Le membre n'a pas reçu l'email
     * - Le membre a perdu le lien
     * </p>
     * 
     * <p><strong>Sécurité anti-énumération (approche hybride):</strong>
     * <ul>
     *   <li>Retourne 200 OK même si l'email n'existe pas (anti-énumération)</li>
     *   <li>Retourne 409 Conflict si le compte est déjà confirmé (UX claire)</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Processus :
     * 1. Recherche du membre par email
     * 2. Vérification du statut de confirmation
     * 3. Invalidation de l'ancien token (si existant)
     * 4. Génération d'un nouveau token UUID sécurisé
     * 5. Envoi du nouveau lien par email
     * </p>
     * 
     * @param email Adresse email du membre
     * @return Réponse avec message de confirmation et durée de validité (24h)
     * @throws MemberAlreadyConfirmedException si le compte est déjà confirmé (409 Conflict)
     */
    public ResponseEntity<ResendConfirmationLink200Response> resendConfirmationLink(String email) {
        confirmationService.sendConfirmationLink(email);
        
        ResendConfirmationLink200Response response = new ResendConfirmationLink200Response()
                .message("Si cette adresse email est associée à un compte non confirmé, un nouveau lien de confirmation a été envoyé.")
                .expiresIn(86400L);
        
        return ResponseEntity.ok(response);
    }
}
