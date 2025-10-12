package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.ConfirmationRequestPayload;
import com.ecclesiaflow.web.model.ConfirmationResponse;
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
     * Confirme le compte d'un membre avec le code de confirmation.
     * <p>
     * Processus :
     * 1. Conversion du modèle OpenAPI vers l'objet métier
     * 2. Validation du code via le service métier
     * 3. Mise à jour du statut de confirmation
     * 4. Génération d'un token temporaire pour définir le mot de passe
     * 5. Transformation de la réponse vers le modèle OpenAPI
     * </p>
     * 
     * @param memberId Identifiant du membre
     * @param confirmationRequestPayload Payload contenant le code de confirmation (modèle OpenAPI)
     * @return Réponse avec token temporaire et URL de redirection
     */
    public ResponseEntity<ConfirmationResponse> confirmMember(
            UUID memberId, ConfirmationRequestPayload confirmationRequestPayload) {
        
        // Conversion du modèle OpenAPI vers l'objet métier
        MembershipConfirmation membershipConfirmation = MembershipConfirmation.builder()
                .memberId(memberId)
                .confirmationCode(confirmationRequestPayload.getCode())
                .build();
        
        // Validation et confirmation via le service métier
        MembershipConfirmationResult result = confirmationService.confirmMember(membershipConfirmation);
        
        // Transformation de la réponse vers le modèle OpenAPI
        ConfirmationResponse response = openApiModelMapper.createConfirmationResponse(result);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Renvoie un nouveau code de confirmation par email.
     * <p>
     * Cette méthode permet de renvoyer un code de confirmation si :
     * - Le code précédent a expiré
     * - Le membre n'a pas reçu l'email
     * - Le membre a perdu le code
     * </p>
     * 
     * @param memberId Identifiant du membre
     * @return Réponse vide avec statut 200
     */
    public ResponseEntity<Void> resendConfirmationCode(UUID memberId) {
        confirmationService.sendConfirmationCode(memberId);
        return ResponseEntity.ok().build();
    }
}
