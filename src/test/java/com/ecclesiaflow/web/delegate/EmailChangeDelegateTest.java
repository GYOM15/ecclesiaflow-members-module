package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.business.services.EmailChangeService;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.EmailChangeResponse;
import com.ecclesiaflow.web.model.SignUpResponse;
import com.ecclesiaflow.web.model.UpdateEmailRequestPayload;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChangeDelegateTest {

    @Mock private EmailChangeService emailChangeService;
    @Mock private MemberService memberService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private OpenApiModelMapper openApiModelMapper;

    @InjectMocks
    private EmailChangeDelegate delegate;

    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final String KC_USER_ID = "kc-123";

    @Test
    void requestEmailChange_returns202() {
        Member member = Member.builder().memberId(MEMBER_ID).status(MemberStatus.ACTIVE).build();
        when(authenticatedUserService.getKeycloakUserId()).thenReturn(KC_USER_ID);
        when(memberService.getByKeycloakUserId(KC_USER_ID)).thenReturn(member);

        UpdateEmailRequestPayload payload = new UpdateEmailRequestPayload();
        payload.setEmail("new@example.com");

        ResponseEntity<EmailChangeResponse> response = delegate.requestEmailChange(payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Confirmation email sent");
        verify(emailChangeService).requestEmailChange(MEMBER_ID, "new@example.com");
    }

    @Test
    void confirmEmailChange_returns200() {
        UUID token = UUID.randomUUID();
        Member updated = Member.builder().memberId(MEMBER_ID).email("new@example.com").status(MemberStatus.ACTIVE).build();
        SignUpResponse dto = new SignUpResponse();
        dto.setEmail("new@example.com");

        when(emailChangeService.confirmEmailChange(token)).thenReturn(updated);
        when(openApiModelMapper.createSignUpResponse(updated, "Email updated")).thenReturn(dto);

        ResponseEntity<SignUpResponse> response = delegate.confirmEmailChange(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("new@example.com");
    }
}
