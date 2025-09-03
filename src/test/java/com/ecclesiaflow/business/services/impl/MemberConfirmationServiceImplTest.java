package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.services.EmailService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.MemberConfirmation;
import com.ecclesiaflow.io.repository.MemberConfirmationRepository;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.web.client.AuthClient;
import com.ecclesiaflow.web.exception.ExpiredConfirmationCodeException;
import com.ecclesiaflow.web.exception.InvalidConfirmationCodeException;
import com.ecclesiaflow.web.exception.MemberAlreadyConfirmedException;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MemberConfirmationServiceImpl.
 * Vérifie la logique de confirmation des comptes membres.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberConfirmationServiceImpl - Tests Unitaires")
class MemberConfirmationServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberConfirmationRepository confirmationRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MemberConfirmationServiceImpl confirmationService;

    private UUID testMemberId;
    private Member testMember;
    private MemberConfirmation testConfirmation;
    private MembershipConfirmation testRequest;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();

        testMember = new Member();
        testMember.setId(testMemberId);
        testMember.setMemberId(UUID.randomUUID());
        testMember.setEmail("test@example.com");
        testMember.setFirstName("Jean");
        testMember.setConfirmed(false);

        testConfirmation = MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(testMemberId)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        testRequest = MembershipConfirmation.builder()
                .memberId(testMemberId)
                .confirmationCode("123456")
                .build();
    }

    // --- TESTS POUR confirmMember(MembershipConfirmation confirmationRequest) ---
    @Test
    @DisplayName("Devrait confirmer un membre avec un code valide et retourner un token")
    void confirmMember_WithValidCode_ShouldConfirmMemberAndReturnToken() {
        // Given
        String expectedToken = "temp_token_123";
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(confirmationRepository.findByMemberIdAndCode(testMemberId, "123456")).thenReturn(Optional.of(testConfirmation));
        when(authClient.generateTemporaryToken(any(String.class))).thenReturn(expectedToken);

        // When
        MembershipConfirmationResult result = confirmationService.confirmMember(testRequest);

        // Then
        assertNotNull(result);
        assertEquals("Compte confirmé avec succès", result.getMessage());
        assertEquals(expectedToken, result.getTemporaryToken());
        assertEquals(900, result.getExpiresInSeconds()); // 15 minutes

        assertTrue(testMember.isConfirmed());
        verify(memberRepository).save(testMember);
        verify(confirmationRepository).delete(testConfirmation);
    }

    @Test
    @DisplayName("Devrait lancer une exception pour un code invalide")
    void confirmMember_WithInvalidCode_ShouldThrowException() {
        // Given
        testRequest = MembershipConfirmation.builder()
                .memberId(testMemberId)
                .confirmationCode("wrong_code")
                .build();

        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(confirmationRepository.findByMemberIdAndCode(testMemberId, "wrong_code")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidConfirmationCodeException.class,
                () -> confirmationService.confirmMember(testRequest));
    }

    @Test
    @DisplayName("Devrait lancer une exception pour un code expiré")
    void confirmMember_WithExpiredCode_ShouldThrowException() {
        // Given
        testConfirmation.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(confirmationRepository.findByMemberIdAndCode(testMemberId, "123456")).thenReturn(Optional.of(testConfirmation));

        // When & Then
        assertThrows(ExpiredConfirmationCodeException.class,
                () -> confirmationService.confirmMember(testRequest));
    }

    @Test
    @DisplayName("Devrait lancer une exception si le membre n'existe pas")
    void confirmMember_WithNonExistentMember_ShouldThrowException() {
        // Given
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class,
                () -> confirmationService.confirmMember(testRequest));
    }

    @Test
    @DisplayName("Devrait lancer une exception si le membre est déjà confirmé")
    void confirmMember_WithAlreadyConfirmedMember_ShouldThrowException() {
        // Given
        testMember.setConfirmed(true);
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));

        // When & Then
        assertThrows(MemberAlreadyConfirmedException.class,
                () -> confirmationService.confirmMember(testRequest));
    }

    // --- NOUVEAUX TESTS POUR sendConfirmationCode(UUID memberId) ---
    @Test
    @DisplayName("Devrait envoyer un code de confirmation pour un nouveau membre")
    void sendConfirmationCode_WithNewMember_ShouldSendCode() {
        // Given
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(confirmationRepository.findByMemberId(testMemberId)).thenReturn(Optional.empty());
        when(confirmationRepository.save(any(MemberConfirmation.class))).thenReturn(any());

        // When
        confirmationService.sendConfirmationCode(testMemberId);

        // Then
        verify(confirmationRepository, times(1)).save(any(MemberConfirmation.class));
        verify(emailService, times(1)).sendConfirmationCode(
                eq(testMember.getEmail()), any(String.class), eq(testMember.getFirstName()));
    }

    @Test
    @DisplayName("Devrait supprimer l'ancien code avant d'en créer un nouveau")
    void sendConfirmationCode_WithExistingCode_ShouldDeleteOldCodeAndSendNewOne() {
        // Given
        MemberConfirmation oldConfirmation = MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(testMemberId)
                .code("old_code")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(confirmationRepository.findByMemberId(testMemberId)).thenReturn(Optional.of(oldConfirmation));

        // When
        confirmationService.sendConfirmationCode(testMemberId);

        // Then
        verify(confirmationRepository, times(1)).delete(oldConfirmation);
        verify(confirmationRepository, times(1)).save(any(MemberConfirmation.class));
        verify(emailService, times(1)).sendConfirmationCode(
                eq(testMember.getEmail()), any(String.class), eq(testMember.getFirstName()));
    }

    @Test
    @DisplayName("Devrait lancer une exception si le membre n'existe pas lors de l'envoi du code")
    void sendConfirmationCode_WithNonExistentMember_ShouldThrowException() {
        // Given
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class,
                () -> confirmationService.sendConfirmationCode(testMemberId));

        verify(confirmationRepository, never()).findByMemberId(any());
        verify(confirmationRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationCode(any(), any(), any());
    }

    @Test
    @DisplayName("Devrait lancer une exception si le membre est déjà confirmé lors de l'envoi du code")
    void sendConfirmationCode_WithAlreadyConfirmedMember_ShouldThrowException() {
        // Given
        testMember.setConfirmed(true);
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));

        // When & Then
        assertThrows(MemberAlreadyConfirmedException.class,
                () -> confirmationService.sendConfirmationCode(testMemberId));

        verify(confirmationRepository, never()).findByMemberId(any());
        verify(confirmationRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationCode(any(), any(), any());
    }

}