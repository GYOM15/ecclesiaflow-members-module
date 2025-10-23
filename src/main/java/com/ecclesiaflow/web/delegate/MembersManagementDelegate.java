package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.mappers.SignUpRequestMapper;
import com.ecclesiaflow.web.mappers.UpdateRequestMapper;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Délégué pour la gestion des membres - Pattern Delegate avec OpenAPI Generator.
 * <p>
 * Ce délégué contient toute la logique métier pour les opérations de gestion des membres,
 * séparant ainsi les responsabilités entre le contrôleur (gestion HTTP) et la logique applicative.
 * </p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <pre>
 * MembersController (implémente MembersManagementApi)
 *    ↓ délègue à
 * MembersManagementDelegate ← Cette classe
 *    ↓ utilise
 * MemberService (logique métier)
 * </pre>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembersManagementDelegate {

    private final MemberService memberService;
    private final UpdateRequestMapper updateRequestMapper;
    private final OpenApiModelMapper openApiModelMapper;

    /**
     * Crée un nouveau membre dans le système.
     * 
     * @param signUpRequestPayload Données d'inscription (modèle OpenAPI)
     * @return Membre créé avec statut HTTP 201
     */
    public ResponseEntity<SignUpResponse> createMember(SignUpRequestPayload signUpRequestPayload) {
        // Transformation vers l'objet métier
        MembershipRegistration registration = SignUpRequestMapper.fromSignUpRequest(signUpRequestPayload);
        
        // Enregistrement via le service métier
        Member member = memberService.registerMember(registration);
        
        // Transformation vers le modèle OpenAPI
        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Member registered (temporary - approval system coming)");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère la liste de tous les membres avec pagination et filtrage.
     * 
     * @param page Numéro de page
     * @param size Taille de la page
     * @param search Terme de recherche
     * @param confirmed Filtrage par statut de confirmation
     * @param sort Critère de tri
     * @param direction Direction du tri
     * @return Page de membres
     */
    public ResponseEntity<MemberPageResponse> getAllMembers(
            Integer page, Integer size, String search, Boolean confirmed, String sort, String direction) {

        // Création du Pageable
        Pageable pageable = createPageable(page, size, sort, direction);
        
        // Récupération via le service métier
        Page<Member> memberPage = memberService.getAllMembers(pageable, search, confirmed);
        
        // Transformation vers le modèle OpenAPI
        MemberPageResponse response = openApiModelMapper.createMemberPageResponse(memberPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les détails d'un membre par son ID.
     * 
     * @param memberId Identifiant unique du membre
     * @return Détails du membre
     */
    public ResponseEntity<SignUpResponse> getMemberById(UUID memberId) {
        Member member = memberService.findById(memberId);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Membre trouvé");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour partiellement un membre.
     * 
     * @param memberId Identifiant du membre
     * @param updateMemberRequestPayload Données de mise à jour (modèle OpenAPI)
     * @return Membre mis à jour
     */
    public ResponseEntity<SignUpResponse> updateMemberPartially(UUID memberId, UpdateMemberRequestPayload updateMemberRequestPayload) {
        // Transformation vers l'objet métier
        MembershipUpdate businessRequest = updateRequestMapper.fromUpdateMemberRequest(memberId, updateMemberRequestPayload);
        
        // Mise à jour via le service métier
        Member updatedMember = memberService.updateMember(businessRequest);
        
        // Transformation vers le modèle OpenAPI
        SignUpResponse response = openApiModelMapper.createSignUpResponse(updatedMember, "Membre modifié avec succès");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime définitivement un membre.
     * 
     * @param memberId Identifiant du membre
     * @return Réponse vide avec statut 204
     */
    public ResponseEntity<Void> deleteMember(UUID memberId) {
        memberService.deleteMember(memberId);
        
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // Routes /me (membre authentifié)
    // ========================================

    /**
     * Récupère les informations du membre authentifié.
     * 
     * @return Informations du membre connecté
     */
    public ResponseEntity<SignUpResponse> getMyProfile() {
        // TODO: Extraire memberId du JWT via AuthenticatedUserContextProvider
        UUID authenticatedMemberId = UUID.randomUUID(); // Placeholder
        
        Member member = memberService.findById(authenticatedMemberId);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(member, "Profil récupéré");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour les informations du membre authentifié.
     * 
     * @param updateMemberRequestPayload Données de mise à jour
     * @return Membre mis à jour
     */
    public ResponseEntity<SignUpResponse> updateMyProfile(UpdateMemberRequestPayload updateMemberRequestPayload) {
        // TODO: Extraire memberId du JWT via AuthenticatedUserContextProvider
        UUID authenticatedMemberId = UUID.randomUUID(); // Placeholder
        
        MembershipUpdate businessRequest = updateRequestMapper.fromUpdateMemberRequest(authenticatedMemberId, updateMemberRequestPayload);
        Member updatedMember = memberService.updateMember(businessRequest);
        SignUpResponse response = openApiModelMapper.createSignUpResponse(updatedMember, "Profil mis à jour");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime le compte du membre authentifié.
     * 
     * @return Réponse vide avec statut 204
     */
    public ResponseEntity<Void> deleteMyAccount() {
        // TODO: Extraire memberId du JWT via AuthenticatedUserContextProvider
        UUID authenticatedMemberId = UUID.randomUUID(); // Placeholder
        
        memberService.deleteMember(authenticatedMemberId);
        
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // Méthodes utilitaires privées
    // ========================================

    /**
     * Crée un objet Pageable à partir des paramètres de pagination.
     */
    private Pageable createPageable(Integer page, Integer size, String sort, String direction) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sortField = sort != null ? sort : "firstName";
        String sortDirection = direction != null ? direction : "asc";
        
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        return PageRequest.of(pageNumber, pageSize, Sort.by(dir, sortField));
    }
}
