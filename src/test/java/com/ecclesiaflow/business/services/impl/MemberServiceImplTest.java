package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.member.*;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
