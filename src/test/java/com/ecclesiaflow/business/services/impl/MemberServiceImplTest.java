package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.services.MemberConfirmationService;
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
        verify(confirmationService).sendConfirmationCode(result);
    }

    @Test
    void registerMember_shouldThrowIfEmailAlreadyUsed() {
        when(memberRepository.existsByEmail(registration.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> memberService.registerMember(registration));

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

        when(memberRepository.findByEmail("john.doe@mail.com")).thenReturn(Optional.of(confirmedMember));

        assertTrue(memberService.isEmailConfirmed("john.doe@mail.com"));
    }

    @Test
    void isEmailConfirmed_shouldReturnFalseIfNotFound() {
        when(memberRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        assertFalse(memberService.isEmailConfirmed("unknown@mail.com"));
    }

    @Test
    void findById_shouldReturnMemberIfExists() {
        UUID id = UUID.randomUUID();
        Member member = Member.builder().memberId(id).firstName("Jane").email("jane@mail.com").build();

        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        Member result = memberService.findById(id);

        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void findById_shouldThrowIfNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.findById(id));
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
                .email("new@mail.com")
                .build();

        when(memberRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(memberRepository.findById(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.updateMember(update);

        assertEquals("NewName", result.getFirstName());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void updateMember_shouldThrowIfEmailAlreadyUsed() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder()
                .memberId(id)
                .firstName("OldName")
                .email("old@mail.com")
                .build();

        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(id)
                .firstName("NewName")
                .email("existing@mail.com")
                .build();

        when(memberRepository.existsByEmail("existing@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> memberService.updateMember(update));

        verify(memberRepository, never()).findById(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateMember_shouldThrowIfMemberNotFound() {
        UUID id = UUID.randomUUID();
        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(id)
                .firstName("NewName")
                .email("new@mail.com")
                .build();

        when(memberRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.updateMember(update));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void deleteMember_shouldDeleteIfExists() {
        UUID id = UUID.randomUUID();
        Member existing = Member.builder().memberId(id).firstName("ToDelete").email("del@mail.com").build();

        when(memberRepository.findById(id)).thenReturn(Optional.of(existing));

        memberService.deleteMember(id);

        verify(memberRepository).delete(existing);
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
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository, times(1)).getMembersBySearchTerm(searchTerm, pageable);
    }

    @Test
    void getAllMembers_shouldReturnMembersWithConfirmationStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Boolean confirmed = true;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").confirmed(true).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getByConfirmedStatus(confirmed, pageable)).thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, null, confirmed);

        // Then
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isConfirmed());
        verify(memberRepository, times(1)).getByConfirmedStatus(confirmed, pageable);
    }

    @Test
    void getAllMembers_shouldReturnMembersWithSearchTermAndConfirmationStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String searchTerm = "john";
        Boolean confirmed = false;
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("John").email("john@mail.com").confirmed(false).build()
        );
        Page<Member> memberPage = new PageImpl<>(members, pageable, 1);

        when(memberRepository.getMembersBySearchTermAndConfirmationStatus(searchTerm, confirmed, pageable))
                .thenReturn(memberPage);

        // When
        Page<Member> result = memberService.getAllMembers(pageable, searchTerm, confirmed);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
        assertFalse(result.getContent().get(0).isConfirmed());
        verify(memberRepository, times(1)).getMembersBySearchTermAndConfirmationStatus(searchTerm, confirmed, pageable);
    }

    @Test
    void getAllMembers_shouldThrowExceptionWhenPageableIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> memberService.getAllMembers(null, null, null));
        
        verifyNoInteractions(memberRepository);
    }
}
