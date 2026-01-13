package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.InvalidEmailUpdateException;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implémentation complète du service de gestion des membres EcclesiaFlow.
 * <p>
 * Cette classe implémente l'interface {@link MemberService} et fournit toutes les opérations
 * CRUD pour la gestion des membres : inscription, consultation, mise à jour, suppression.
 * Gère également la génération automatique des tokens de confirmation lors de l'inscription.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Logique métier des membres</p>
 *
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Inscription de nouveaux membres avec validation d'unicité email</li>
 *   <li>Génération automatique de tokens de confirmation à l'inscription</li>
 *   <li>Opérations CRUD complètes sur les profils membres</li>
 *   <li>Validation du statut de confirmation des comptes</li>
 *   <li>Orchestration avec les services d'email pour les notifications</li>
 * </ul>
 *
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Persistance et requêtes sur les membres</li>
 *   <li>{@link MemberConfirmationRepository} - Gestion des tokens de confirmation</li>
 *   <li>{@link EmailClient} - Envoi des emails de confirmation</li>
 * </ul>
 *
 * <p><strong>Flux d'inscription typique :</strong></p>
 * <ol>
 *   <li>Vérification de l'unicité de l'email</li>
 *   <li>Création de l'entité Member avec statut non confirmé</li>
 *   <li>Persistance en base de données</li>
 *   <li>Génération d'un token de confirmation sécurisé (UUID)</li>
 *   <li>Envoi automatique de l'email avec lien de confirmation</li>
 * </ol>
 *
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, gestion d'erreurs robuste.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberService
 * @see MemberRepository
 * @see EmailClient
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    private final MemberConfirmationService confirmationService;

    @Override
    @Transactional
    public Member registerMember(MembershipRegistration registration) {
        if (isEmailAlreadyUsed(registration.email())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }
        Member member = createMemberFromRegistration(registration);
        Member savedMember = memberRepository.save(member);
        confirmationService.sendConfirmationLink(savedMember);
        
        return savedMember;
    }

    public boolean isEmailAlreadyUsed(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailConfirmed(String email) {
        return memberRepository.getByEmail(email)
                .map(Member::isConfirmed)
                .orElse(false);
    }

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
    public Member findByMemberId(UUID memberId) {
        return memberRepository.getByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé avec le memberId"));
    }

    @Override
    @Transactional
    public Member updateMember(MembershipUpdate update) {
        Member existing = findByMemberId(update.getMemberId());

        validateEmailUpdate(existing, update.getEmail());

        Member updatedMember = existing.withUpdatedFields(update);
        return memberRepository.save(updatedMember);
    }

    private void validateEmailUpdate(Member existing, String newEmail) {
        if (newEmail == null) {
            return;
        }
        if (existing.getEmail().equalsIgnoreCase(newEmail)) {
            throw new InvalidEmailUpdateException(
                    "Le nouvel email doit être différent de l'email actuel."
            );
        }
        if (isEmailAlreadyUsed(newEmail)) {
            throw new EmailAlreadyUsedException(
                    "Un compte avec cet email existe déjà."
            );
        }
    }



    @Override
    @Transactional
    public void deleteMember(UUID memberId) {
        Member member = findByMemberId(memberId);
        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Member> getAllMembers(Pageable pageable, String search, Boolean confirmed) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        return findMembersWithCriteria(pageable, normalizeSearch(search), confirmed);
    }

    // Methodes Utilitaies
    private Page<Member> findMembersWithCriteria(Pageable pageable, String normalizedSearch, Boolean confirmed) {
        if (normalizedSearch != null && confirmed != null) {
            return memberRepository.getMembersBySearchTermAndConfirmationStatus(normalizedSearch, confirmed, pageable);
        }
        if (normalizedSearch != null) {
            return memberRepository.getMembersBySearchTerm(normalizedSearch, pageable);
        }
        if (confirmed != null) {
            return memberRepository.getByConfirmedStatus(confirmed, pageable);
        }
        return memberRepository.getAll(pageable);
    }

    private String normalizeSearch(String search) {
        return (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    }
}
