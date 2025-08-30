package com.ecclesiaflow.web.controller;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.mappers.ConfirmationResponseMapper;
import com.ecclesiaflow.business.mappers.ConfirmationRequestMapper;
import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.io.repository.MemberConfirmationRepository;
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
 * Contrôleur pour la confirmation des comptes membres
 *
 * Gère la confirmation des comptes via code envoyé par email
 * et génère un token temporaire pour définir le mot de passe
 */
@RestController
@RequestMapping("ecclesiaflow/members/{memberId}/confirmation")
@RequiredArgsConstructor
@Tag(name = "Member Confirmation", description = "Confirmation des comptes membres")
public class MembersConfirmationController {

    private final MemberConfirmationService confirmationService;
    private final ConfirmationResponseMapper confirmationResponseMapper;
    private final ConfirmationRequestMapper confirmationRequestMapper;

    @PostMapping(produces = "application/vnd.ecclesiaflow.members.v2+json")
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

    @PostMapping(value = "/resend", produces = "application/vnd.ecclesiaflow.members.v2+json")
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