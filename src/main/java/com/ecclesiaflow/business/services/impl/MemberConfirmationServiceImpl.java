package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.communication.CodeGenerator;
import com.ecclesiaflow.business.domain.communication.EmailService;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.web.security.JwtProcessor;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.web.exception.ExpiredConfirmationCodeException;
import com.ecclesiaflow.web.exception.InvalidConfirmationCodeException;
import com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
 *   <li>{@link JwtProcessor} - Génération de tokens temporaires</li>
 *   <li>{@link ConfirmationNotifier} - Envoi des codes de confirmation</li>
 *   <li>{@link CodeGenerator} - Génération de codes aléatoires</li>
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
 * @see JwtProcessor
 * @see ConfirmationNotifier
 * @see CodeGenerator
 */
@Service
@RequiredArgsConstructor
public class MemberConfirmationServiceImpl implements MemberConfirmationService {

    private final MemberRepository memberRepository;
    private final MemberConfirmationRepository confirmationRepository;
    private final JwtProcessor jwtProcessor;
    private final ConfirmationNotifier confirmationNotifier;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public MembershipConfirmationResult confirmMember(MembershipConfirmation membershipConfirmation) {
        UUID memberId = membershipConfirmation.getMemberId();
        String code = membershipConfirmation.getConfirmationCode();

        Member member = getMemberToConfirmOrThrow(memberId);

        MemberConfirmation confirmation = validateConfirmationCode(memberId, code);

        // Marquer le membre comme confirmé
        member.confirm();
        memberRepository.save(member);

        // Supprimer le code utilisé
        confirmationRepository.delete(confirmation);

        // Générer un token temporaire via le service dédié
        String temporaryToken = jwtProcessor.generateTemporaryToken(member.getEmail());

        return MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès")
                .temporaryToken(temporaryToken)
                .expiresInSeconds(900)
                .build();
    }

    private MemberConfirmation validateConfirmationCode(UUID memberId, String code) {
        MemberConfirmation confirmation = confirmationRepository.findByMemberIdAndCode(memberId, code)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Code de confirmation invalide"));

        if (confirmation.isExpired()) {
            throw new ExpiredConfirmationCodeException("Code de confirmation expiré");
        }
        return confirmation;
    }

    @Override
    @Transactional
    public void sendConfirmationCode(UUID memberId) {
        Member member = getMemberToConfirmOrThrow(memberId);

        // Supprimer l'ancien code s'il existe
        deleteExistingConfirmationCode(memberId);

        // Générer un nouveau code via le service spécialisé
        String newCode = codeGenerator.generateCode();

        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .code(newCode)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        confirmationRepository.save(confirmation);

        // Envoyer le code via le service de notification
        confirmationNotifier.sendCode(member.getEmail(), newCode, member.getFirstName());
    }

    // Méthodes utilitaires privées

    private Member getMemberToConfirmOrThrow(UUID memberId) throws MemberNotFoundException, MemberAlreadyConfirmedException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé"));
        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException("Compte déjà confirmé");
        }
        return member;
    }

    private void deleteExistingConfirmationCode(UUID memberId) {
        confirmationRepository.findByMemberId(memberId)
                .ifPresent(confirmationRepository::delete);
    }
}