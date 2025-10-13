package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.ConfirmationRequestPayload;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberConfirmationDelegateTest {

    @Mock
    private MemberConfirmationService confirmationService;

    @Mock
    private OpenApiModelMapper openApiModelMapper;

    @InjectMocks
    private MemberConfirmationDelegate memberConfirmationDelegate;

    // --- Tests for confirmMember ---
    @Test
    void confirmMember_shouldReturnSuccessResponse() {
        // Given
        UUID memberId = UUID.randomUUID();
        String code = "123456";
        ConfirmationRequestPayload requestPayload = new ConfirmationRequestPayload();
        requestPayload.setCode(code);

        MembershipConfirmationResult businessResult = MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresInSeconds(900)
                .build();

        ConfirmationResponse expectedResponse = new ConfirmationResponse()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresIn(900L);

        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenReturn(businessResult);
        when(openApiModelMapper.createConfirmationResponse(businessResult))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ConfirmationResponse> response = memberConfirmationDelegate.confirmMember(memberId, requestPayload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Compte confirmé avec succès");
        assertThat(response.getBody().getTemporaryToken()).isEqualTo("temp-token-abc123");
        assertThat(response.getBody().getExpiresIn()).isEqualTo(900L);

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verify(openApiModelMapper).createConfirmationResponse(businessResult);
    }

    @Test
    void confirmMember_shouldPassCorrectMemberIdAndCode() {
        // Given
        UUID memberId = UUID.randomUUID();
        String code = "654321";
        ConfirmationRequestPayload requestPayload = new ConfirmationRequestPayload();
        requestPayload.setCode(code);

        MembershipConfirmationResult businessResult = MembershipConfirmationResult.builder()
                .message("Success")
                .temporaryToken("token")
                .expiresInSeconds(900)
                .build();

        ConfirmationResponse response = new ConfirmationResponse()
                .message("Success")
                .temporaryToken("token")
                .expiresIn(900L);

        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenReturn(businessResult);
        when(openApiModelMapper.createConfirmationResponse(businessResult))
                .thenReturn(response);

        // When
        memberConfirmationDelegate.confirmMember(memberId, requestPayload);

        // Then
        verify(confirmationService).confirmMember(argThat(confirmation ->
                confirmation.getMemberId().equals(memberId) &&
                        confirmation.getConfirmationCode().equals(code)
        ));
    }

    @Test
    void confirmMember_shouldPropagateInvalidConfirmationCodeException() {
        // Given
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload requestPayload = new ConfirmationRequestPayload();
        requestPayload.setCode("999999");

        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new InvalidConfirmationCodeException("Code de confirmation invalide"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMember(memberId, requestPayload))
                .isInstanceOf(InvalidConfirmationCodeException.class)
                .hasMessage("Code de confirmation invalide");

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(openApiModelMapper);
    }

    @Test
    void confirmMember_shouldPropagateExpiredConfirmationCodeException() {
        // Given
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload requestPayload = new ConfirmationRequestPayload();
        requestPayload.setCode("123456");

        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new ExpiredConfirmationCodeException("Code de confirmation expiré"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMember(memberId, requestPayload))
                .isInstanceOf(ExpiredConfirmationCodeException.class)
                .hasMessage("Code de confirmation expiré");

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(openApiModelMapper);
    }

    @Test
    void confirmMember_shouldPropagateMemberNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload requestPayload = new ConfirmationRequestPayload();
        requestPayload.setCode("123456");

        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMember(memberId, requestPayload))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(openApiModelMapper);
    }

    // --- Tests for resendConfirmationCode ---
    @Test
    void resendConfirmationCode_shouldReturnOkResponse() {
        // Given
        UUID memberId = UUID.randomUUID();
        doNothing().when(confirmationService).sendConfirmationCode(memberId);

        // When
        ResponseEntity<Void> response = memberConfirmationDelegate.resendConfirmationCode(memberId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(confirmationService).sendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldPropagateMemberNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        doThrow(new MemberNotFoundException("Membre non trouvé"))
                .when(confirmationService).sendConfirmationCode(memberId);

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.resendConfirmationCode(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Membre non trouvé");

        verify(confirmationService).sendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldPropagateMemberAlreadyConfirmedException() {
        // Given
        UUID memberId = UUID.randomUUID();
        doThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé. Aucun nouveau code n'est requis."))
                .when(confirmationService).sendConfirmationCode(memberId);

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.resendConfirmationCode(memberId))
                .isInstanceOf(MemberAlreadyConfirmedException.class)
                .hasMessage("Le compte est déjà confirmé. Aucun nouveau code n'est requis.");

        verify(confirmationService).sendConfirmationCode(memberId);
    }
}
