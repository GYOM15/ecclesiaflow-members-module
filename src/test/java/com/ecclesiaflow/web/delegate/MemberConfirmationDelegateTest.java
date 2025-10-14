package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.mappers.OpenApiModelMapper;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.ResendConfirmationLink200Response;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberConfirmationDelegateTest {

    @Mock
    private MemberConfirmationService confirmationService;

    @Mock
    private OpenApiModelMapper openApiModelMapper;

    @InjectMocks
    private MemberConfirmationDelegate memberConfirmationDelegate;

    // --- Tests for confirmMemberByToken ---
    @Test
    void confirmMemberByToken_shouldReturnSuccessResponse() {
        // Given
        UUID token = UUID.randomUUID();

        MembershipConfirmationResult businessResult = MembershipConfirmationResult.builder()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresInSeconds(900)
                .build();

        ConfirmationResponse expectedResponse = new ConfirmationResponse()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresIn(900L);

        when(confirmationService.confirmMemberByToken(token))
                .thenReturn(businessResult);
        when(openApiModelMapper.createConfirmationResponse(businessResult))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ConfirmationResponse> response = memberConfirmationDelegate.confirmMemberByToken(token);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Compte confirmé avec succès");
        assertThat(response.getBody().getTemporaryToken()).isEqualTo("temp-token-abc123");
        assertThat(response.getBody().getExpiresIn()).isEqualTo(900L);

        verify(confirmationService).confirmMemberByToken(token);
        verify(openApiModelMapper).createConfirmationResponse(businessResult);
    }

    @Test
    void confirmMemberByToken_shouldPassCorrectToken() {
        // Given
        UUID token = UUID.randomUUID();

        MembershipConfirmationResult businessResult = MembershipConfirmationResult.builder()
                .message("Success")
                .temporaryToken("token")
                .expiresInSeconds(900)
                .build();

        ConfirmationResponse response = new ConfirmationResponse()
                .message("Success")
                .temporaryToken("token")
                .expiresIn(900L);

        when(confirmationService.confirmMemberByToken(token))
                .thenReturn(businessResult);
        when(openApiModelMapper.createConfirmationResponse(businessResult))
                .thenReturn(response);

        // When
        memberConfirmationDelegate.confirmMemberByToken(token);

        // Then
        verify(confirmationService).confirmMemberByToken(token);
    }

    @Test
    void confirmMemberByToken_shouldPropagateInvalidConfirmationCodeException() {
        // Given
        UUID token = UUID.randomUUID();

        when(confirmationService.confirmMemberByToken(token))
                .thenThrow(new InvalidConfirmationCodeException("Token de confirmation invalide"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMemberByToken(token))
                .isInstanceOf(InvalidConfirmationCodeException.class)
                .hasMessage("Token de confirmation invalide");

        verify(confirmationService).confirmMemberByToken(token);
        verifyNoInteractions(openApiModelMapper);
    }

    @Test
    void confirmMemberByToken_shouldPropagateExpiredConfirmationCodeException() {
        // Given
        UUID token = UUID.randomUUID();

        when(confirmationService.confirmMemberByToken(token))
                .thenThrow(new ExpiredConfirmationCodeException("Token de confirmation expiré"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMemberByToken(token))
                .isInstanceOf(ExpiredConfirmationCodeException.class)
                .hasMessage("Token de confirmation expiré");

        verify(confirmationService).confirmMemberByToken(token);
        verifyNoInteractions(openApiModelMapper);
    }

    @Test
    void confirmMemberByToken_shouldPropagateMemberAlreadyConfirmedException() {
        // Given
        UUID token = UUID.randomUUID();

        when(confirmationService.confirmMemberByToken(token))
                .thenThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé"));

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.confirmMemberByToken(token))
                .isInstanceOf(MemberAlreadyConfirmedException.class)
                .hasMessage("Le compte est déjà confirmé");

        verify(confirmationService).confirmMemberByToken(token);
        verifyNoInteractions(openApiModelMapper);
    }

    // --- Tests for resendConfirmationLink ---
    @Test
    void resendConfirmationLink_shouldReturnOkResponse() {
        // Given
        String email = "john@test.com";
        doNothing().when(confirmationService).sendConfirmationLink(email);

        // When
        ResponseEntity<ResendConfirmationLink200Response> response = memberConfirmationDelegate.resendConfirmationLink(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Si cette adresse email");
        assertThat(response.getBody().getExpiresIn()).isEqualTo(86400L);

        verify(confirmationService).sendConfirmationLink(email);
    }

    @Test
    void resendConfirmationLink_shouldReturnOkEvenIfEmailNotFound() {
        // Given - Anti-enumeration: même comportement si l'email n'existe pas
        String email = "nonexistent@test.com";
        doNothing().when(confirmationService).sendConfirmationLink(email);

        // When
        ResponseEntity<ResendConfirmationLink200Response> response = memberConfirmationDelegate.resendConfirmationLink(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(confirmationService).sendConfirmationLink(email);
    }

    @Test
    void resendConfirmationLink_shouldPropagateMemberAlreadyConfirmedException() {
        // Given
        String email = "confirmed@test.com";
        doThrow(new MemberAlreadyConfirmedException("Votre compte est déjà confirmé. Vous pouvez vous connecter directement."))
                .when(confirmationService).sendConfirmationLink(email);

        // When/Then
        assertThatThrownBy(() -> memberConfirmationDelegate.resendConfirmationLink(email))
                .isInstanceOf(MemberAlreadyConfirmedException.class)
                .hasMessage("Votre compte est déjà confirmé. Vous pouvez vous connecter directement.");

        verify(confirmationService).sendConfirmationLink(email);
    }
}
