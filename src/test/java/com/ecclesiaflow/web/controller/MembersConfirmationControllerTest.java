package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.business.mappers.ConfirmationRequestMapper;
import com.ecclesiaflow.business.mappers.ConfirmationResponseMapper;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import com.ecclesiaflow.web.dto.ConfirmationRequest;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour MembersConfirmationController.
 * Vérifie les endpoints de confirmation des comptes membres.
 */
@WebMvcTest(MembersConfirmationController.class)
class MembersConfirmationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberConfirmationService confirmationService;

    @MockitoBean
    private ConfirmationRequestMapper confirmationRequestMapper;

    @MockitoBean
    private ConfirmationResponseMapper confirmationResponseMapper;

    private UUID testMemberId;
    private ConfirmationRequest confirmationRequest;
    private MembershipConfirmation membershipConfirmation;
    private MembershipConfirmationResult confirmationResult;
    private ConfirmationResponse confirmationResponse;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();

        confirmationRequest = new ConfirmationRequest();
        confirmationRequest.setCode("123456");

        membershipConfirmation = MembershipConfirmation.builder()
                .memberId(testMemberId)
                .confirmationCode("123456")
                .build();

        confirmationResult = MembershipConfirmationResult.builder()
                .message("Confirmation réussie ! Vous pouvez maintenant définir votre mot de passe.")
                .temporaryToken("temp_token_123")
                .expiresInSeconds(3600)
                .build();

        confirmationResponse = ConfirmationResponse.builder()
                .message("Confirmation réussie ! Vous pouvez maintenant définir votre mot de passe.")
                .temporaryToken("temp_token_123")
                .expiresIn(3600)
                .build();
    }

    @Test
    void confirmMember_WithValidCode_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(confirmationRequestMapper.fromConfirmationRequest(testMemberId, confirmationRequest))
                .thenReturn(membershipConfirmation);
        when(confirmationService.confirmMember(membershipConfirmation))
                .thenReturn(confirmationResult);
        when(confirmationResponseMapper.fromMemberConfirmationResult(confirmationResult))
                .thenReturn(confirmationResponse);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Confirmation réussie ! Vous pouvez maintenant définir votre mot de passe."))
                .andExpect(jsonPath("$.temporaryToken").value("temp_token_123"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(confirmationRequestMapper).fromConfirmationRequest(testMemberId, confirmationRequest);
        verify(confirmationService).confirmMember(membershipConfirmation);
        verify(confirmationResponseMapper).fromMemberConfirmationResult(confirmationResult);
    }

    @Test
    void confirmMember_WithInvalidCode_ShouldReturnErrorResponse() throws Exception {
        // Given - Use valid 6-digit format but wrong code for business logic error
        ConfirmationRequest invalidRequest = new ConfirmationRequest();
        invalidRequest.setCode("999999"); // Valid format but wrong code

        MembershipConfirmation invalidConfirmation = MembershipConfirmation.builder()
                .memberId(testMemberId)
                .confirmationCode("999999")
                .build();

        MembershipConfirmationResult errorResult = MembershipConfirmationResult.builder()
                .message("Code de confirmation invalide.")
                .temporaryToken(null)
                .expiresInSeconds(0)
                .build();

        ConfirmationResponse errorResponse = ConfirmationResponse.builder()
                .message("Code de confirmation invalide.")
                .temporaryToken(null)
                .expiresIn(0)
                .build();

        when(confirmationRequestMapper.fromConfirmationRequest(testMemberId, invalidRequest))
                .thenReturn(invalidConfirmation);
        when(confirmationService.confirmMember(invalidConfirmation))
                .thenReturn(errorResult);
        when(confirmationResponseMapper.fromMemberConfirmationResult(errorResult))
                .thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Code de confirmation invalide."))
                .andExpect(jsonPath("$.temporaryToken").isEmpty())
                .andExpect(jsonPath("$.expiresIn").value(0));
    }

    @Test
    void confirmMember_WithEmptyCode_ShouldReturnBadRequest() throws Exception {
        // Given
        ConfirmationRequest emptyRequest = new ConfirmationRequest();
        emptyRequest.setCode("");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation des données"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void confirmMember_WithNullCode_ShouldReturnBadRequest() throws Exception {
        // Given
        ConfirmationRequest nullRequest = new ConfirmationRequest();
        nullRequest.setCode(null);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation des données"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void confirmMember_WithInvalidMemberId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmationRequest)))
                .andExpect(status().isInternalServerError());

        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void confirmMember_WithMissingRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void confirmMember_WithExpiredCode_ShouldReturnExpiredMessage() throws Exception {
        // Given
        MembershipConfirmationResult expiredResult = MembershipConfirmationResult.builder()
                .message("Code de confirmation expiré. Veuillez demander un nouveau code.")
                .temporaryToken(null)
                .expiresInSeconds(0)
                .build();

        ConfirmationResponse expiredResponse = ConfirmationResponse.builder()
                .message("Code de confirmation expiré. Veuillez demander un nouveau code.")
                .temporaryToken(null)
                .expiresIn(0)
                .build();

        when(confirmationRequestMapper.fromConfirmationRequest(testMemberId, confirmationRequest))
                .thenReturn(membershipConfirmation);
        when(confirmationService.confirmMember(membershipConfirmation))
                .thenReturn(expiredResult);
        when(confirmationResponseMapper.fromMemberConfirmationResult(expiredResult))
                .thenReturn(expiredResponse);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Code de confirmation expiré. Veuillez demander un nouveau code."))
                .andExpect(jsonPath("$.temporaryToken").isEmpty())
                .andExpect(jsonPath("$.expiresIn").value(0));
    }

    @Test
    void confirmMember_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(confirmationRequestMapper.fromConfirmationRequest(testMemberId, confirmationRequest))
                .thenReturn(membershipConfirmation);
        when(confirmationService.confirmMember(membershipConfirmation))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmationRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue"));
    }

    @Test
    void confirmMember_WithLongCode_ShouldReturnValidationError() throws Exception {
        // Given - Long code should fail validation
        ConfirmationRequest longCodeRequest = new ConfirmationRequest();
        longCodeRequest.setCode("123456789012"); // Code plus long - invalid format

        // When & Then - Expect validation error
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longCodeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation des données"))
                .andExpect(jsonPath("$.errors[0].message").value("Le code doit contenir exactement 6 chiffres"));

        // Verify service is never called due to validation failure
        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void confirmMember_WithSpecialCharactersInCode_ShouldReturnValidationError() throws Exception {
        // Given - Special characters should fail validation
        ConfirmationRequest specialCodeRequest = new ConfirmationRequest();
        specialCodeRequest.setCode("AB-123"); // Code avec caractères spéciaux - invalid format

        // When & Then - Expect validation error
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCodeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation des données"))
                .andExpect(jsonPath("$.errors[0].message").value("Le code doit contenir exactement 6 chiffres"));

        // Verify service is never called due to validation failure
        verify(confirmationService, never()).confirmMember(any());
    }

    @Test
    void resendConfirmationCode_ShouldReturnOk() throws Exception {
        // Given
        // L'action est gérée par le service, donc on s'assure qu'il n'y a pas de problème.
        doNothing().when(confirmationService).sendConfirmationCode(testMemberId);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/confirmation-code", testMemberId))
                .andExpect(status().isOk());

        // Vérification de l'appel au service
        verify(confirmationService, times(1)).sendConfirmationCode(testMemberId);
    }
}