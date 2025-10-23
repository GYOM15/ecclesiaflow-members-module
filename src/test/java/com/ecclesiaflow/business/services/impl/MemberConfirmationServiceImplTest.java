package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.confirmation.*;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.token.AuthenticationService;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberConfirmationServiceImplTest {

    private MemberRepository memberRepository;
    private MemberConfirmationRepository confirmationRepository;
    private AuthenticationService authenticationService;
    private ApplicationEventPublisher eventPublisher;
    private ConfirmationTokenGenerator tokenGenerator;

    private MemberConfirmationServiceImpl service;

    private UUID memberId;
    private UUID token;
    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        confirmationRepository = mock(MemberConfirmationRepository.class);
        authenticationService = mock(AuthenticationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        tokenGenerator = mock(ConfirmationTokenGenerator.class);

        service = new MemberConfirmationServiceImpl(
                memberRepository, confirmationRepository, authenticationService, eventPublisher, tokenGenerator);

        memberId = UUID.randomUUID();
        token = UUID.randomUUID();
        member = Member.builder()
                .id(memberId)
                .memberId(memberId)
                .firstName("John")
                .email("john@test.com")
                .build();
    }

    @Test
    void confirmMemberByToken_ShouldSucceed_WhenTokenValid() {
        // given
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(confirmationRepository.getByToken(token)).thenReturn(Optional.of(confirmation));
        when(memberRepository.getByMemberId(memberId)).thenReturn(Optional.of(member));
        when(authenticationService.retrievePostActivationToken(member.getEmail(), member.getMemberId())).thenReturn("TEMP_TOKEN");

        // when
        MembershipConfirmationResult result = service.confirmMemberByToken(token);

        // then
        assertNotNull(result);
        assertEquals("TEMP_TOKEN", result.getTemporaryToken());
        assertEquals("Compte confirmé avec succès. Vous pouvez maintenant définir votre mot de passe.", result.getMessage());
        assertEquals(900, result.getExpiresInSeconds());
        
        verify(confirmationRepository).getByToken(token);
        verify(memberRepository).getByMemberId(memberId);
        verify(memberRepository).save(argThat(savedMember -> 
            savedMember.isConfirmed() && 
            savedMember.getMemberId().equals(member.getMemberId())
        ));
        verify(confirmationRepository).delete(confirmation);
        verify(authenticationService).retrievePostActivationToken(member.getEmail(), member.getMemberId());
    }

    @Test
    void confirmMemberByToken_ShouldThrow_WhenTokenIsNull() {
        // when/then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.confirmMemberByToken(null));
        
        assertEquals("Le token ne peut pas être null", exception.getMessage());
        
        // Verify no repository interactions
        verify(confirmationRepository, never()).getByToken(any());
        verify(memberRepository, never()).getByMemberId(any());
    }

    @Test
    void confirmMemberByToken_ShouldThrow_WhenTokenInvalid() {
        // given
        UUID invalidToken = UUID.randomUUID();
        when(confirmationRepository.getByToken(invalidToken)).thenReturn(Optional.empty());

        // when/then
        InvalidConfirmationCodeException exception = assertThrows(InvalidConfirmationCodeException.class,
                () -> service.confirmMemberByToken(invalidToken));
        
        assertEquals("Token de confirmation invalide ou déjà utilisé", exception.getMessage());
        verify(confirmationRepository).getByToken(invalidToken);
        verify(memberRepository, never()).getByMemberId(any());
    }

    @Test
    void confirmMemberByToken_ShouldThrow_WhenTokenExpired() {
        // given
        MemberConfirmation expired = MemberConfirmation.builder()
                .memberId(memberId)
                .token(token)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(confirmationRepository.getByToken(token)).thenReturn(Optional.of(expired));

        // when/then
        ExpiredConfirmationCodeException exception = assertThrows(ExpiredConfirmationCodeException.class,
                () -> service.confirmMemberByToken(token));
        
        assertEquals("Token de confirmation expiré", exception.getMessage());
        verify(confirmationRepository).getByToken(token);
        verify(memberRepository, never()).getByMemberId(any());
        verify(memberRepository, never()).save(any());
        verify(confirmationRepository, never()).delete(any());
    }

    @Test
    void confirmMemberByToken_ShouldThrow_WhenMemberNotFound() {
        // given
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(confirmationRepository.getByToken(token)).thenReturn(Optional.of(confirmation));
        when(memberRepository.getByMemberId(memberId)).thenReturn(Optional.empty());

        // when/then
        MemberNotFoundException exception = assertThrows(MemberNotFoundException.class,
                () -> service.confirmMemberByToken(token));
        
        assertEquals("Membre non trouvé", exception.getMessage());
        verify(confirmationRepository).getByToken(token);
        verify(memberRepository).getByMemberId(memberId);
        verify(memberRepository, never()).save(any());
        verify(confirmationRepository, never()).delete(any());
        verify(authenticationService, never()).retrievePostActivationToken(any(), any());
    }

    @Test
    void confirmMemberByToken_ShouldThrow_WhenMemberAlreadyConfirmed() {
        // given
        Member confirmedMember = member.confirm();
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(confirmationRepository.getByToken(token)).thenReturn(Optional.of(confirmation));
        when(memberRepository.getByMemberId(memberId)).thenReturn(Optional.of(confirmedMember));

        // when/then
        MemberAlreadyConfirmedException exception = assertThrows(MemberAlreadyConfirmedException.class,
                () -> service.confirmMemberByToken(token));
        
        assertEquals("Le compte est déjà confirmé", exception.getMessage());
        verify(confirmationRepository).getByToken(token);
        verify(memberRepository).getByMemberId(memberId);
        verify(memberRepository, never()).save(any());
        verify(confirmationRepository, never()).delete(any());
        verify(authenticationService, never()).retrievePostActivationToken(any(), any());
    }

    @Test
    void sendConfirmationLink_ByEmail_ShouldGenerateAndPublishEvent() {
        // given
        String email = "john@test.com";
        UUID newToken = UUID.randomUUID();
        
        when(memberRepository.getByEmail(email)).thenReturn(Optional.of(member));
        when(confirmationRepository.getByMemberId(memberId)).thenReturn(Optional.empty());
        when(tokenGenerator.generateToken()).thenReturn(newToken);

        // when
        service.sendConfirmationLink(email);

        // then
        verify(memberRepository).getByEmail(email);
        verify(confirmationRepository).getByMemberId(memberId);
        verify(tokenGenerator).generateToken();
        verify(confirmationRepository).save(argThat(confirmation -> 
            confirmation.getToken().equals(newToken) &&
            confirmation.getMemberId().equals(memberId) &&
            confirmation.getCreatedAt() != null &&
            confirmation.getExpiresAt() != null &&
            confirmation.getExpiresAt().isAfter(confirmation.getCreatedAt())
        ));
        
        // Verify event is published instead of direct notifier call
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        MemberRegisteredEvent publishedEvent = eventCaptor.getValue();
        assertEquals("john@test.com", publishedEvent.getEmail());
        assertEquals(newToken, publishedEvent.getConfirmationToken());
        assertEquals("John", publishedEvent.getFirstName());
    }

    @Test
    void sendConfirmationLink_ByEmail_ShouldThrow_WhenAlreadyConfirmed() {
        // given
        String email = "john@test.com";
        Member confirmedMember = member.confirm();
        
        when(memberRepository.getByEmail(email)).thenReturn(Optional.of(confirmedMember));

        // when/then
        MemberAlreadyConfirmedException exception = assertThrows(MemberAlreadyConfirmedException.class, 
                () -> service.sendConfirmationLink(email));
        
        assertEquals("Votre compte est déjà confirmé. Vous pouvez vous connecter directement.", exception.getMessage());
        verify(memberRepository).getByEmail(email);
        verify(confirmationRepository, never()).getByMemberId(any());
        verify(confirmationRepository, never()).save(any());
        verify(tokenGenerator, never()).generateToken();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void sendConfirmationLink_ByEmail_ShouldDoNothing_WhenEmailNotFound() {
        // given - Anti-enumeration: ne lève pas d'exception
        String email = "nonexistent@test.com";
        
        when(memberRepository.getByEmail(email)).thenReturn(Optional.empty());

        // when
        service.sendConfirmationLink(email);

        // then - no exception, silent fail for anti-enumeration
        verify(memberRepository).getByEmail(email);
        verify(confirmationRepository, never()).getByMemberId(any());
        verify(confirmationRepository, never()).save(any());
        verify(tokenGenerator, never()).generateToken();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void sendConfirmationLink_ByMember_ShouldGenerateAndPublishEvent() {
        // given
        UUID newToken = UUID.randomUUID();
        
        when(confirmationRepository.getByMemberId(memberId)).thenReturn(Optional.empty());
        when(tokenGenerator.generateToken()).thenReturn(newToken);

        // when
        service.sendConfirmationLink(member);

        // then
        verify(confirmationRepository).getByMemberId(memberId);
        verify(tokenGenerator).generateToken();
        verify(confirmationRepository).save(argThat(confirmation -> 
            confirmation.getToken().equals(newToken) &&
            confirmation.getMemberId().equals(memberId) &&
            confirmation.getCreatedAt() != null &&
            confirmation.getExpiresAt() != null
        ));
        
        // Verify event is published instead of direct notifier call
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        MemberRegisteredEvent publishedEvent = eventCaptor.getValue();
        assertEquals("john@test.com", publishedEvent.getEmail());
        assertEquals(newToken, publishedEvent.getConfirmationToken());
        assertEquals("John", publishedEvent.getFirstName());
    }

    @Test
    void sendConfirmationLink_ByEmail_ShouldDeleteOldTokenAndGenerateNew() {
        // given - Il existe déjà un token de confirmation
        String email = "john@test.com";
        UUID oldToken = UUID.randomUUID();
        UUID newToken = UUID.randomUUID();
        
        MemberConfirmation oldConfirmation = MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .token(oldToken)
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(23))
                .build();
        
        when(memberRepository.getByEmail(email)).thenReturn(Optional.of(member));
        when(confirmationRepository.getByMemberId(memberId)).thenReturn(Optional.of(oldConfirmation));
        when(tokenGenerator.generateToken()).thenReturn(newToken);

        // when
        service.sendConfirmationLink(email);

        // then - L'ancien token doit être supprimé et un nouveau créé
        verify(confirmationRepository).getByMemberId(memberId);
        verify(confirmationRepository).delete(oldConfirmation);
        verify(confirmationRepository).save(argThat(confirmation -> 
            confirmation.getToken().equals(newToken) && 
            confirmation.getMemberId().equals(memberId)
        ));
        
        // Verify event is published with new token
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        MemberRegisteredEvent publishedEvent = eventCaptor.getValue();
        assertEquals(email, publishedEvent.getEmail());
        assertEquals(newToken, publishedEvent.getConfirmationToken());
        assertEquals("John", publishedEvent.getFirstName());
    }

    @Test
    void sendConfirmationLink_ByMember_ShouldDeleteOldTokenAndGenerateNew() {
        // given - Il existe déjà un token de confirmation
        UUID oldToken = UUID.randomUUID();
        UUID newToken = UUID.randomUUID();
        
        MemberConfirmation oldConfirmation = MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .token(oldToken)
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(23))
                .build();
        
        when(confirmationRepository.getByMemberId(memberId)).thenReturn(Optional.of(oldConfirmation));
        when(tokenGenerator.generateToken()).thenReturn(newToken);

        // when
        service.sendConfirmationLink(member);

        // then - L'ancien token doit être supprimé et un nouveau créé
        verify(confirmationRepository).getByMemberId(memberId);
        verify(confirmationRepository).delete(oldConfirmation);
        verify(confirmationRepository).save(argThat(confirmation -> 
            confirmation.getToken().equals(newToken) && 
            confirmation.getMemberId().equals(memberId)
        ));
        
        // Verify event is published with new token
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        MemberRegisteredEvent publishedEvent = eventCaptor.getValue();
        assertEquals("john@test.com", publishedEvent.getEmail());
        assertEquals(newToken, publishedEvent.getConfirmationToken());
        assertEquals("John", publishedEvent.getFirstName());
    }
}
