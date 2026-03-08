package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.web.delegate.MemberConfirmationDelegate;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.MembersResendConfirmationLink200Response;
import com.ecclesiaflow.web.model.ResendConfirmationLinkRequest;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MembersConfirmationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler.class)
class MembersConfirmationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberConfirmationDelegate memberConfirmationDelegate;

    // --- Tests for GET /ecclesiaflow/members/confirmation?token={uuid} (confirmMemberByToken) ---
    @Test
    void confirmMemberByToken_shouldReturnSuccessWithToken() throws Exception {
        UUID token = UUID.randomUUID();

        ConfirmationResponse response = new ConfirmationResponse()
                .message("Compte confirmé avec succès")
                .temporaryToken("temp-token-abc123")
                .expiresIn(900L);

        try {
            response.setPasswordEndpoint(new java.net.URI("http://localhost:8081/ecclesiaflow/auth/password"));
        } catch (java.net.URISyntaxException e) {
            // Ignore
        }

        when(memberConfirmationDelegate.confirmMemberByToken(token))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ecclesiaflow/members/confirmation")
                        .param("token", token.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Compte confirmé avec succès"))
                .andExpect(jsonPath("$.temporaryToken").value("temp-token-abc123"))
                .andExpect(jsonPath("$.passwordEndpoint").exists())
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(memberConfirmationDelegate).confirmMemberByToken(token);
    }

    @Test
    void confirmMemberByToken_shouldReturnBadRequestForInvalidToken() throws Exception {
        UUID token = UUID.randomUUID();

        when(memberConfirmationDelegate.confirmMemberByToken(token))
                .thenThrow(new InvalidConfirmationCodeException("Token de confirmation invalide"));

        mockMvc.perform(get("/ecclesiaflow/members/confirmation")
                        .param("token", token.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmation invalide"));

        verify(memberConfirmationDelegate).confirmMemberByToken(token);
    }

    @Test
    void confirmMemberByToken_shouldReturnBadRequestForExpiredToken() throws Exception {
        UUID token = UUID.randomUUID();

        when(memberConfirmationDelegate.confirmMemberByToken(token))
                .thenThrow(new ExpiredConfirmationCodeException("Token de confirmation expiré"));

        mockMvc.perform(get("/ecclesiaflow/members/confirmation")
                        .param("token", token.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmation expiré"));

        verify(memberConfirmationDelegate).confirmMemberByToken(token);
    }

    @Test
    void confirmMemberByToken_shouldReturnConflictForAlreadyConfirmed() throws Exception {
        UUID token = UUID.randomUUID();

        when(memberConfirmationDelegate.confirmMemberByToken(token))
                .thenThrow(new MemberAlreadyConfirmedException("Le compte est déjà confirmé"));

        mockMvc.perform(get("/ecclesiaflow/members/confirmation")
                        .param("token", token.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Le compte est déjà confirmé"));

        verify(memberConfirmationDelegate).confirmMemberByToken(token);
    }


    // --- Tests for POST /ecclesiaflow/members/new-confirmation (resendConfirmationLink) ---
    @Test
    void resendConfirmationLink_shouldReturnOk() throws Exception {
        String email = "john@test.com";
        ResendConfirmationLinkRequest request = new ResendConfirmationLinkRequest();
        request.setEmail(email);

        MembersResendConfirmationLink200Response response = new MembersResendConfirmationLink200Response()
                .message("Si cette adresse email est associée à un compte non confirmé, un nouveau lien de confirmation a été envoyé.")
                .expiresIn(86400L);

        when(memberConfirmationDelegate.resendConfirmationLink(email))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/ecclesiaflow/members/new-confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si cette adresse email est associée à un compte non confirmé, un nouveau lien de confirmation a été envoyé."))
                .andExpect(jsonPath("$.expiresIn").value(86400));

        verify(memberConfirmationDelegate).resendConfirmationLink(email);
    }

    @Test
    void resendConfirmationLink_shouldReturnConflictForAlreadyConfirmed() throws Exception {
        String email = "confirmed@test.com";
        ResendConfirmationLinkRequest request = new ResendConfirmationLinkRequest();
        request.setEmail(email);

        when(memberConfirmationDelegate.resendConfirmationLink(email))
                .thenThrow(new MemberAlreadyConfirmedException("Votre compte est déjà confirmé. Vous pouvez vous connecter directement."));

        mockMvc.perform(post("/ecclesiaflow/members/new-confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Votre compte est déjà confirmé. Vous pouvez vous connecter directement."));

        verify(memberConfirmationDelegate).resendConfirmationLink(email);
    }

    @Test
    void resendConfirmationLink_shouldReturnOkEvenForNonexistentEmail() throws Exception {
        // Anti-enumeration: même comportement si l'email n'existe pas
        String email = "nonexistent@test.com";
        ResendConfirmationLinkRequest request = new ResendConfirmationLinkRequest();
        request.setEmail(email);

        MembersResendConfirmationLink200Response response = new MembersResendConfirmationLink200Response()
                .message("Si cette adresse email est associée à un compte non confirmé, un nouveau lien de confirmation a été envoyé.")
                .expiresIn(86400L);

        when(memberConfirmationDelegate.resendConfirmationLink(email))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/ecclesiaflow/members/new-confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si cette adresse email est associée à un compte non confirmé, un nouveau lien de confirmation a été envoyé."));

        verify(memberConfirmationDelegate).resendConfirmationLink(email);
    }

}