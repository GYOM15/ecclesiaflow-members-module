package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.payloads.ConfirmationRequestPayload;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler;
import com.ecclesiaflow.web.mappers.ConfirmationResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MembersConfirmationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MemberConfirmationService confirmationService;

    @Mock
    private ConfirmationResponseMapper confirmationResponseMapper;


    @InjectMocks
    private MembersConfirmationController membersConfirmationController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(membersConfirmationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // --- Tests for POST /ecclesiaflow/members/{memberId}/confirmation ---

    @Test
    void confirmMember_shouldReturnOkWithConfirmationResponse() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        // Using builder for MembershipConfirmationResult
        MembershipConfirmationResult serviceResult = MembershipConfirmationResult.builder()
                .message("Confirmation successful!")
                .temporaryToken("temp_token")
                .expiresInSeconds(3600)
                .build();

        // Using builder for ConfirmationResponse
        ConfirmationResponse responseDto = ConfirmationResponse.builder()
                .message("Confirmation successful!")
                .temporaryToken("temp_token")
                .expiresIn(3600) // Ensure the field is 'expiresIn' in the DTO
                .build();

        when(confirmationService.confirmMember(any(MembershipConfirmation.class))).thenReturn(serviceResult);
        when(confirmationResponseMapper.fromMemberConfirmationResult(any(MembershipConfirmationResult.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.message").value("Confirmation successful!"))
                .andExpect(jsonPath("$.temporaryToken").value("temp_token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verify(confirmationResponseMapper).fromMemberConfirmationResult(any(MembershipConfirmationResult.class));
    }

    @Test
    void confirmMember_shouldReturnBadRequestForInvalidCodeFormat() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("ABC12"); // Invalid code (e.g., does not meet @Pattern/@Size constraints)

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                // Verify ApiErrorResponse structure and ValidationError
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Erreur de validation des données")) // General message for MethodArgumentNotValidException
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].path").value("code")); // The 'code' field of ConfirmationRequestPayload is in error
        verifyNoInteractions(confirmationService);
        verifyNoInteractions(confirmationResponseMapper);
    }

    @Test
    void confirmMember_shouldReturnBadRequestForInvalidConfirmationCodeException() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        // The service simulates throwing web.exception.*
        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new InvalidConfirmationCodeException("Code de confirmation incorrect"));

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                // Verify ApiErrorResponse structure for a simple 400 error
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Code de confirmation incorrect"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].message").value("Code de confirmation incorrect"))
                .andExpect(jsonPath("$.errors[0].path").value("request"));


        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(confirmationResponseMapper);
    }

    @Test
    void confirmMember_shouldReturnBadRequestForExpiredConfirmationCodeException() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("654321");

        // The service simulates throwing web.exception.*
        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new ExpiredConfirmationCodeException("Le code de confirmation a expiré"));

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                // Verify ApiErrorResponse structure for a simple 400 error
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Le code de confirmation a expiré"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].message").value("Le code de confirmation a expiré"))
                .andExpect(jsonPath("$.errors[0].path").value("request"));

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(confirmationResponseMapper);
    }

    @Test
    void confirmMember_shouldReturnNotFoundForNonExistentMember() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("112233");

        // The service simulates throwing web.exception.*
        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                // Verify ApiErrorResponse structure for a simple 404 error
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Membre non trouvé"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation"))
                .andExpect(jsonPath("$.errors").doesNotExist()); // errors should be null for 404

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(confirmationResponseMapper);
    }

    @Test
    void confirmMember_shouldReturnConflictIfAlreadyConfirmed() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("789012");

        // The service simulates throwing web.exception.*
        when(confirmationService.confirmMember(any(MembershipConfirmation.class)))
                .thenThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé"));

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                // Verify ApiErrorResponse structure for a simple 409 error
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Le compte est déjà confirmé"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation"))
                .andExpect(jsonPath("$.errors").doesNotExist()); // errors should be null for 409

        verify(confirmationService).confirmMember(any(MembershipConfirmation.class));
        verifyNoInteractions(confirmationResponseMapper);
    }

    // --- Tests for POST /ecclesiaflow/members/{memberId}/confirmation-code (resendConfirmationCode) ---

    @Test
    void resendConfirmationCode_shouldReturnOk() throws Exception {
        UUID memberId = UUID.randomUUID();
        doNothing().when(confirmationService).sendConfirmationCode(memberId);

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation-code", memberId))
                .andExpect(status().isOk());

        verify(confirmationService).sendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldReturnNotFoundIfMemberDoesNotExist() throws Exception {
        UUID memberId = UUID.randomUUID();
        // The service simulates throwing web.exception.*
        doThrow(new MemberNotFoundException("Membre non trouvé pour le renvoi du code"))
                .when(confirmationService).sendConfirmationCode(memberId);

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation-code", memberId))
                .andExpect(status().isNotFound())
                // Verify ApiErrorResponse structure for a simple 404 error
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Membre non trouvé pour le renvoi du code"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation-code"))
                .andExpect(jsonPath("$.errors").doesNotExist()); // errors should be null for 404

        verify(confirmationService).sendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldReturnConflictIfAlreadyConfirmed() throws Exception {
        UUID memberId = UUID.randomUUID();
        // The service simulates throwing web.exception.*
        doThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé, impossible de renvoyer le code"))
                .when(confirmationService).sendConfirmationCode(memberId);

        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation-code", memberId))
                .andExpect(status().isConflict())
                // Verify ApiErrorResponse structure for a simple 409 error
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.message").value("Le compte est déjà confirmé, impossible de renvoyer le code"))
                .andExpect(jsonPath("$.path").value("/ecclesiaflow/members/" + memberId + "/confirmation-code"))
                .andExpect(jsonPath("$.errors").doesNotExist()); // errors should be null for 409

        verify(confirmationService).sendConfirmationCode(memberId);
    }
}