package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.services.AuthModuleService;
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
                .expiresInSeconds(3600) // 1 heure
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

    private String generateConfirmationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
