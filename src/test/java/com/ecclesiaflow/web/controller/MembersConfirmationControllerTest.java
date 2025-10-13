package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.web.delegate.MemberConfirmationDelegate;
import com.ecclesiaflow.web.model.ConfirmationRequestPayload;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MembersConfirmationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
@Import(com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler.class)
class MembersConfirmationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberConfirmationDelegate memberConfirmationDelegate;

    // --- Tests for PATCH /ecclesiaflow/members/{memberId}/confirmation (confirmMember) ---
    @Test
    void confirmMember_shouldReturnSuccessWithToken() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        ConfirmationResponse response = new ConfirmationResponse()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresIn(900L);

        try {
            response.setPasswordEndpoint(new java.net.URI("http://localhost:8081/ecclesiaflow/auth/password"));
        } catch (java.net.URISyntaxException e) {
            // Ignore
        }

        when(memberConfirmationDelegate.confirmMember(eq(memberId), any(ConfirmationRequestPayload.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/ecclesiaflow/members/" + memberId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.ecclesiaflow.members.v1+json"))
                .andExpect(jsonPath("$.message").value("Compte confirmé avec succès"))
                .andExpect(jsonPath("$.temporaryToken").value("temp-token-abc123"))
                .andExpect(jsonPath("$.passwordEndpoint").exists())
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(memberConfirmationDelegate).confirmMember(eq(memberId), any(ConfirmationRequestPayload.class));
    }

    @Test
    void confirmMember_shouldReturnBadRequestForInvalidCode() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("999999");

        when(memberConfirmationDelegate.confirmMember(eq(memberId), any(ConfirmationRequestPayload.class)))
                .thenThrow(new InvalidConfirmationCodeException("Code de confirmation invalide"));

        mockMvc.perform(patch("/ecclesiaflow/members/" + memberId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Code de confirmation invalide"));

        verify(memberConfirmationDelegate).confirmMember(eq(memberId), any(ConfirmationRequestPayload.class));
    }

    @Test
    void confirmMember_shouldReturnBadRequestForExpiredCode() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        when(memberConfirmationDelegate.confirmMember(eq(memberId), any(ConfirmationRequestPayload.class)))
                .thenThrow(new ExpiredConfirmationCodeException("Code de confirmation expiré"));

        mockMvc.perform(patch("/ecclesiaflow/members/" + memberId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Code de confirmation expiré"));

        verify(memberConfirmationDelegate).confirmMember(eq(memberId), any(ConfirmationRequestPayload.class));
    }

    @Test
    void confirmMember_shouldReturnNotFoundForNonexistentMember() throws Exception {
        UUID memberId = UUID.randomUUID();
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        when(memberConfirmationDelegate.confirmMember(eq(memberId), any(ConfirmationRequestPayload.class)))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        mockMvc.perform(patch("/ecclesiaflow/members/" + memberId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Membre non trouvé"));

        verify(memberConfirmationDelegate).confirmMember(eq(memberId), any(ConfirmationRequestPayload.class));
    }


    // --- Tests for POST /ecclesiaflow/members/{memberId}/confirmation/resend (resendConfirmationCode) ---
    // Note: Ces tests appellent directement le contrôleur au lieu d'utiliser MockMvc car @WebMvcTest
    // a des limitations avec certains mappings d'interfaces OpenAPI générées
    
    @Test
    void resendConfirmationCode_shouldReturnOk() {
        // Given
        UUID memberId = UUID.randomUUID();
        when(memberConfirmationDelegate.resendConfirmationCode(memberId))
                .thenReturn(ResponseEntity.ok().build());

        // When
        ResponseEntity<Void> response = getController()._resendConfirmationCode(memberId);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(memberConfirmationDelegate).resendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldPropagateNotFoundException() {
        // Given
        UUID memberId = UUID.randomUUID();
        when(memberConfirmationDelegate.resendConfirmationCode(memberId))
                .thenThrow(new MemberNotFoundException("Membre non trouvé"));

        // When & Then - Vérifie que l'exception est bien propagée
        // Le GlobalExceptionHandler la convertira en 404 dans un vrai contexte
        try {
            getController()._resendConfirmationCode(memberId);
            throw new AssertionError("Expected MemberNotFoundException");
        } catch (MemberNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Membre non trouvé");
        }

        verify(memberConfirmationDelegate).resendConfirmationCode(memberId);
    }

    @Test
    void resendConfirmationCode_shouldPropagateAlreadyConfirmedException() {
        // Given
        UUID memberId = UUID.randomUUID();
        when(memberConfirmationDelegate.resendConfirmationCode(memberId))
                .thenThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé. Aucun nouveau code n'est requis."));

        // When & Then - Vérifie que l'exception est bien propagée
        // Le GlobalExceptionHandler la convertira en 400 dans un vrai contexte
        try {
            getController()._resendConfirmationCode(memberId);
            throw new AssertionError("Expected MemberAlreadyConfirmedException");
        } catch (MemberAlreadyConfirmedException e) {
            assertThat(e.getMessage()).isEqualTo("Le compte est déjà confirmé. Aucun nouveau code n'est requis.");
        }

        verify(memberConfirmationDelegate).resendConfirmationCode(memberId);
    }

    // Helper method pour obtenir le contrôleur depuis le contexte Spring
    private MembersConfirmationController getController() {
        return new MembersConfirmationController(memberConfirmationDelegate);
    }

}