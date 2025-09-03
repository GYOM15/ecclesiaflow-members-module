package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.MembershipPassword;
import com.ecclesiaflow.business.mappers.web.PasswordSetMapper;
import com.ecclesiaflow.business.services.MemberPasswordService;
import com.ecclesiaflow.web.dto.SetPasswordRequest;
import com.ecclesiaflow.web.dto.PasswordSetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * Contrôleur REST pour la gestion des mots de passe des membres EcclesiaFlow.
 * <p>
 * Ce contrôleur gère spécifiquement la définition initiale des mots de passe
 * après confirmation d'inscription. Il fait partie du flux d'inscription complet
 * et délègue les opérations de mot de passe au module d'authentification externe.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Contrôleur web - Gestion des mots de passe</p>
 * 
 * <p><strong>Flux d'utilisation :</strong></p>
 * <ol>
 *   <li>Membre confirme son inscription → reçoit token temporaire (15 min)</li>
 *   <li>Membre utilise token pour définir son mot de passe initial</li>
 *   <li>Validation de la correspondance email/memberId</li>
 *   <li>Délégation au module d'authentification pour stockage sécurisé</li>
 * </ol>
 * 
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>Validation du token temporaire (Bearer token)</li>
 *   <li>Vérification de la correspondance email/memberId</li>
 *   <li>Délégation au module d'authentification pour le hachage</li>
 *   <li>Token à durée limitée (15 minutes)</li>
 * </ul>
 * 
 * <p><strong>Intégration :</strong></p>
 * <ul>
 *   <li>Module membres : validation de l'identité</li>
 *   <li>Module d'authentification : stockage sécurisé du mot de passe</li>
 *   <li>Système de tokens temporaires</li>
 * </ul>
 * 
 * <p><strong>Endpoints :</strong></p>
 * <ul>
 *   <li>POST /ecclesiaflow/members/{memberId}/password - Définir mot de passe initial</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberPasswordService
 * @see SetPasswordRequest
 */
@RestController
@RequestMapping("/ecclesiaflow/members")
@RequiredArgsConstructor
@Tag(name = "Member Password", description = "Gestion des mots de passe des membres")
public class MemberPasswordController {

    private final MemberPasswordService memberPasswordService;
    private final PasswordSetMapper passwordSetMapper;

    @Operation(
        summary = "Définir le mot de passe initial d'un membre",
        description = """
            Permet à un membre confirmé de définir son mot de passe initial après confirmation d'inscription.
            Nécessite un token temporaire valide (durée: 15 minutes) obtenu lors de la confirmation.
            Valide la correspondance entre l'email et l'ID du membre avant délégation au module d'authentification.
            """,
        tags = {"Member Password"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Mot de passe défini avec succès",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requête invalide - données manquantes, format incorrect ou token expiré",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.ecclesiaflow.web.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Erreur de validation",
                    value = """
                        {
                          "timestamp": "2023-12-01T10:30:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Le mot de passe doit contenir au moins 8 caractères",
                          "path": "/ecclesiaflow/members/123e4567-e89b-12d3-a456-426614174000/password"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token temporaire invalide ou expiré",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.ecclesiaflow.web.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Token expiré",
                    value = """
                        {
                          "timestamp": "2023-12-01T10:30:00",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Token temporaire expiré",
                          "path": "/ecclesiaflow/members/123e4567-e89b-12d3-a456-426614174000/password"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membre non trouvé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.ecclesiaflow.web.dto.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Membre inexistant",
                    value = """
                        {
                          "timestamp": "2023-12-01T10:30:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Aucun membre trouvé avec cet ID",
                          "path": "/ecclesiaflow/members/123e4567-e89b-12d3-a456-426614174000/password"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur interne du serveur",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.ecclesiaflow.web.dto.ErrorResponse.class)
            )
        )
    })
    @PostMapping(value = "/{memberId}/password", produces = "application/vnd.ecclesiaflow.members.v1+json")
    public ResponseEntity<PasswordSetResponse> setPassword(
            @PathVariable UUID memberId,
            @RequestBody @Valid SetPasswordRequest request,
            @RequestHeader("Authorization") String authHeader) throws IllegalArgumentException  {

        String token = extractBearerToken(authHeader);
        MembershipPassword membershipPassword = passwordSetMapper.fromSetPasswordRequest(request);
        // Valider que l'email correspond au memberId via le module Members
        UUID realMemberId = memberPasswordService.getMemberIdByEmail(membershipPassword.getEmail());
        if (!realMemberId.equals(memberId)) {
            throw new IllegalArgumentException("L'email ne correspond pas à l'ID du membre");
        }
        memberPasswordService.setPassword(membershipPassword.getEmail(), membershipPassword.getPassword(), token);
        PasswordSetResponse response = passwordSetMapper.toSuccessResponse();
        return ResponseEntity.ok(response);
    }




    /**
     * Extrait le token du header Authorization
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Format du token invalide");
    }
}

