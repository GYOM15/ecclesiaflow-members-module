package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.services.EmailService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.MemberConfirmation;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.io.repository.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.web.exception.ExpiredConfirmationCodeException;
import com.ecclesiaflow.web.exception.InvalidConfirmationCodeException;
import com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Implémentation complète du service de confirmation des membres EcclesiaFlow.
 * <p>
 * Cette classe implémente l'interface {@link MemberConfirmationService} et orchestre
 * le processus complet de confirmation des comptes : validation des codes,
 * mise à jour du statut de confirmation, génération de tokens temporaires,
 * et intégration avec le module d'authentification.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Logique de confirmation</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Validation des codes de confirmation avec vérification d'expiration</li>
 *   <li>Mise à jour du statut de confirmation des membres</li>
 *   <li>Génération et renvoi de nouveaux codes de confirmation</li>
 *   <li>Intégration avec le module d'authentification pour les tokens temporaires</li>
 *   <li>Nettoyage automatique des codes utilisés ou expirés</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Gestion des membres et statuts de confirmation</li>
 *   <li>{@link MemberConfirmationRepository} - Persistance des codes de confirmation</li>
 *   <li>{@link AuthModuleService} - Génération de tokens temporaires</li>
 *   <li>{@link EmailService} - Envoi des codes par email</li>
 * </ul>
 * 
 * <p><strong>Flux de confirmation typique :</strong></p>
 * <ol>
 *   <li>Validation de l'existence du membre</li>
 *   <li>Vérification que le compte n'est pas déjà confirmé</li>
 *   <li>Validation du code et vérification de l'expiration</li>
 *   <li>Mise à jour du statut de confirmation du membre</li>
 *   <li>Suppression du code utilisé</li>
 *   <li>Génération d'un token temporaire pour définir le mot de passe</li>
 * </ol>
 * 
 * <p><strong>Sécurité :</strong> Codes à usage unique, expiration courte (5 min pour renvoi),
 * suppression automatique après utilisation.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, gestion d'erreurs complète.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberConfirmationService
 * @see AuthModuleService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberConfirmationServiceImpl implements MemberConfirmationService {

    private final MemberRepository memberRepository;
    private final MemberConfirmationRepository confirmationRepository;
    private final AuthModuleService authModuleService; // Service pour appeler le module d'auth
    private final EmailService emailService;

    @Override
    @Transactional
    public MembershipConfirmationResult confirmMember(MembershipConfirmation confirmationRequest) {
        UUID memberId = confirmationRequest.getMemberId();
        String code = confirmationRequest.getConfirmationCode();
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé"));

        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException("Compte déjà confirmé");
        }

        MemberConfirmation confirmation = confirmationRepository.findByMemberIdAndCode(memberId, code)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Code de confirmation invalide"));

        if (confirmation.isExpired()) {
            throw new ExpiredConfirmationCodeException("Code de confirmation expiré");
        }

        // Marquer comme confirmé
        member.setConfirmed(true);
        member.setConfirmedAt(LocalDateTime.now());
        memberRepository.save(member);

        // Supprimer le code utilisé
        confirmationRepository.delete(confirmation);

        // Générer un token temporaire via le module d'authentification
        String temporaryToken = authModuleService.generateTemporaryToken(member.getEmail());

        return MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès")
                .temporaryToken(temporaryToken)
                .expiresInSeconds(900) // 15 minutes
                .build();
    }

    @Override
    @Transactional
    public void sendConfirmationCode(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé"));

        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException("Compte déjà confirmé");
        }

        // Supprimer l'ancien code s'il existe
        confirmationRepository.findByMemberId(memberId)
                .ifPresent(confirmationRepository::delete);

        // Générer un nouveau code
        String newCode = generateConfirmationCode();
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .code(newCode)
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 5 minutes seulement !
                .build();

        confirmationRepository.save(confirmation);

        // Envoyer le nouveau code par email
        emailService.sendConfirmationCode(member.getEmail(), newCode, member.getFirstName());
    }

    /**
     * Génère un code de confirmation à 6 chiffres aléatoire.
     * <p>
     * Utilise {@link Random} pour générer un entier entre 0 et 999999,
     * puis le formate en chaîne de 6 caractères avec zéros de tête si nécessaire.
     * Identique à la méthode dans {@link MemberServiceImpl} pour cohérence.
     * </p>
     * 
     * @return un code de confirmation de 6 chiffres (ex: "012345", "987654")
     * 
     * @implNote Utilise String.format("%06d") pour garantir 6 caractères avec zéros de tête.
     */
    private String generateConfirmationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
