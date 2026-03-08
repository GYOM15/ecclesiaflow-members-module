package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.web.api.MembersManagementApi;
import com.ecclesiaflow.web.api.MembersTemporaryApi;
import com.ecclesiaflow.web.api.SocialOnboardingApi;
import com.ecclesiaflow.web.delegate.MembersManagementDelegate;
import com.ecclesiaflow.web.delegate.MembersTemporaryDelegate;
import com.ecclesiaflow.web.delegate.SocialOnboardingDelegate;
import com.ecclesiaflow.web.model.MemberConfirmationStatusResponse;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpRequestPayload;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.SocialOnboardingRequest;
import com.ecclesiaflow.web.model.SocialOnboardingResponse;
import com.ecclesiaflow.web.model.UpdateMemberRequestPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des membres EcclesiaFlow - Pattern Delegate avec OpenAPI Generator.
 * <p>
 * Ce contrôleur implémente les interfaces générées par OpenAPI Generator et utilise le pattern Delegate
 * pour séparer les responsabilités entre la gestion HTTP (contrôleur) et la logique métier (délégués).
 * Respecte le principe d'inversion de dépendance (DIP) de SOLID.
 * </p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <pre>
 * OpenAPI Spec (members.yaml)
 *    ↓ génère
 * Interfaces (MembersManagementApi, MembersTemporaryApi)
 *    ↓ implémentées par
 * MembersController ← Cette classe
 *    ↓ délègue à
 * Delegates (MembersManagementDelegate, MembersTemporaryDelegate)
 *    ↓ utilisent
 * Service Layer (MemberService)
 * </pre>
 * 
 * <p><strong>Interfaces implémentées :</strong></p>
 * <ul>
 *   <li>{@link MembersManagementApi} - Gestion CRUD des membres</li>
 *   <li>{@link MembersTemporaryApi} - Endpoints temporaires</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0 (Refactorisé avec pattern Delegate)
 */
@RestController
@RequiredArgsConstructor
public class MembersController implements MembersManagementApi, MembersTemporaryApi, SocialOnboardingApi {

    private final MembersManagementDelegate membersManagementDelegate;
    private final MembersTemporaryDelegate membersTemporaryDelegate;
    private final SocialOnboardingDelegate socialOnboardingDelegate;

    // ========================================
    // SocialOnboardingApi
    // ========================================

    @Override
    public ResponseEntity<SocialOnboardingResponse> _membersSocialOnboarding(
            SocialOnboardingRequest socialOnboardingRequest) {
        return socialOnboardingDelegate.socialOnboarding(socialOnboardingRequest);
    }

    // ========================================
    // MembersTemporaryApi
    // ========================================

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
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersTemporaryDelegate}
     * @see MembersTemporaryDelegate#sayHello()
     */
    @Override
    public ResponseEntity<String> _membersSayHello() {
        return membersTemporaryDelegate.sayHello();
    }

    /**
     * Vérifie le statut de confirmation d'un membre par son email.
     * <p>
     * Endpoint interne pour que le module d'auth vérifie si un membre est confirmé.
     * </p>
     * 
     * @param email Adresse email du membre à vérifier
     * @return {@link ResponseEntity} avec objet contenant le statut de confirmation
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersTemporaryDelegate}
     * @see MembersTemporaryDelegate#getMemberConfirmationStatus(String)
     */
    @Override
