package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.confirmation.ConfirmationTokenGenerator;
import com.ecclesiaflow.business.domain.emailchange.PendingEmailChange;
import com.ecclesiaflow.business.domain.emailchange.PendingEmailChangeRepository;
import com.ecclesiaflow.business.domain.events.EmailChangedEvent;
import com.ecclesiaflow.business.domain.events.PendingEmailChangeRequestedEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.business.domain.member.SocialProvider;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidEmailUpdateException;
import com.ecclesiaflow.business.exceptions.LocalCredentialsRequiredException;
import com.ecclesiaflow.business.services.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChangeServiceImplTest {

    @Mock private MemberService memberService;
    @Mock private MemberRepository memberRepository;
    @Mock private PendingEmailChangeRepository pendingEmailChangeRepository;
    @Mock private ConfirmationTokenGenerator tokenGenerator;
    @Mock private AuthClient authClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailChangeServiceImpl service;

    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final UUID TOKEN = UUID.randomUUID();

    private Member buildMember() {
        return Member.builder()
                .memberId(MEMBER_ID)
                .email(OLD_EMAIL)
                .firstName("Jean")
                .lastName("Dupont")
                .keycloakUserId("kc-123")
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private Member buildSsoMember(boolean hasLocalCredentials) {
        return buildMember().toBuilder()
                .socialProvider(SocialProvider.GOOGLE)
                .hasLocalCredentials(hasLocalCredentials)
                .build();
    }

    private PendingEmailChange buildPendingChange(boolean expired) {
        return PendingEmailChange.builder()
                .id(UUID.randomUUID())
                .memberId(MEMBER_ID)
                .newEmail(NEW_EMAIL)
                .token(TOKEN)
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(expired ? LocalDateTime.now().minusHours(1) : LocalDateTime.now().plusHours(23))
                .build();
    }

    // --- requestEmailChange ---

    @Test
    void requestEmailChange_happyPath_savesAndPublishesEvent() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildMember());
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(tokenGenerator.generateToken()).thenReturn(TOKEN);
        when(pendingEmailChangeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.requestEmailChange(MEMBER_ID, NEW_EMAIL);

        verify(pendingEmailChangeRepository).deleteByMemberId(MEMBER_ID);

        ArgumentCaptor<PendingEmailChange> captor = ArgumentCaptor.forClass(PendingEmailChange.class);
        verify(pendingEmailChangeRepository).save(captor.capture());
        PendingEmailChange saved = captor.getValue();
        assertThat(saved.getNewEmail()).isEqualTo(NEW_EMAIL);
        assertThat(saved.getToken()).isEqualTo(TOKEN);
        assertThat(saved.getMemberId()).isEqualTo(MEMBER_ID);

        ArgumentCaptor<PendingEmailChangeRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(PendingEmailChangeRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().newEmail()).isEqualTo(NEW_EMAIL);
        assertThat(eventCaptor.getValue().token()).isEqualTo(TOKEN);
    }

    @Test
    void requestEmailChange_sameEmail_throws() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildMember());

        assertThatThrownBy(() -> service.requestEmailChange(MEMBER_ID, OLD_EMAIL))
                .isInstanceOf(InvalidEmailUpdateException.class);

        verify(pendingEmailChangeRepository, never()).save(any());
    }

    @Test
    void requestEmailChange_emailAlreadyUsed_throws() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildMember());
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.requestEmailChange(MEMBER_ID, NEW_EMAIL))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void requestEmailChange_cancelsPreviousRequest() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildMember());
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(tokenGenerator.generateToken()).thenReturn(TOKEN);
        when(pendingEmailChangeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.requestEmailChange(MEMBER_ID, NEW_EMAIL);

        verify(pendingEmailChangeRepository).deleteByMemberId(MEMBER_ID);
    }

    @Test
    void requestEmailChange_ssoWithoutPassword_throws403() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildSsoMember(false));

        assertThatThrownBy(() -> service.requestEmailChange(MEMBER_ID, NEW_EMAIL))
                .isInstanceOf(LocalCredentialsRequiredException.class)
                .satisfies(ex -> assertThat(((LocalCredentialsRequiredException) ex).getProvider())
                        .isEqualTo(SocialProvider.GOOGLE));

        verify(pendingEmailChangeRepository, never()).save(any());
    }

    @Test
    void requestEmailChange_ssoWithPassword_succeeds() {
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(buildSsoMember(true));
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(tokenGenerator.generateToken()).thenReturn(TOKEN);
        when(pendingEmailChangeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.requestEmailChange(MEMBER_ID, NEW_EMAIL);

        verify(pendingEmailChangeRepository).save(any());
        verify(eventPublisher).publishEvent(any(PendingEmailChangeRequestedEvent.class));
    }

    // --- confirmEmailChange ---

    @Test
    void confirmEmailChange_happyPath_updatesEmailAndPublishesEvent() {
        PendingEmailChange pending = buildPendingChange(false);
        Member member = buildMember();

        when(pendingEmailChangeRepository.getByToken(TOKEN)).thenReturn(Optional.of(pending));
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(member);
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Member result = service.confirmEmailChange(TOKEN);

        assertThat(result.getEmail()).isEqualTo(NEW_EMAIL);
        verify(authClient).updateKeycloakUserEmail("kc-123", NEW_EMAIL);
        verify(pendingEmailChangeRepository).delete(pending);

        ArgumentCaptor<EmailChangedEvent> eventCaptor = ArgumentCaptor.forClass(EmailChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().oldEmail()).isEqualTo(OLD_EMAIL);
    }

    @Test
    void confirmEmailChange_invalidToken_throws() {
        when(pendingEmailChangeRepository.getByToken(TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmEmailChange(TOKEN))
                .isInstanceOf(InvalidConfirmationCodeException.class);
    }

    @Test
    void confirmEmailChange_expiredToken_throws() {
        when(pendingEmailChangeRepository.getByToken(TOKEN)).thenReturn(Optional.of(buildPendingChange(true)));

        assertThatThrownBy(() -> service.confirmEmailChange(TOKEN))
                .isInstanceOf(ExpiredConfirmationCodeException.class);
    }

    @Test
    void confirmEmailChange_emailTakenMeanwhile_throws() {
        when(pendingEmailChangeRepository.getByToken(TOKEN)).thenReturn(Optional.of(buildPendingChange(false)));
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.confirmEmailChange(TOKEN))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void confirmEmailChange_noKeycloakUserId_skipsKeycloakUpdate() {
        PendingEmailChange pending = buildPendingChange(false);
        Member memberNoKc = buildMember().toBuilder().keycloakUserId(null).build();

        when(pendingEmailChangeRepository.getByToken(TOKEN)).thenReturn(Optional.of(pending));
        when(memberRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(memberService.findByMemberId(MEMBER_ID)).thenReturn(memberNoKc);
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.confirmEmailChange(TOKEN);

        verify(authClient, never()).updateKeycloakUserEmail(any(), any());
    }
}
