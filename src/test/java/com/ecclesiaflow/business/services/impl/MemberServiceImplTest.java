package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.events.MemberActivatedEvent;
import com.ecclesiaflow.business.domain.member.*;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.exceptions.SocialAccountAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberConfirmationService confirmationService;

    @Mock
    private AuthClient authClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MembershipRegistration registration;

    @BeforeEach
    void setUp() {
        registration = new MembershipRegistration("John", "Doe", "john.doe@mail.com", "123 Street",null );
    }

    @Test
    void registerMember_shouldSaveAndSendConfirmation() {
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .address("123 Street")
                .build();

        when(memberRepository.existsByEmail(registration.email())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.registerMember(registration);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(memberRepository).save(any(Member.class));
        verify(confirmationService).sendConfirmationLink(result);
    }

    @Test
    void registerMember_shouldThrowIfEmailAlreadyUsed() {
        when(memberRepository.existsByEmail(registration.email())).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> memberService.registerMember(registration));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void isEmailAlreadyUsed_shouldReturnTrueIfExists() {
        when(memberRepository.existsByEmail("john.doe@mail.com")).thenReturn(true);

        assertTrue(memberService.isEmailAlreadyUsed("john.doe@mail.com"));
    }

    @Test
    void isEmailConfirmed_shouldReturnTrueIfMemberConfirmed() {
        Member member = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("John")
                .email("john.doe@mail.com")
                .build();
        Member confirmedMember = member.confirm();

        when(memberRepository.getByEmail("john.doe@mail.com")).thenReturn(Optional.of(confirmedMember));

        assertTrue(memberService.isEmailConfirmed("john.doe@mail.com"));
    }

    @Test
    void isEmailConfirmed_shouldReturnFalseIfNotFound() {
        when(memberRepository.getByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        assertFalse(memberService.isEmailConfirmed("unknown@mail.com"));
    }

    @Test
    void findById_shouldReturnMemberIfExists() {
        UUID id = UUID.randomUUID();
        Member member = Member.builder().memberId(id).firstName("Jane").email("jane@mail.com").build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(member));

        Member result = memberService.findByMemberId(id);

        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void findById_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.getByMemberId(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.findByMemberId(id));
    }

    @Test
    void updateMember_shouldUpdateAndSave() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id)
                .firstName("OldName")
                .email("old@mail.com")
                .build();

        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(id)
                .firstName("NewName")
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.updateMember(update);

        assertEquals("NewName", result.getFirstName());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void updateMember_shouldThrowIfMemberNotFound() {
        UUID id = UUID.randomUUID();
        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(id)
                .firstName("NewName")
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.updateMember(update));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void deleteMember_shouldDeleteKeycloakUserThenDbMember() {
        UUID id = UUID.randomUUID();
        String keycloakUserId = "kc-user-123";
        Member existing = Member.builder()
                .memberId(id).firstName("ToDelete").email("del@mail.com")
                .keycloakUserId(keycloakUserId).status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));

        memberService.deleteMember(id);

        var inOrder = inOrder(authClient, memberRepository);
        inOrder.verify(authClient).deleteKeycloakUser(keycloakUserId);
        inOrder.verify(memberRepository).delete(existing);
    }

    @Test
    void deleteMember_shouldSkipKeycloakWhenUserIdIsNull() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id).firstName("Pending").email("pending@mail.com")
                .keycloakUserId(null).status(MemberStatus.PENDING)
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));

        memberService.deleteMember(id);

        verify(authClient, never()).deleteKeycloakUser(any());
        verify(memberRepository).delete(existing);
    }

    @Test
    void deleteMember_shouldNotDeleteDbWhenKeycloakFails() {
        UUID id = UUID.randomUUID();
        String keycloakUserId = "kc-user-456";
        Member existing = Member.builder()
                .memberId(id).firstName("ToDelete").email("del@mail.com")
                .keycloakUserId(keycloakUserId).status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("Keycloak unavailable"))
                .when(authClient).deleteKeycloakUser(keycloakUserId);

        assertThrows(RuntimeException.class, () -> memberService.deleteMember(id));

        verify(memberRepository, never()).delete(any());
    }

    @Test
    void getAllMembers_shouldReturnAllMembersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").build(),
                Member.builder().memberId(UUID.randomUUID()).firstName("B").email("b@mail.com").build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 2);

        when(memberRepository.getAll(pageable)).thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, null, null);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(memberRepository, times(1)).getAll(pageable);
    }

    @Test
    void getAllMembers_shouldReturnMembersWithSearchTerm() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String searchTerm = "john";
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("John").email("john@mail.com").build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getMembersBySearchTerm(searchTerm, pageable)).thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, searchTerm, null);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().getFirst().getFirstName());
        verify(memberRepository, times(1)).getMembersBySearchTerm(searchTerm, pageable);
    }

    @Test
    void getAllMembers_shouldReturnMembersWithStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        MemberStatus status = MemberStatus.ACTIVE;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").status(MemberStatus.ACTIVE).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getByStatus(status, pageable)).thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, null, status);

        // Then
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().isConfirmed());
        verify(memberRepository, times(1)).getByStatus(status, pageable);
    }

    @Test
    void getAllMembers_shouldReturnMembersWithSearchTermAndStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String searchTerm = "john";
        MemberStatus status = MemberStatus.PENDING;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("John").email("john@mail.com").status(MemberStatus.PENDING).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getMembersBySearchTermAndStatus(searchTerm, status, pageable))
                .thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, searchTerm, status);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().getFirst().getFirstName());
        assertFalse(result.getContent().getFirst().isConfirmed());
        verify(memberRepository, times(1)).getMembersBySearchTermAndStatus(searchTerm, status, pageable);
    }

    @Test
    void getAllMembers_shouldThrowExceptionWhenPageableIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> memberService.getAllMembers(null, null, null));
        
        verifyNoInteractions(memberRepository);
    }

    @Test
    void getAllMembers_shouldNormalizeEmptySearchToNull() {
        // Given - Empty search string should be normalized to null
        Pageable pageable = PageRequest.of(0, 20);
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getAll(pageable)).thenReturn(memberPage);

        // When - Empty string should be normalized to null
        Page<Member> result = memberService.getAllMembers(pageable, "", null);

        // Then - Should call getAll (no search criteria)
        assertEquals(1, result.getContent().size());
        verify(memberRepository, times(1)).getAll(pageable);
        verify(memberRepository, never()).getMembersBySearchTerm(anyString(), any());
    }

    @Test
    void getAllMembers_shouldNormalizeWhitespaceSearchToNull() {
        // Given - Whitespace-only search should be normalized to null
        Pageable pageable = PageRequest.of(0, 20);
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getAll(pageable)).thenReturn(memberPage);

        // When - Whitespace-only string should be normalized to null
        Page<Member> result = memberService.getAllMembers(pageable, "   ", null);

        // Then - Should call getAll (no search criteria)
        assertEquals(1, result.getContent().size());
        verify(memberRepository, times(1)).getAll(pageable);
        verify(memberRepository, never()).getMembersBySearchTerm(anyString(), any());
    }

    @Test
    void getAllMembers_shouldTrimSearchTerm() {
        // Given - Search with leading/trailing spaces should be trimmed
        Pageable pageable = PageRequest.of(0, 20);
        String searchTermWithSpaces = "  john  ";
        String trimmedSearchTerm = "john";
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("John").email("john@mail.com").build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getMembersBySearchTerm(trimmedSearchTerm, pageable)).thenReturn(memberPage);

        // When - Search term should be trimmed
        Page<Member> result = memberService.getAllMembers(pageable, searchTermWithSpaces, null);

        // Then - Should call with trimmed search term
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().getFirst().getFirstName());
        verify(memberRepository, times(1)).getMembersBySearchTerm(trimmedSearchTerm, pageable);
        verify(memberRepository, never()).getMembersBySearchTerm(searchTermWithSpaces, pageable);
    }

    @Test
    void getAllMembers_shouldTrimSearchTermWithStatus() {
        // Given - Search with spaces and status filter
        Pageable pageable = PageRequest.of(0, 20);
        String searchTermWithSpaces = "  jane  ";
        String trimmedSearchTerm = "jane";
        MemberStatus status = MemberStatus.ACTIVE;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("Jane").email("jane@mail.com").status(MemberStatus.ACTIVE).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getMembersBySearchTermAndStatus(trimmedSearchTerm, status, pageable))
                .thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, searchTermWithSpaces, status);

        // Then - Should call with trimmed search term
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().getFirst().isConfirmed());
        verify(memberRepository, times(1)).getMembersBySearchTermAndStatus(trimmedSearchTerm, status, pageable);
    }

    @Test
    void getAllMembers_shouldHandleEmptySearchWithStatus() {
        // Given - Empty search with status should ignore search
        Pageable pageable = PageRequest.of(0, 20);
        MemberStatus status = MemberStatus.PENDING;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").status(MemberStatus.PENDING).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getByStatus(status, pageable)).thenReturn(memberPage);

        // When - Empty search should be normalized to null
        Page<Member> result = memberService.getAllMembers(pageable, "  ", status);

        // Then - Should only filter by status
        assertEquals(1, result.getContent().size());
        verify(memberRepository, times(1)).getByStatus(status, pageable);
        verify(memberRepository, never()).getMembersBySearchTermAndStatus(anyString(), any(), any());
    }

    // --- deactivateMember tests ---

    @Test
    void deactivateMember_shouldSetStatusDeactivatedWithTimestamp() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id).firstName("ToDeactivate").email("deact@mail.com")
                .keycloakUserId("kc-user-123").status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.deactivateMember(id);

        verify(memberRepository).save(argThat(member ->
                member.getStatus() == MemberStatus.DEACTIVATED
                        && member.getDeactivatedAt() != null));
        verifyNoInteractions(authClient);
    }

    @Test
    void deactivateMember_shouldThrowWhenMemberNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.getByMemberId(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.deactivateMember(id));

        verify(memberRepository, never()).save(any());
    }

    // --- reactivateMember tests ---

    @Test
    void reactivateMember_shouldSetStatusActiveAndClearDeactivatedAt() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id).firstName("Deactivated").email("deact@mail.com")
                .keycloakUserId("kc-user-123").status(MemberStatus.DEACTIVATED)
                .deactivatedAt(java.time.LocalDateTime.now().minusDays(5))
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.reactivateMember(id);

        assertEquals(MemberStatus.ACTIVE, result.getStatus());
        assertNull(result.getDeactivatedAt());
        verify(memberRepository).save(argThat(member ->
                member.getStatus() == MemberStatus.ACTIVE
                        && member.getDeactivatedAt() == null));
    }

    @Test
    void reactivateMember_shouldThrowWhenNotDeactivated() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id).firstName("Active").email("active@mail.com")
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.getByMemberId(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> memberService.reactivateMember(id));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void reactivateMember_shouldThrowWhenMemberNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.getByMemberId(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.reactivateMember(id));

        verify(memberRepository, never()).save(any());
    }

    // --- registerSocialMember tests ---

    @Test
    void registerSocialMember_shouldPublishMemberActivatedEvent() {
        // Given
        String keycloakUserId = "kc-social-123";
        MembershipRegistration socialRegistration = new MembershipRegistration(
                "Alice", "Martin", "alice@gmail.com", "456 Avenue", null);

        Member savedMember = Member.builder()
                .memberId(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Martin")
                .email("alice@gmail.com")
                .address("456 Avenue")
                .keycloakUserId(keycloakUserId)
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.existsByEmail(socialRegistration.email())).thenReturn(false);
        when(memberRepository.existsByKeycloakUserId(keycloakUserId)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        Member result = memberService.registerSocialMember(keycloakUserId, SocialProvider.GOOGLE, socialRegistration);

        // Then
        assertNotNull(result);
        assertEquals("Alice", result.getFirstName());
        assertEquals("alice@gmail.com", result.getEmail());

        ArgumentCaptor<MemberActivatedEvent> eventCaptor = ArgumentCaptor.forClass(MemberActivatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        MemberActivatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals("alice@gmail.com", publishedEvent.email());
        assertEquals("Alice", publishedEvent.firstName());
    }

    @Test
    void registerSocialMember_shouldNotPublishEventWhenEmailAlreadyExists() {
        // Given
        String keycloakUserId = "kc-social-456";
        MembershipRegistration socialRegistration = new MembershipRegistration(
                "Bob", "Dupont", "bob@gmail.com", "789 Rue", null);

        when(memberRepository.existsByEmail(socialRegistration.email())).thenReturn(true);

        // When & Then
        assertThrows(SocialAccountAlreadyExistsException.class,
                () -> memberService.registerSocialMember(keycloakUserId, SocialProvider.GOOGLE, socialRegistration));

        verify(memberRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void registerSocialMember_shouldNotPublishEventWhenKeycloakUserIdAlreadyExists() {
        // Given
        String keycloakUserId = "kc-social-789";
        MembershipRegistration socialRegistration = new MembershipRegistration(
                "Claire", "Bernard", "claire@gmail.com", "101 Boulevard", null);

        when(memberRepository.existsByEmail(socialRegistration.email())).thenReturn(false);
        when(memberRepository.existsByKeycloakUserId(keycloakUserId)).thenReturn(true);

        // When & Then
        assertThrows(SocialAccountAlreadyExistsException.class,
                () -> memberService.registerSocialMember(keycloakUserId, SocialProvider.GOOGLE, socialRegistration));

        verify(memberRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
