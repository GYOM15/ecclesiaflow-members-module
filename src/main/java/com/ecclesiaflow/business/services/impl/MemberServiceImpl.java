package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.communication.CodeGenerator;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.business.domain.communication.EmailService;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
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
    private MemberConfirmationService confirmationService;

    @Override
    @Transactional
    public Member registerMember(MembershipRegistration registration) {
        if (isEmailAlreadyUsed(registration.email())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }
        Member member = createMemberFromRegistration(registration);
        Member savedMember = memberRepository.save(member);
        confirmationService.sendConfirmationCode(savedMember.getMemberId());
        
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
}
