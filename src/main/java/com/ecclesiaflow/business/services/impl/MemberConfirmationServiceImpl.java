package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.communication.CodeGenerator;
import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MemberConfirmationRepository;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.exception.ExpiredConfirmationCodeException;
import com.ecclesiaflow.web.exception.InvalidConfirmationCodeException;
import com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import com.ecclesiaflow.business.domain.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberConfirmationServiceImpl implements MemberConfirmationService {

    private final MemberRepository memberRepository;
    private final MemberConfirmationRepository confirmationRepository;
    private final TokenGenerator tokenGenerator;
    private final ConfirmationNotifier confirmationNotifier;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public MembershipConfirmationResult confirmMember(MembershipConfirmation membershipConfirmation) {
        UUID memberId = membershipConfirmation.getMemberId();
        String code = membershipConfirmation.getConfirmationCode();

        Member member = getMemberOrThrow(memberId);
        MemberConfirmation confirmation = validateConfirmationCode(memberId, code);

        member.confirm();
        memberRepository.save(member);

        confirmationRepository.delete(confirmation);

        String temporaryToken = tokenGenerator.generateTemporaryToken(member.getEmail());

        return MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès")
                .temporaryToken(temporaryToken)
                .expiresInSeconds(900)
                .build();
    }


    @Override
    @Transactional
    public void sendConfirmationCode(UUID memberId) {
        Member member = getMemberOrThrow(memberId);
        if (member.isConfirmed()) {
            throw new MemberAlreadyConfirmedException("Le compte est déjà confirmé. Aucun nouveau code n'est requis.");
        }
        generateAndSaveCode(member);
    }

    @Override
    @Transactional
    public void sendConfirmationCode(Member member) {
        generateAndSaveCode(member);
    }

    private void generateAndSaveCode(Member member) {
        deleteExistingConfirmationCode(member.getId());
        String newCode = codeGenerator.generateCode();
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(member.getId())
                .code(newCode)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        confirmationRepository.save(confirmation);
        confirmationNotifier.sendCode(member.getEmail(), newCode, member.getFirstName());
    }

    private MemberConfirmation validateConfirmationCode(UUID memberId, String code) {
        MemberConfirmation confirmation = confirmationRepository.findByMemberIdAndCode(memberId, code)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Code de confirmation invalide"));
        if (confirmation.isExpired()) {
            throw new ExpiredConfirmationCodeException("Code de confirmation expiré");
        }
        return confirmation;
    }

    private Member getMemberOrThrow(UUID memberId) throws MemberNotFoundException, MemberAlreadyConfirmedException {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé"));
    }

    private void deleteExistingConfirmationCode(UUID memberId) {
        confirmationRepository.findByMemberId(memberId)
                .ifPresent(confirmationRepository::delete);
    }
}