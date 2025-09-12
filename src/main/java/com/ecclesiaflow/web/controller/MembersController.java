package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.dto.MemberPageResponse;
import com.ecclesiaflow.web.dto.SignUpResponse;
import com.ecclesiaflow.web.payloads.SignUpRequestPayload;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.web.mappers.SignUpRequestMapper;
import com.ecclesiaflow.web.mappers.MemberPageMapper;
import com.ecclesiaflow.web.mappers.MemberResponseMapper;
import com.ecclesiaflow.web.payloads.UpdateMemberRequestPayload;
import com.ecclesiaflow.web.mappers.UpdateRequestMapper;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des membres EcclesiaFlow.
 * <p>
 * Ce contrôleur expose les endpoints HTTP pour les opérations CRUD sur les membres :
 * inscription, consultation, mise à jour et suppression. Respecte les principes
 * REST et utilise une architecture en couches avec mappers pour la transformation
 * des données entre DTOs et objets métier.
 * </p>
 * 
 * <p><strong>⚠️ STATUT TEMPORAIRE :</strong> Ce contrôleur sera migré vers le module
 * ecclesiaflow-member-management-module dans la future architecture multi-tenant.</p>
 * 
 * <p><strong>Rôle architectural :</strong> Couche de présentation - API REST</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Exposition des endpoints HTTP pour la gestion des membres</li>
 *   <li>Validation des données d'entrée via annotations Bean Validation</li>
 *   <li>Transformation des DTOs en objets métier via mappers</li>
 *   <li>Gestion des codes de statut HTTP appropriés</li>
 *   <li>Documentation OpenAPI/Swagger intégrée</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberService} - Logique métier des membres</li>
 *   <li>Mappers - Transformation DTOs ↔ objets métier</li>
 *   <li>Spring Web MVC - Framework REST</li>
 * </ul>
 * 
 * <p><strong>Endpoints exposés :</strong></p>
 * <ul>
 *   <li>GET /ecclesiaflow/hello - Test d'authentification</li>
 *   <li>POST /ecclesiaflow/members - Inscription d'un nouveau membre</li>
 *   <li>GET /ecclesiaflow/members/{id} - Consultation d'un membre</li>
 *   <li>PATCH /ecclesiaflow/members/{id} - Mise à jour d'un membre</li>
 *   <li>DELETE /ecclesiaflow/members/{id} - Suppression d'un membre</li>
 *   <li>GET /ecclesiaflow/members - Liste de tous les membres</li>
 *   <li>GET /ecclesiaflow/members/{email}/confirmation-status - Statut de confirmation</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Validation automatique, gestion d'erreurs, documentation OpenAPI.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/ecclesiaflow")
@RequiredArgsConstructor
@Tag(name = "Members (Temporary)", description = "API temporaire - sera migrée vers le module de gestion des membres")
public class MembersController {
    private final MemberService memberService;
    private final UpdateRequestMapper updateRequestMapper;
    private final MemberPageMapper memberPageMapper;

    /**
     * Endpoint de test d'authentification pour les membres.
     * <p>
     * Cette méthode simple permet de vérifier que l'authentification JWT
     * fonctionne correctement pour les membres connectés. Retourne un message
     * de bienvenue basique.
     * </p>
     * 
     * @return {@link ResponseEntity} contenant le message "Hi Member" avec statut HTTP 200
     * 
     * @implNote Endpoint sécurisé nécessitant un token JWT valide.
     */
    @GetMapping(value = "/hello", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
        summary = "Message de bienvenue pour les membres",
        description = "Endpoint de test pour les membres authentifiés"
    )
    @SecurityRequirement(name = "BearerAuth")
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

    @PostMapping(value = "/members", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @RateLimiter(name = "member-registration")
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
                schema = @Schema(implementation = SignUpResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données d'enregistrement invalides ou email déjà utilisé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/BadRequestError")
            )
        )
    })
    /**
     * Enregistre un nouveau membre dans le système.
     * <p>
     * Cette méthode permet l'auto-inscription d'un nouveau membre.
     * Elle valide les données d'entrée, transforme le DTO en objet métier,
     * appelle le service pour l'enregistrement, puis retourne la réponse formatée.
     * </p>
     * 
     * <p><strong>⚠️ TEMPORAIRE :</strong> Sera remplacé par un système d'approbation admin.</p>
     * 
     * @param signUpRequestPayload les données d'inscription du membre, validées automatiquement
     * @return {@link ResponseEntity} avec {@link SignUpResponse} et statut HTTP 201
     * @throws org.springframework.web.bind.MethodArgumentNotValidException si les données sont invalides
     * @throws IllegalArgumentException si l'email existe déjà
     * 
     * @implNote Utilise le pattern Mapper pour la transformation DTO → Objet métier → DTO.
     */
    public ResponseEntity<SignUpResponse> registerMember(@Valid @RequestBody SignUpRequestPayload signUpRequestPayload) {
        MembershipRegistration registration = SignUpRequestMapper.fromSignUpRequest(signUpRequestPayload);
        Member member = memberService.registerMember(registration);
        SignUpResponse response = MemberResponseMapper.fromMember(member, "Member registered (temporary - approval system coming)");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/members/{memberId}", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
            summary = "Obtenir les informations d'un membre",
            description = "Récupérer les détails d'un membre par son ID"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Membre trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/NotFoundError")
                    )
            )
    })
    public ResponseEntity<SignUpResponse> getMember(@PathVariable UUID memberId) {
        Member member = memberService.findById(memberId);
        SignUpResponse response = MemberResponseMapper.fromMember(member, "Membre trouvé");
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/members/{memberId}", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
            summary = "Modifier un membre",
            description = "Mettre à jour les informations d'un membre"
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Membre modifié avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Membre non trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/NotFoundError")
                    )
            )
    })
    public ResponseEntity<SignUpResponse> updateMember(@PathVariable UUID memberId,
                                                       @Valid @RequestBody UpdateMemberRequestPayload updateRequest) {
        MembershipUpdate businessRequest = updateRequestMapper.fromUpdateMemberRequest(memberId, updateRequest);
        Member updatedMember = memberService.updateMember(businessRequest);
        SignUpResponse response = MemberResponseMapper.fromMember(updatedMember, "Membre modifié avec succès");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/members", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "Lister tous les membres",
            description = "Récupère la liste de tous les membres avec pagination et filtrage optionnel"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Page de membres récupérée avec succès",
                    content = @Content(
                            mediaType = "application/vnd.ecclesiaflow.members.v1+json",
                            schema = @Schema(implementation = MemberPageResponse.class)
                    )
            )
    })
    public ResponseEntity<MemberPageResponse> getAllMembers(
            @Parameter(description = "Recherche par nom ou email", example = "jean")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Filtrer par statut de confirmation", example = "true")
            @RequestParam(required = false) Boolean confirmed,
            
            @SortDefault(sort = "firstName", direction = Sort.Direction.ASC)
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Member> memberPage = memberService.getAllMembers(pageable, search, confirmed);
        MemberPageResponse response = memberPageMapper.toPageResponse(memberPage);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/members/{email}/confirmation-status", produces = "application/vnd.ecclesiaflow.members.v1+json")
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

    @DeleteMapping(value = "/members/{memberId}", produces = "application/vnd.ecclesiaflow.members.v1+json")
    @Operation(
            summary = "Supprimer un membre",
            description = "Supprimer définitivement un membre"
    )
    @SecurityRequirement(name = "BearerAuth")
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
