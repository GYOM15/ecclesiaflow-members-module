package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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
        member.confirm();

        when(memberRepository.findByEmail("john.doe@mail.com")).thenReturn(Optional.of(member));

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
                .build();

        when(memberRepository.findById(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.updateMember(update);

        assertEquals("NewName", result.getFirstName());
        verify(memberRepository).save(any(Member.class));
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
    void getAllMembers_shouldReturnList() {
        List<Member> members = List.of(
                Member.builder().memberId(UUID.randomUUID()).firstName("A").email("a@mail.com").build(),
                Member.builder().memberId(UUID.randomUUID()).firstName("B").email("b@mail.com").build()
        );

        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.getAllMembers();

        assertEquals(2, result.size());
    }
}
