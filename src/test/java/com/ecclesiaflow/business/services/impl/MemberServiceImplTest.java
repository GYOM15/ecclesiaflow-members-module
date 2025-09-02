package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.business.domain.MembershipUpdate;
import com.ecclesiaflow.business.services.EmailService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.MemberConfirmation;
import com.ecclesiaflow.io.entities.Role;
import com.ecclesiaflow.io.repository.MemberConfirmationRepository;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl - Tests unitaires")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberConfirmationRepository confirmationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MembershipRegistration testRegistration;
    private Member testMember;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();

        testRegistration = new MembershipRegistration(
                "Jean",
                "Dupont",
                "jean.dupont@example.com",
                "123 Rue de la Paix"
        );

        testMember = new Member();
        testMember.setId(testMemberId);
        testMember.setMemberId(UUID.randomUUID());
        testMember.setFirstName("Jean");
        testMember.setLastName("Dupont");
        testMember.setEmail("jean.dupont@example.com");
        testMember.setAddress("123 Rue de la Paix");
        testMember.setRole(Role.MEMBER);
        testMember.setConfirmed(false);
        testMember.setCreatedAt(LocalDateTime.now());
    }

    // --- TESTS EXISTANTS (omitted for brevity) ---
    @Test
    void registerMember_WithNewEmail_ShouldCreateMemberAndSendConfirmation() {
        when(memberRepository.existsByEmail(testRegistration.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(confirmationRepository.findByMemberId(any())).thenReturn(Optional.empty());

        Member result = memberService.registerMember(testRegistration);

        assertNotNull(result);
        assertEquals(testMember, result);
        verify(memberRepository).save(any(Member.class));
        verify(confirmationRepository).save(any(MemberConfirmation.class));
        verify(emailService).sendConfirmationCode(anyString(), anyString(), anyString());
    }

    // --- NOUVEAU TEST 1 : Gérer l'échec de l'envoi d'email ---
    @Test
    @DisplayName("Devrait créer le membre et le code même si l'envoi de l'email échoue")
    void registerMember_WithEmailServiceFailure_ShouldStillCreateMemberAndConfirmation() {
        // Given
        when(memberRepository.existsByEmail(testRegistration.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(confirmationRepository.findByMemberId(any())).thenReturn(Optional.empty());

        doThrow(new RuntimeException("Email service failure"))
                .when(emailService).sendConfirmationCode(anyString(), anyString(), anyString());

        // When
        assertDoesNotThrow(() -> memberService.registerMember(testRegistration));

        // Then
        // Utiliser ArgumentCaptor pour vérifier l'objet passé à save()
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(1)).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertNotNull(capturedMember);
        assertEquals(testMember.getEmail(), capturedMember.getEmail());
        assertEquals(testMember.getFirstName(), capturedMember.getFirstName());

        verify(confirmationRepository).save(any(MemberConfirmation.class));
    }

    // --- NOUVEAU TEST 2 : Gérer la suppression de l'ancien code ---
    @Test
    @DisplayName("Devrait supprimer l'ancien code de confirmation avant d'en créer un nouveau")
    void registerMember_WithExistingConfirmation_ShouldDeleteOldCode() {
        // Given
        MemberConfirmation existingConfirmation = new MemberConfirmation();
        when(memberRepository.existsByEmail(testRegistration.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(confirmationRepository.findByMemberId(testMemberId)).thenReturn(Optional.of(existingConfirmation));

        // When
        Member result = memberService.registerMember(testRegistration);

        // Then
        verify(confirmationRepository).findByMemberId(testMemberId);
        verify(confirmationRepository).delete(existingConfirmation);
        verify(confirmationRepository, times(1)).save(any(MemberConfirmation.class)); // S'assurer qu'un nouveau code est bien créé
        verify(emailService).sendConfirmationCode(anyString(), anyString(), anyString());
    }

    // --- TESTS EXISTANTS (omitted for brevity) ---
    @Test
    void registerMember_WithExistingEmail_ShouldThrowException() {
        when(memberRepository.existsByEmail(testRegistration.email())).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.registerMember(testRegistration));
        assertEquals("Un compte avec cet email existe déjà.", exception.getMessage());
        verify(memberRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationCode(anyString(), anyString(), anyString());
    }

    @Test
    void isEmailAlreadyUsed_WithExistingEmail_ShouldReturnTrue() {
        when(memberRepository.existsByEmail("existing@example.com")).thenReturn(true);
        boolean result = memberService.isEmailAlreadyUsed("existing@example.com");
        assertTrue(result);
    }

    @Test
    void isEmailAlreadyUsed_WithNewEmail_ShouldReturnFalse() {
        when(memberRepository.existsByEmail("new@example.com")).thenReturn(false);
        boolean result = memberService.isEmailAlreadyUsed("new@example.com");
        assertFalse(result);
    }

    @Test
    void isEmailConfirmed_WithConfirmedEmail_ShouldReturnTrue() {
        testMember.setConfirmed(true);
        when(memberRepository.findByEmail("confirmed@example.com")).thenReturn(Optional.of(testMember));
        boolean result = memberService.isEmailConfirmed("confirmed@example.com");
        assertTrue(result);
    }

    @Test
    void isEmailConfirmed_WithUnconfirmedEmail_ShouldReturnFalse() {
        testMember.setConfirmed(false);
        when(memberRepository.findByEmail("unconfirmed@example.com")).thenReturn(Optional.of(testMember));
        boolean result = memberService.isEmailConfirmed("unconfirmed@example.com");
        assertFalse(result);
    }

    @Test
    void isEmailConfirmed_WithNonExistentEmail_ShouldReturnFalse() {
        when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        boolean result = memberService.isEmailConfirmed("nonexistent@example.com");
        assertFalse(result);
    }

    @Test
    void findById_WithExistingId_ShouldReturnMember() {
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        Member result = memberService.findById(testMemberId);
        assertNotNull(result);
        assertEquals(testMember, result);
    }

    @Test
    void findById_WithNonExistentId_ShouldThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        when(memberRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class, () -> memberService.findById(nonExistentId));
    }

    @Test
    void updateMember_WithValidData_ShouldUpdateAndReturnMember() {
        MembershipUpdate updateRequest = MembershipUpdate.builder()
                .memberId(testMemberId)
                .firstName("Jean-Updated")
                .lastName("Dupont-Updated")
                .email("jean.updated@example.com")
                .address("456 Nouvelle Adresse")
                .build();
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        Member result = memberService.updateMember(updateRequest);
        assertNotNull(result);
        verify(memberRepository).save(testMember);
        assertEquals("Jean-Updated", testMember.getFirstName());
        assertEquals("Dupont-Updated", testMember.getLastName());
        assertEquals("jean.updated@example.com", testMember.getEmail());
        assertEquals("456 Nouvelle Adresse", testMember.getAddress());
    }

    @Test
    void updateMember_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        MembershipUpdate updateRequest = MembershipUpdate.builder()
                .memberId(testMemberId)
                .firstName("Jean-Updated")
                .email("jean.updated@example.com")
                .build();
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        Member result = memberService.updateMember(updateRequest);
        assertNotNull(result);
        assertEquals("Jean-Updated", testMember.getFirstName());
        assertEquals("jean.updated@example.com", testMember.getEmail());
        assertEquals("Dupont", testMember.getLastName());
        assertEquals("123 Rue de la Paix", testMember.getAddress());
    }

    @Test
    void deleteMember_WithExistingId_ShouldDeleteMember() {
        when(memberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));
        memberService.deleteMember(testMemberId);
        verify(memberRepository).delete(testMember);
    }

    @Test
    void deleteMember_WithNonExistentId_ShouldThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        when(memberRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class, () -> memberService.deleteMember(nonExistentId));
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void getAllMembers_ShouldReturnAllMembers() {
        Member member2 = new Member();
        member2.setId(UUID.randomUUID());
        member2.setEmail("member2@example.com");
        List<Member> expectedMembers = Arrays.asList(testMember, member2);
        when(memberRepository.findAll()).thenReturn(expectedMembers);
        List<Member> result = memberService.getAllMembers();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedMembers, result);
    }
}