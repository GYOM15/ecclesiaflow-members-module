package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.member.*;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.exceptions.SocialAccountAlreadyExistsException;
import com.ecclesiaflow.business.domain.events.MemberActivatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core member management service.
 *
 * <p>Handles registration (with email confirmation), social onboarding,
 * CRUD operations, and email uniqueness enforcement.</p>
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    private final MemberConfirmationService confirmationService;
    private final AuthClient authClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Member registerMember(MembershipRegistration registration) {
        if (isEmailAlreadyUsed(registration.email())) {
            throw new EmailAlreadyUsedException(
                    "An account with this email already exists.");
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
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Member getByKeycloakUserId(String keycloakUserId) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IllegalArgumentException("keycloakUserId must not be null or blank");
        }
        return memberRepository.getByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found for keycloakUserId: " + keycloakUserId));
    }

    @Override
    @Transactional
    public Member updateMember(MembershipUpdate update) {
        Member existing = findByMemberId(update.getMemberId());
        Member updatedMember = existing.withUpdatedFields(update);
        return memberRepository.save(updatedMember);
    }

    @Override
    @Transactional
    public void deactivateMember(UUID memberId) {
        Member member = findByMemberId(memberId);
        Member deactivated = member.toBuilder()
                .status(MemberStatus.DEACTIVATED)
                .deactivatedAt(LocalDateTime.now())
                .build();
        memberRepository.save(deactivated);
    }

    @Override
    @Transactional
    public void deleteMember(UUID memberId) {
        Member member = findByMemberId(memberId);
        if (member.getKeycloakUserId() != null) {
            authClient.deleteKeycloakUser(member.getKeycloakUserId());
        }
        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Member> getAllMembers(Pageable pageable, String search, com.ecclesiaflow.business.domain.member.MemberStatus status) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        return findMembersWithCriteria(pageable, normalizeSearch(search), status);
    }

    // --- Private helpers ---
    private Page<Member> findMembersWithCriteria(Pageable pageable, String normalizedSearch, com.ecclesiaflow.business.domain.member.MemberStatus status) {
        if (normalizedSearch != null && status != null) {
            return memberRepository.getMembersBySearchTermAndStatus(normalizedSearch, status, pageable);
        }
        if (normalizedSearch != null) {
            return memberRepository.getMembersBySearchTerm(normalizedSearch, pageable);
        }
        if (status != null) {
            return memberRepository.getByStatus(status, pageable);
        }
        return memberRepository.getAll(pageable);
    }

    @Override
    @Transactional
    public Member registerSocialMember(String keycloakUserId, SocialProvider socialProvider,
                                       MembershipRegistration registration) {
        if (memberRepository.existsByEmail(registration.email())) {
            throw new SocialAccountAlreadyExistsException(
                    "A member with this email already exists.");
        }
        if (memberRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new SocialAccountAlreadyExistsException(
                    "A member with this keycloakUserId already exists.");
        }

        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName(registration.firstName())
                .lastName(registration.lastName())
                .email(registration.email())
                .address(registration.address())
                .phoneNumber(registration.phoneNumber())
                .keycloakUserId(keycloakUserId)
                .socialProvider(socialProvider)
                .hasLocalCredentials(false)
                .status(MemberStatus.ACTIVE)
                .confirmedAt(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        eventPublisher.publishEvent(new MemberActivatedEvent(savedMember.getEmail(), savedMember.getFirstName()));
        return savedMember;
    }

    @Override
    @Transactional
    public Member reactivateMember(UUID memberId) {
        Member member = findByMemberId(memberId);
        if (member.getStatus() != MemberStatus.DEACTIVATED) {
            throw new IllegalStateException("Only DEACTIVATED accounts can be reactivated");
        }
        Member reactivated = member.toBuilder()
                .status(MemberStatus.ACTIVE)
                .deactivatedAt(null)
                .build();
        return memberRepository.save(reactivated);
    }

    private String normalizeSearch(String search) {
        return (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    }
}
