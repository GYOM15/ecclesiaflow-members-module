package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.dto.MemberResponse;
import com.ecclesiaflow.web.dto.SignUpRequest;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.business.mappers.MemberMapper;
import com.ecclesiaflow.business.mappers.MemberResponseMapper;
import com.ecclesiaflow.web.dto.UpdateMemberRequest;
import com.ecclesiaflow.business.mappers.MemberUpdateMapper;
import com.ecclesiaflow.business.domain.MembershipUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * CONTRÔLEUR TEMPORAIRE - À MIGRER VERS LE MODULE DE GESTION DES MEMBRES
 * 
 * Ce contrôleur sera déplacé vers ecclesiaflow-member-management-module
 * dans la future architecture multi-tenant où :
 * - Les pasteurs (admins tenant) créent les membres
 * - Les demandes d'inscription sont soumises pour approbation
 * - La gestion des membres est séparée de l'authentification
 */
@RestController
@RequestMapping("/ecclesiaflow")
@RequiredArgsConstructor
@Tag(name = "Members (Temporary)", description = "API temporaire - sera migrée vers le module de gestion des membres")
public class MembersController {
    private final MemberService memberService;
    private final MemberUpdateMapper memberUpdateMapper;

    @GetMapping(value = "/hello", produces = "application/vnd.ecclesiaflow.members.v2+json")
    @Operation(
        summary = "Message de bienvenue pour les membres",
        description = "Endpoint de test pour les membres authentifiés"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Message de bienvenue",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "Hi Member")
            )
        )
    })
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hi Member");
    }

    @PostMapping(value = "/members", produces = "application/vnd.ecclesiaflow.members.v2+json")
    @Operation(
        summary = "[TEMPORAIRE] Auto-enregistrement d'un membre",
        description = "SERA REMPLACÉ par un système d'approbation admin dans le module de gestion des membres"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Membre créé avec succès (temporaire - sera un système d'approbation)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MemberResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données d'enregistrement invalides ou email déjà utilisé",
            content = @Content
        )
    })
    public ResponseEntity<MemberResponse> registerMember(@Valid @RequestBody SignUpRequest signUpRequest) {
        MembershipRegistration registration = MemberMapper.fromSignUpRequest(signUpRequest);
        Member member = memberService.registerMember(registration);
        MemberResponse response = MemberResponseMapper.fromMember(member, "Member registered (temporary - approval system coming)");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/members/{memberId}", produces = "application/vnd.ecclesiaflow.members.v2+json")
    @Operation(
            summary = "Obtenir les informations d'un membre",
            description = "Récupérer les détails d'un membre par son ID"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Membre trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé",
                    content = @Content
            )
    })
    public ResponseEntity<MemberResponse> getMember(@PathVariable UUID memberId) {
        Member member = memberService.findById(memberId);
        MemberResponse response = MemberResponseMapper.fromMember(member, "Membre trouvé");
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/members/{memberId}", produces = "application/vnd.ecclesiaflow.members.v2+json")
    @Operation(
            summary = "Modifier un membre",
            description = "Mettre à jour les informations d'un membre"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Membre modifié avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé",
                    content = @Content
            )
    })
    public ResponseEntity<MemberResponse> updateMember( @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRequest updateRequest) {
        MembershipUpdate businessRequest = memberUpdateMapper.fromUpdateMemberRequest(memberId, updateRequest);
        Member updatedMember = memberService.updateMember(businessRequest);
        MemberResponse response = MemberResponseMapper.fromMember(updatedMember, "Membre modifié avec succès");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/members", produces = "application/vnd.ecclesiaflow.members.v2+json")
    @Operation(
            summary = "[TEMPORAIRE] Lister tous les membres",
            description = "Endpoint temporaire pour les tests - récupérer tous les membres"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des membres",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<?> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/members/{email}/confirmation-status")
    @Operation(
            summary = "Vérifier le statut de confirmation d'un membre",
            description = "Endpoint interne pour que le module d'auth vérifie si un membre est confirmé"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut de confirmation récupéré",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé"
            )
    })
    public ResponseEntity<Map<String, Boolean>> getMemberConfirmationStatus(@PathVariable String email) {
        boolean isConfirmed = memberService.isEmailConfirmed(email);
        return ResponseEntity.ok(Map.of("confirmed", isConfirmed));
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(
            summary = "Supprimer un membre",
            description = "Supprimer définitivement un membre"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Membre supprimé avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé"
            )
    })
    public ResponseEntity<Void> deleteMember(@PathVariable UUID memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

}
