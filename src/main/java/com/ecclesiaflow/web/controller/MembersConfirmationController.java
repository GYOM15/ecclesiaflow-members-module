package com.ecclesiaflow.web.controller;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.mappers.ConfirmationResponseMapper;
import com.ecclesiaflow.business.mappers.ConfirmationRequestMapper;
import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.web.dto.ConfirmationRequest;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion du processus de confirmation des membres EcclesiaFlow.
 * <p>
 * Ce contrôleur gère le processus complet de confirmation des comptes membres :
 * validation des codes de confirmation envoyés par email, mise à jour du statut
 * de confirmation, et génération de tokens temporaires pour la définition des mots de passe.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Couche de présentation - API REST Confirmation</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Validation des codes de confirmation saisis par les membres</li>
 *   <li>Orchestration du processus de confirmation avec les services métier</li>
 *   <li>Génération et retour des tokens temporaires</li>
 *   <li>Gestion des erreurs de confirmation (code invalide, expiré, etc.)</li>
 *   <li>Endpoints de débogage pour les tests (temporaires)</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberConfirmationService} - Logique métier de confirmation</li>
 *   <li>Mappers - Transformation DTOs ↔ objets métier</li>
 *   <li>Spring Web MVC - Framework REST</li>
 * </ul>
 * 
 * <p><strong>Endpoints exposés :</strong></p>
 * <ul>
 *   <li>POST /ecclesiaflow/members/{memberId}/confirmation - Confirmer un compte</li>
 *   <li>GET /ecclesiaflow/members/{memberId}/confirmation/debug/code - Debug (temporaire)</li>
 * </ul>
 * 
 * <p><strong>Flux typique :</strong></p>
 * <ol>
 *   <li>Membre reçoit un email avec code de confirmation</li>
 *   <li>Membre saisit le code via l'interface</li>
 *   <li>Validation du code et mise à jour du statut</li>
 *   <li>Génération d'un token temporaire pour définir le mot de passe</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Validation automatique, sécurité des codes, gestion d'erreurs.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("ecclesiaflow/members/{memberId}")
@RequiredArgsConstructor
@Tag(name = "Member Confirmation", description = "Confirmation des comptes membres")
public class MembersConfirmationController {

    private final MemberConfirmationService confirmationService;
    private final ConfirmationResponseMapper confirmationResponseMapper;
    private final ConfirmationRequestMapper confirmationRequestMapper;

    @PostMapping(value = "/confirmation", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
            summary = "Confirmer le compte d'un membre",
            description = "Confirmer l'inscription d'un membre avec le code reçu par email. " +
                    "Génère un token temporaire pour définir le mot de passe."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Confirmation réussie, token temporaire généré",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConfirmationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Code de confirmation invalide ou expiré",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Compte déjà confirmé",
                    content = @Content
            )
    })
    public ResponseEntity<ConfirmationResponse> confirmMember(
            @PathVariable UUID memberId,
            @Valid @RequestBody ConfirmationRequest confirmationRequest) {
        MembershipConfirmation businessRequest = confirmationRequestMapper.fromConfirmationRequest(memberId, confirmationRequest);
        MembershipConfirmationResult result = confirmationService.confirmMember(businessRequest);
        ConfirmationResponse response = confirmationResponseMapper.fromMemberConfirmationResult(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/confirmation-code", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
            summary = "Renvoyer le code de confirmation",
            description = "Renvoyer un nouveau code de confirmation par email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Nouveau code envoyé"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Compte déjà confirmé"
            )
    })
    public ResponseEntity<Void> resendConfirmationCode(@PathVariable UUID memberId) {
        confirmationService.sendConfirmationCode(memberId);
        return ResponseEntity.ok().build();
    }

}