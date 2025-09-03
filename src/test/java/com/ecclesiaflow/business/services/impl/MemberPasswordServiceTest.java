package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.services.MemberPasswordService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.web.client.AuthClient;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MemberPasswordService.
 * Vérifie l'orchestration des opérations de mot de passe.
 */
@ExtendWith(MockitoExtension.class)
class MemberPasswordServiceTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberPasswordService memberPasswordService;

    private Member testMember;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();
        
        testMember = new Member();
        testMember.setId(testMemberId);
        testMember.setEmail("test@example.com");
        testMember.setFirstName("Jean");
        testMember.setLastName("Dupont");
    }

    @Test
    void setPassword_WithValidData_ShouldDelegateToAuthModule() {
        // Given
        String email = "test@example.com";
        String password = "newPassword123";
        String temporaryToken = "temp_token_123";

        // When
        memberPasswordService.setPassword(email, password, temporaryToken);

        // Then
        verify(authClient).setPassword(email, password, temporaryToken);
    }

    @Test
    void setPassword_WithNullValues_ShouldStillDelegateToAuthModule() {
        // Given
        String email = null;
        String password = null;
        String temporaryToken = null;

        // When
        memberPasswordService.setPassword(email, password, temporaryToken);

        // Then
        verify(authClient).setPassword(email, password, temporaryToken);
    }

    @Test
    void setPassword_WithEmptyValues_ShouldStillDelegateToAuthModule() {
        // Given
        String email = "";
        String password = "";
        String temporaryToken = "";

        // When
        memberPasswordService.setPassword(email, password, temporaryToken);

        // Then
        verify(authClient).setPassword(email, password, temporaryToken);
    }

    @Test
    void setPassword_WhenAuthModuleThrowsException_ShouldPropagateException() {
        // Given
        String email = "test@example.com";
        String password = "newPassword123";
        String temporaryToken = "temp_token_123";
        
        RuntimeException authException = new RuntimeException("Auth module error");
        doThrow(authException).when(authClient).setPassword(email, password, temporaryToken);

        // When & Then
        assertThrows(RuntimeException.class, () ->
            memberPasswordService.setPassword(email, password, temporaryToken));
    }

    @Test
    void getMemberIdByEmail_WithExistingEmail_ShouldReturnMemberId() {
        // Given
        String email = "test@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        UUID result = memberPasswordService.getMemberIdByEmail(email);

        // Then
        assertEquals(testMemberId, result);
        verify(memberRepository).findByEmail(email);
    }

    @Test
    void getMemberIdByEmail_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        MemberNotFoundException exception = assertThrows(MemberNotFoundException.class, () ->
            memberPasswordService.getMemberIdByEmail(email));
        
        assertEquals("Membre non trouvé pour l'email : " + email, exception.getMessage());
        verify(memberRepository).findByEmail(email);
    }

    @Test
    void getMemberIdByEmail_WithNullEmail_ShouldCallRepository() {
        // Given
        String email = null;
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () ->
            memberPasswordService.getMemberIdByEmail(email));
        
        verify(memberRepository).findByEmail(email);
    }

    @Test
    void getMemberIdByEmail_WithEmptyEmail_ShouldCallRepository() {
        // Given
        String email = "";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () ->
            memberPasswordService.getMemberIdByEmail(email));
        
        verify(memberRepository).findByEmail(email);
    }

    @Test
    void getMemberIdByEmail_WithValidEmailButNullMemberId_ShouldReturnNull() {
        // Given
        String email = "test@example.com";
        testMember.setId(null);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        UUID result = memberPasswordService.getMemberIdByEmail(email);

        // Then
        assertNull(result);
        verify(memberRepository).findByEmail(email);
    }

    @Test
    void getMemberIdByEmail_WithSpecialCharactersInEmail_ShouldWork() {
        // Given
        String email = "jean.françois+test@église.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        UUID result = memberPasswordService.getMemberIdByEmail(email);

        // Then
        assertEquals(testMemberId, result);
        verify(memberRepository).findByEmail(email);
    }
}
