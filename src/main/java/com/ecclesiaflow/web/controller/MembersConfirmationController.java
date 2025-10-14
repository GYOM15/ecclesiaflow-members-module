package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.web.api.MemberConfirmationApi;
import com.ecclesiaflow.web.delegate.MemberConfirmationDelegate;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.ResendConfirmationLink200Response;
import com.ecclesiaflow.web.model.ResendConfirmationLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion du processus de confirmation des membres - Pattern Delegate avec OpenAPI Generator.
 * <p>
 * Ce contrôleur implémente l'interface générée par OpenAPI Generator et utilise le pattern Delegate
 * pour séparer les responsabilités entre la gestion HTTP (contrôleur) et la logique métier (délégué).
 * Respecte le principe d'inversion de dépendance (DIP) de SOLID.
 * </p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <pre>
 * OpenAPI Spec (members.yaml)
 *    ↓ génère
 * MemberConfirmationApi
 *    ↓ implémentée par
 * MembersConfirmationController ← Cette classe
 *    ↓ délègue à
 * MemberConfirmationDelegate
 *    ↓ utilise
 * MemberConfirmationService
 * </pre>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Implémentation de l'interface MemberConfirmationApi générée</li>
 *   <li>Délégation de la logique métier au délégué approprié</li>
 *   <li>Respect strict des contrats définis dans la spécification OpenAPI</li>
 *   <li>Respect des bonnes pratiques REST</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0 (Refactorisé avec pattern Delegate)
 */
@RestController
@RequiredArgsConstructor
public class MembersConfirmationController implements MemberConfirmationApi {

    private final MemberConfirmationDelegate memberConfirmationDelegate;

    /**
     * Confirmer le compte d'un membre via token reçu par email.
     * <p>
     * Confirme automatiquement le compte d'un membre en utilisant le token unique reçu par email.
     * Génère un token temporaire d'authentification pour définir le mot de passe.
     * </p>
     * 
     * @param token Token de confirmation UUID reçu par email
     * @return {@link ResponseEntity} avec token temporaire et informations de redirection
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre associé au token n'existe pas
     * @throws com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException si le token est invalide
     * @throws com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException si le token est expiré
     * @throws com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException si le compte est déjà confirmé
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MemberConfirmationDelegate}
     * @see MemberConfirmationDelegate#confirmMemberByToken(UUID)
     */
    @Override
    public ResponseEntity<ConfirmationResponse> _confirmMemberByToken(UUID token) {
        return memberConfirmationDelegate.confirmMemberByToken(token);
    }

    /**
     * Renvoyer le lien de confirmation via email.
     * <p>
     * Génère un nouveau token de confirmation et renvoie un lien par email.
     * Invalide l'ancien token si existant. L'utilisateur fournit simplement son email.
     * </p>
     * 
     * <p><strong>Sécurité:</strong> Approche hybride anti-énumération :
     * <ul>
     *   <li>Retourne 200 OK même si l'email n'existe pas</li>
     *   <li>Retourne 409 Conflict si le compte est déjà confirmé</li>
     * </ul>
     * </p>
     * 
     * @param resendConfirmationLinkRequest Requête contenant l'adresse email du membre
     * @return {@link ResponseEntity} avec message de confirmation et durée de validité
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException si le compte est déjà confirmé
     * 
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MemberConfirmationDelegate}
     * @see MemberConfirmationDelegate#resendConfirmationLink(String)
     */
    @Override
    public ResponseEntity<ResendConfirmationLink200Response> _resendConfirmationLink(ResendConfirmationLinkRequest resendConfirmationLinkRequest) {
        return memberConfirmationDelegate.resendConfirmationLink(resendConfirmationLinkRequest.getEmail());
    }

}