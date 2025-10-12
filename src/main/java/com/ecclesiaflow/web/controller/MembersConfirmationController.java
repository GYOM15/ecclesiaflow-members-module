package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.web.api.MemberConfirmationApi;
import com.ecclesiaflow.web.delegate.MemberConfirmationDelegate;
import com.ecclesiaflow.web.model.ConfirmationRequestPayload;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
 *   <li>Gestion des annotations spécifiques (ex: @RateLimiter)</li>
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
     * Confirmer le compte d'un membre (mise à jour d'état).
     * <p>
     * Met à jour l'état de confirmation d'un membre avec le code reçu par email.
     * Génère un token temporaire et l'URL de redirection pour définir le mot de passe.
     * </p>
     * 
     * @param memberId Identifiant unique du membre
     * @param confirmationRequestPayload Payload contenant le code de confirmation
     * @return {@link ResponseEntity} avec token temporaire et URL de redirection
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre n'existe pas
     * @throws com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException si le code est invalide
     * 
     * @implNote Rate limiting activé pour prévenir les attaques par force brute
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MemberConfirmationDelegate}
     * @see MemberConfirmationDelegate#confirmMember(UUID, ConfirmationRequestPayload)
     */
    @RateLimiter(name = "confirmation-attempts")
    @Override
    public ResponseEntity<ConfirmationResponse> _confirmMember(
            UUID memberId, ConfirmationRequestPayload confirmationRequestPayload) {
        return memberConfirmationDelegate.confirmMember(memberId, confirmationRequestPayload);
    }

    /**
     * Renvoyer le code de confirmation.
     * <p>
     * Renvoyer un nouveau code de confirmation par email.
     * </p>
     * 
     * @param memberId Identifiant unique du membre
     * @return {@link ResponseEntity} vide avec statut 200
     * 
     * @throws com.ecclesiaflow.business.exceptions.MemberNotFoundException si le membre n'existe pas
     * @throws com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException si le compte est déjà confirmé
     * 
     * @implNote Rate limiting activé pour prévenir l'abus d'envoi d'emails
     * @implNote <strong>Implémentation :</strong> Délègue au {@link MemberConfirmationDelegate}
     * @see MemberConfirmationDelegate#resendConfirmationCode(UUID)
     */
    @RateLimiter(name = "confirmation-resend")
    @Override
    public ResponseEntity<Void> _resendConfirmationCode(UUID memberId) {
        return memberConfirmationDelegate.resendConfirmationCode(memberId);
    }

}