public ResponseEntity<MemberConfirmationStatusResponse> _membersGetConfirmationStatus(String email) {
        return membersTemporaryDelegate.getMemberConfirmationStatus(email);
    }

    // ========================================
    // Implémentation de MembersManagementApi
    // ========================================

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
     * 
     * @throws IllegalArgumentException si l'email existe déjà
     * 
     * @implNote Utilise le pattern Mapper pour la transformation DTO → Objet métier → DTO.
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersManagementDelegate}
     * @see MembersManagementDelegate#createMember(SignUpRequestPayload)
     */
    @Override
    public ResponseEntity<SignUpResponse> _membersCreate(SignUpRequestPayload signUpRequestPayload) {
        return membersManagementDelegate.createMember(signUpRequestPayload);
    }

    /**
     * Lister tous les membres.
     * <p>
     * Récupère la liste de tous les membres avec pagination et filtrage optionnel.
     * </p>
     * 
     * @param page Numéro de page
     * @param size Taille de la page
     * @param search Terme de recherche (nom ou email)
     * @param status Filtrer par statut de confirmation
     * @param sort Champ de tri
     * @param direction Direction du tri (asc/desc)
     * @return {@link ResponseEntity} avec la page de membres
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersManagementDelegate}
     * @see MembersManagementDelegate#getAllMembers(Integer, Integer, String, String, String, String)
     */
    @Override
    public ResponseEntity<MemberPageResponse> _membersGetAll(
            Integer page, Integer size, @Nullable String search, 
            @Nullable String status, String sort, String direction) {
        return membersManagementDelegate.getAllMembers(page, size, search, status, sort, direction);
    }

    /**
     * Obtenir les informations d'un membre.
     * <p>
     * Récupérer les détails d'un membre par son ID.
     * </p>
     * 
     * @param memberId Identifiant unique du membre
     * @return {@link ResponseEntity} avec les détails du membre
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre n'existe pas
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersManagementDelegate}
     * @see MembersManagementDelegate#getMemberById(UUID)
     */
    @Override
    public ResponseEntity<SignUpResponse> _membersGetById(UUID memberId) {
        return membersManagementDelegate.getMemberById(memberId);
    }

    /**
     * Met à jour partiellement un membre.
     * <p>
     * Permet de mettre à jour certaines informations d'un membre existant.
     * </p>
     * 
     * @param memberId Identifiant unique du membre à mettre à jour
     * @param updateMemberRequestPayload les informations à mettre à jour
     * @return {@link ResponseEntity} avec les informations mises à jour
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre n'existe pas
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersManagementDelegate}
     * @see MembersManagementDelegate#updateMemberPartially(UUID, UpdateMemberRequestPayload)
     */
    @Override
    public ResponseEntity<SignUpResponse> _membersUpdatePartially(UUID memberId, UpdateMemberRequestPayload updateMemberRequestPayload) {
        return membersManagementDelegate.updateMemberPartially(memberId, updateMemberRequestPayload);
    }

    /**
     * Supprimer définitivement un membre.
     * <p>
     * <strong>⚠️ ATTENTION :</strong> Cette opération est irréversible !
     * </p>
     * 
     * @param memberId Identifiant unique du membre à supprimer
     * @return {@link ResponseEntity} vide avec statut 204 (No Content)
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre n'existe pas
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MembersManagementDelegate}
     * @see MembersManagementDelegate#deleteMember(UUID)
     */
    @Override
    public ResponseEntity<Void> _membersDelete(UUID memberId) {
        return membersManagementDelegate.deleteMember(memberId);
    }

    // ========================================
    // Implémentation des routes /me
    // ========================================

    /**
     * Récupère les informations du membre authentifié.
     * 
     * @return {@link ResponseEntity} avec les informations du membre connecté
     */
    @Override
    public ResponseEntity<SignUpResponse> _membersGetMyProfile() {
        return membersManagementDelegate.getMyProfile();
    }

    /**
     * Met à jour les informations du membre authentifié.
     * 
     * @param updateMemberRequestPayload les informations à mettre à jour
     * @return {@link ResponseEntity} avec les informations mises à jour
     */
    @Override
    public ResponseEntity<SignUpResponse> _membersUpdateMyProfile(UpdateMemberRequestPayload updateMemberRequestPayload) {
        return membersManagementDelegate.updateMyProfile(updateMemberRequestPayload);
    }

    /**
     * Supprime le compte du membre authentifié.
     * 
     * @return {@link ResponseEntity} vide avec statut 204 (No Content)
     */
    @Override
    public ResponseEntity<Void> _membersDeleteMyAccount() {
        return membersManagementDelegate.deleteMyAccount();
    }

}
