package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.business.domain.Member;
import com.ecclesiaflow.business.services.repositories.MemberRepository;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.business.services.EmailService;
import com.ecclesiaflow.io.entities.MemberConfirmation;
import com.ecclesiaflow.io.repository.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.MembershipUpdate;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation complète du service de gestion des membres EcclesiaFlow.
 * <p>
 * Cette classe implémente l'interface {@link MemberService} et fournit toutes les opérations
 * CRUD pour la gestion des membres : inscription, consultation, mise à jour, suppression.
 * Gère également la génération automatique des codes de confirmation lors de l'inscription.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Logique métier des membres</p>
 *
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Inscription de nouveaux membres avec validation d'unicité email</li>
 *   <li>Génération automatique de codes de confirmation à l'inscription</li>
 *   <li>Opérations CRUD complètes sur les profils membres</li>
 *   <li>Validation du statut de confirmation des comptes</li>
 *   <li>Orchestration avec les services d'email pour les notifications</li>
 * </ul>
 *
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Persistance et requêtes sur les membres</li>
 *   <li>{@link MemberConfirmationRepository} - Gestion des codes de confirmation</li>
 *   <li>{@link EmailService} - Envoi des emails de confirmation</li>
 * </ul>
 *
 * <p><strong>Flux d'inscription typique :</strong></p>
 * <ol>
 *   <li>Vérification de l'unicité de l'email</li>
 *   <li>Création de l'entité Member avec statut non confirmé</li>
 *   <li>Persistance en base de données</li>
 *   <li>Génération d'un code de confirmation à 6 chiffres</li>
 *   <li>Envoi automatique de l'email de confirmation</li>
 * </ol>
 *
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, gestion d'erreurs robuste.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberService
 * @see MemberRepository
 * @see EmailService
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    private final MemberConfirmationRepository confirmationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Member registerMember(MembershipRegistration registration) {
        if (isEmailAlreadyUsed(registration.email())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }
        Member member = createMemberFromRegistration(registration);
        Member savedMember = memberRepository.save(member);
        generateAndSendConfirmationCode(savedMember);
        
        return savedMember;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAlreadyUsed(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailConfirmed(String email) {
        return memberRepository.findByEmail(email)
                .map(Member::isConfirmed)
                .orElse(false);
    }

    /**
     * Crée une entité Member à partir des données d'inscription.
     * <p>
     * Cette méthode transforme un objet métier {@link MembershipRegistration}
     * en entité JPA {@link Member} prête pour la persistance. Initialise
     * les valeurs par défaut : rôle MEMBER, statut non confirmé, et génère
     * un UUID unique pour l'intégration avec le module d'authentification.
     * </p>
     *
     * @param registration les données d'inscription validées, non null
     * @return une entité Member initialisée prête pour la persistance
     *
     * @implNote Génère automatiquement un memberId UUID pour l'intégration inter-modules.
     */
    private Member createMemberFromRegistration(MembershipRegistration registration) {
        return Member.builder().
                memberId(UUID.randomUUID()).
                firstName(registration.firstName()).
                lastName(registration.lastName()).
                email(registration.email()).
                address(registration.address()).
                build();
    }

    @Override
    @Transactional(readOnly = true)
    public Member findById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé avec l'ID"));
    }

    @Override
    @Transactional
    public Member updateMember(MembershipUpdate update) {
        Member member = findById(update.getMemberId());
        Member updatedMember = member.withUpdatedFields(update);
        return memberRepository.save(updatedMember);
    }

    @Override
    @Transactional
    public void deleteMember(UUID id) {
        Member member = findById(id);
        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Génère et envoie automatiquement un code de confirmation par email.
     * <p>
     * Cette méthode orchestre le processus complet de génération du code :
     * suppression de l'ancien code s'il existe, génération d'un nouveau code
     * à 6 chiffres, persistance avec expiration 24h, et envoi par email.
     * </p>
     *
     * <p><strong>Gestion d'erreurs :</strong> Les erreurs d'envoi d'email ne font pas
     * échouer l'inscription. L'utilisateur peut demander un renvoi ultérieurement.</p>
     *
     * @param member le membre nouvellement inscrit, non null
     *
     * @implNote Opération non-bloquante : l'échec d'envoi d'email n'affecte pas l'inscription.
     */
    private void generateAndSendConfirmationCode(Member member) {
        try {

            // Supprimer l'ancien code s'il existe
            confirmationRepository.findByMemberId(member.getId())
                    .ifPresent(confirmationRepository::delete);

            // Générer un nouveau code à 6 chiffres
            String confirmationCode = generateConfirmationCode();

            // Créer l'entité de confirmation
            MemberConfirmation confirmation = MemberConfirmation.builder()
                    .memberId(member.getId())
                    .code(confirmationCode)
                    .expiresAt(LocalDateTime.now().plusHours(24)) // Expire dans 24h
                    .build();

            confirmationRepository.save(confirmation);

            // Envoyer le code par email
            emailService.sendConfirmationCode(member.getEmail(), confirmationCode, member.getFirstName());

        } catch (Exception e) {
            // Ne pas faire échouer l'inscription si l'email ne peut pas être envoyé
            // L'utilisateur pourra demander un renvoi du code plus tard
        }
    }

    /**
     * Génère un code de confirmation à 6 chiffres aléatoire.
     * <p>
     * Utilise {@link Random} pour générer un entier entre 0 et 999999,
     * puis le formate en chaîne de 6 caractères avec zéros de tête si nécessaire.
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
