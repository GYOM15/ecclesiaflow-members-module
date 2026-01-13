package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.confirmation.ConfirmationTokenGenerator;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.domain.auth.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberConfirmationServiceImpl implements MemberConfirmationService {

    private final MemberRepository memberRepository;
    private final MemberConfirmationRepository confirmationRepository;
    private final AuthClient authClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ConfirmationTokenGenerator tokenGenerator;

    @Override
    @Transactional
    public MembershipConfirmationResult confirmMemberByToken(UUID token) {
        if (token == null) {
            throw new IllegalArgumentException("Le token ne peut pas être null");
        }
        MemberConfirmation confirmation = confirmationRepository.getByToken(token)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Token de confirmation invalide ou déjà utilisé"));

        if (confirmation.isExpired()) {
            throw new ExpiredConfirmationCodeException("Token de confirmation expiré");
        }
        Member member = getMemberOrThrow(confirmation.getMemberId());
        
        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException("Le compte est déjà confirmé");
        }
        Member confirmedMember = member.confirm();
        memberRepository.save(confirmedMember);

        confirmationRepository.delete(confirmation);

        String temporaryToken = authClient.retrievePostActivationToken(member.getEmail(), member.getMemberId());

        return MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès. Vous pouvez maintenant définir votre mot de passe.")
                .temporaryToken(temporaryToken)
                .expiresInSeconds(900)
                .build();
    }


    @Override
    @Transactional
    public void sendConfirmationLink(String email) {
        Member member = memberRepository.getByEmail(email).orElse(null);
        if (member == null) {
            return;
        }
        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException(
                "Votre compte est déjà confirmé. Vous pouvez vous connecter directement."
            );
        }
        generateAndSaveToken(member);
    }

    @Override
    @Transactional
    public void sendConfirmationLink(Member member) {
        generateAndSaveToken(member);
    }

    private void generateAndSaveToken(Member member) {
        deleteExistingConfirmationToken(member.getMemberId());
        UUID newToken = tokenGenerator.generateToken();
        var now = LocalDateTime.now();
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(member.getMemberId())
                .token(newToken)
                .createdAt(now)
                .expiresAt(now.plusHours(24))
                .build();
        confirmationRepository.save(confirmation);
        
        // Publier événement au lieu d'appeler directement le notifier
        // L'email sera envoyé APRÈS le commit via @TransactionalEventListener
        eventPublisher.publishEvent(
            new MemberRegisteredEvent(member.getEmail(), newToken, member.getFirstName())
        );
    }


    private Member getMemberOrThrow(UUID memberId) throws MemberNotFoundException, MemberAlreadyConfirmedException {
        return memberRepository.getByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé"));
    }

    private void deleteExistingConfirmationToken(UUID memberId) {
        confirmationRepository.getByMemberId(memberId)
                .ifPresent(confirmationRepository::delete);
    }
}