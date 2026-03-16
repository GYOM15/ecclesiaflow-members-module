package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.confirmation.ConfirmationTokenGenerator;
import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.business.domain.emailchange.PendingEmailChangeRepository;
import com.ecclesiaflow.business.domain.events.EmailChangedEvent;
import com.ecclesiaflow.business.domain.events.PendingEmailChangeRequestedEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidEmailUpdateException;
import com.ecclesiaflow.business.exceptions.LocalCredentialsRequiredException;
import com.ecclesiaflow.business.services.EmailChangeService;
import com.ecclesiaflow.business.services.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles the email change flow: request → confirmation email → verify token → update.
 * Keycloak email is updated before DB save so that a Keycloak failure triggers rollback.
 */
@Service
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PendingEmailChangeRepository pendingEmailChangeRepository;
    private final ConfirmationTokenGenerator tokenGenerator;
    private final AuthClient authClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void requestEmailChange(UUID memberId, String newEmail) {
        Member existing = memberService.findByMemberId(memberId);

        // SSO users must add a local password first
        if (existing.getSocialProvider() != null && !existing.isHasLocalCredentials()) {
            throw new LocalCredentialsRequiredException(existing.getSocialProvider());
        }

        if (existing.getEmail().equalsIgnoreCase(newEmail)) {
            throw new InvalidEmailUpdateException("New email is identical to the current email");
        }
        if (memberRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyUsedException("A member with this email already exists.");
        }

        // Cancel any previous pending request
        pendingEmailChangeRepository.deleteByMemberId(memberId);

        PendingEmailChange pendingChange = PendingEmailChange.builder()
                .memberId(memberId)
                .newEmail(newEmail)
                .token(tokenGenerator.generateToken())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        pendingEmailChangeRepository.save(pendingChange);

        eventPublisher.publishEvent(
                new PendingEmailChangeRequestedEvent(newEmail, pendingChange.getToken(), existing.getFirstName()));
    }

    @Override
    @Transactional
    public Member confirmEmailChange(UUID token) {
        PendingEmailChange pendingChange = pendingEmailChangeRepository.getByToken(token)
                .orElseThrow(() -> new InvalidConfirmationCodeException("Invalid or expired email change token"));

        if (pendingChange.isExpired()) {
            throw new ExpiredConfirmationCodeException("Email change token has expired");
        }
        if (memberRepository.existsByEmail(pendingChange.getNewEmail())) {
            throw new EmailAlreadyUsedException("Email is now taken by another account.");
        }

        Member member = memberService.findByMemberId(pendingChange.getMemberId());
        String oldEmail = member.getEmail();

        // Update Keycloak FIRST — rollback on failure
        if (member.getKeycloakUserId() != null) {
            authClient.updateKeycloakUserEmail(member.getKeycloakUserId(), pendingChange.getNewEmail());
        }

        Member updated = member.toBuilder()
                .email(pendingChange.getNewEmail())
                .updatedAt(LocalDateTime.now())
                .build();
        Member saved = memberRepository.save(updated);

        pendingEmailChangeRepository.delete(pendingChange);

        eventPublisher.publishEvent(new EmailChangedEvent(oldEmail, saved.getFirstName()));
        return saved;
    }
}
