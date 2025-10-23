package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.model.MembersGetConfirmationStatus200Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembersTemporaryDelegateTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MembersTemporaryDelegate membersTemporaryDelegate;

    // --- Tests for sayHello ---
    @Test
    void sayHello_shouldReturnHiMemberMessage() {
        // When
        ResponseEntity<String> response = membersTemporaryDelegate.sayHello();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hi Member");
        verifyNoInteractions(memberService);
    }

    // --- Tests for getMemberConfirmationStatus ---
    @Test
    void getMemberConfirmationStatus_shouldReturnTrueWhenConfirmed() {
        // Given
        String email = "confirmed@example.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(true);

        // When
        ResponseEntity<MembersGetConfirmationStatus200Response> response = 
                membersTemporaryDelegate.getMemberConfirmationStatus(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConfirmed()).isTrue();

        verify(memberService).isEmailConfirmed(email);
    }

    @Test
    void getMemberConfirmationStatus_shouldReturnFalseWhenNotConfirmed() {
        // Given
        String email = "unconfirmed@example.com";
        when(memberService.isEmailConfirmed(email)).thenReturn(false);

        // When
        ResponseEntity<MembersGetConfirmationStatus200Response> response = 
                membersTemporaryDelegate.getMemberConfirmationStatus(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConfirmed()).isFalse();

        verify(memberService).isEmailConfirmed(email);
    }

    @Test
    void getMemberConfirmationStatus_shouldPropagateMemberNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(memberService.isEmailConfirmed(email))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When/Then
        assertThatThrownBy(() -> membersTemporaryDelegate.getMemberConfirmationStatus(email))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(memberService).isEmailConfirmed(email);
    }
}
