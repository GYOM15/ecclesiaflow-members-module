package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.domain.communication.CodeGenerator;
import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.confirmation.*;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.token.TokenGenerator;
import com.ecclesiaflow.web.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberConfirmationServiceImplTest {

    private MemberRepository memberRepository;
    private MemberConfirmationRepository confirmationRepository;
    private TokenGenerator tokenGenerator;
    private ConfirmationNotifier confirmationNotifier;
    private CodeGenerator codeGenerator;

    private MemberConfirmationServiceImpl service;

    private UUID memberId;
    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        confirmationRepository = mock(MemberConfirmationRepository.class);
        tokenGenerator = mock(TokenGenerator.class);
        confirmationNotifier = mock(ConfirmationNotifier.class);
        codeGenerator = mock(CodeGenerator.class);

        service = new MemberConfirmationServiceImpl(
                memberRepository, confirmationRepository, tokenGenerator, confirmationNotifier, codeGenerator);

        memberId = UUID.randomUUID();
        member = Member.builder()
                .id(memberId)
                .memberId(memberId)
                .firstName("John")
                .email("john@test.com")
                .build();
    }

    @Test
    void confirmMember_ShouldSucceed_WhenCodeValid() {
        // given
        String code = "123456";
        MemberConfirmation confirmation = MemberConfirmation.builder()
                .memberId(memberId)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(confirmationRepository.findByMemberIdAndCode(memberId, code)).thenReturn(Optional.of(confirmation));
        when(tokenGenerator.generateTemporaryToken(member.getEmail())).thenReturn("TEMP_TOKEN");

        // when
        MembershipConfirmation input = MembershipConfirmation.builder().
                                                                memberId(memberId).
                                                                confirmationCode(code).
                                                                build();
        MembershipConfirmationResult result = service.confirmMember(input);

        // then
        assertEquals("TEMP_TOKEN", result.getTemporaryToken());
        verify(memberRepository).save(member);
        verify(confirmationRepository).delete(confirmation);
    }

    @Test
    void confirmMember_ShouldThrow_WhenCodeInvalid() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(confirmationRepository.findByMemberIdAndCode(any(), any())).thenReturn(Optional.empty());

        assertThrows(InvalidConfirmationCodeException.class,
                () -> service.confirmMember(MembershipConfirmation.builder().
                        memberId(memberId).
                        confirmationCode("WRONG").
                        build()));
    }

    @Test
    void confirmMember_ShouldThrow_WhenCodeExpired() {
        String code = "123456";
        MemberConfirmation expired = MemberConfirmation.builder()
                .memberId(memberId)
                .code(code)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(confirmationRepository.findByMemberIdAndCode(memberId, code)).thenReturn(Optional.of(expired));

        assertThrows(ExpiredConfirmationCodeException.class,
                () -> service.confirmMember(MembershipConfirmation.builder().
                        memberId(memberId).
                        confirmationCode(code).
                        build()));
    }

    @Test
    void sendConfirmationCode_ShouldGenerateAndSend() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(codeGenerator.generateCode()).thenReturn("654321");

        service.sendConfirmationCode(memberId);

        verify(confirmationRepository).save(any(MemberConfirmation.class));
        verify(confirmationNotifier).sendCode(eq("john@test.com"), eq("654321"), eq("John"));
    }

    @Test
    void sendConfirmationCode_ShouldThrow_WhenAlreadyConfirmed() {
        member.confirm();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(MemberAlreadyConfirmedException.class, () -> service.sendConfirmationCode(memberId));
    }
}
