package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.Role;
import com.ecclesiaflow.io.repository.MemberRepository;
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
import java.util.Random;
import java.util.UUID;

/**
 * Implémentation du service d'enregistrement de nouveaux membres EcclesiaFlow.
 * <p>
 * Cette classe gère exclusivement l'inscription de nouveaux membres dans le système :
 * validation de l'unicité de l'email, encodage sécurisé du mot de passe et persistance
 * en base de données. Respecte le principe de responsabilité unique.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service de domaine - Gestion des inscriptions</p>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Persistance et vérification d'unicité</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Inscription de nouveaux membres via formulaire web</li>
 *   <li>Validation de l'unicité des emails avant inscription</li>
 *   <li>Création automatique de comptes avec rôle USER par défaut</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (stateless), transactionnel, validation d'unicité.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
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
     * Crée une entité Member à partir des données d'enregistrement
     */
    private Member createMemberFromRegistration(MembershipRegistration registration) {
        Member member = new Member();
        member.setEmail(registration.email());
        member.setFirstName(registration.firstName());
        member.setLastName(registration.lastName());
        member.setAddress(registration.address());
        member.setRole(Role.MEMBER);
        member.setMemberId(java.util.UUID.randomUUID()); // Générer un ID unique pour le module d'auth
        member.setConfirmed(false); // Par défaut non confirmé
        return member;
    }

    @Override
    @Transactional(readOnly = true)
    public Member findById(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé avec l'ID"));
    }

    @Override
    @Transactional
    public Member updateMember(MembershipUpdate updateRequest) {
        Member member = findById(updateRequest.getMemberId());

        if (updateRequest.getFirstName() != null) {
            member.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            member.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getAddress() != null) {
            member.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getEmail() != null) {
            member.setEmail(updateRequest.getEmail());
        }

        return memberRepository.save(member);
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
     * Génère et envoie automatiquement un code de confirmation par email
     * lors de l'inscription d'un nouveau membre.
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
     * Génère un code de confirmation à 6 chiffres aléatoire
     */
    private String generateConfirmationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
