package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.services.MemberPasswordService;
import com.ecclesiaflow.web.dto.SetPasswordRequest;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour MemberPasswordController.
 * Vérifie les endpoints de gestion des mots de passe.
 */
@WebMvcTest(MemberPasswordController.class)
class MemberPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberPasswordService memberPasswordService;

    private SetPasswordRequest setPasswordRequest;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();

        setPasswordRequest = new SetPasswordRequest();
        setPasswordRequest.setEmail("test@example.com");
        setPasswordRequest.setPassword("newPassword123!");
        setPasswordRequest.setTemporaryToken("temp_token_123");
    }

    @Test
    void setPassword_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        when(memberPasswordService.getMemberIdByEmail("test@example.com"))
                .thenReturn(testMemberId);
        doNothing().when(memberPasswordService)
                .setPassword("test@example.com", "newPassword123!", "temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(setPasswordRequest)))
                .andExpect(status().isOk());

        verify(memberPasswordService).getMemberIdByEmail("test@example.com");
        verify(memberPasswordService).setPassword("test@example.com", "newPassword123!", "temp_token_123");
    }

    @Test
    void setPassword_WithInvalidEmail_ShouldReturnNotFound() throws Exception {
        // Given
        UUID invalidMemberId = UUID.randomUUID();
        when(memberPasswordService.getMemberIdByEmail("nonexistent@example.com"))
                .thenThrow(new MemberNotFoundException("Membre non trouvé pour l'email : nonexistent@example.com"));

        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("nonexistent@example.com");
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", invalidMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());

        verify(memberPasswordService).getMemberIdByEmail("nonexistent@example.com");
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail(null);
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("");
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword(null);
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("");
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithMissingTemporaryToken_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken(null);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithEmptyTemporaryToken_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken("");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        SetPasswordRequest invalidRequest = new SetPasswordRequest();
        invalidRequest.setEmail("invalid-email-format");
        invalidRequest.setPassword("newPassword123!");
        invalidRequest.setTemporaryToken("temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(memberPasswordService.getMemberIdByEmail("test@example.com"))
                .thenReturn(testMemberId);
        doThrow(new RuntimeException("Service error"))
                .when(memberPasswordService)
                .setPassword("test@example.com", "newPassword123!", "temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(setPasswordRequest)))
                .andExpect(status().isInternalServerError());

        verify(memberPasswordService).getMemberIdByEmail("test@example.com");
        verify(memberPasswordService).setPassword("test@example.com", "newPassword123!", "temp_token_123");
    }

    @Test
    void setPassword_WithMissingRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123"))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).getMemberIdByEmail(anyString());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithSpecialCharactersInEmail_ShouldWork() throws Exception {
        // Given
        SetPasswordRequest specialRequest = new SetPasswordRequest();
        specialRequest.setEmail("jean.françois+test@église.com");
        specialRequest.setPassword("newPassword123!");
        specialRequest.setTemporaryToken("temp_token_123");

        when(memberPasswordService.getMemberIdByEmail("jean.françois+test@église.com"))
                .thenReturn(testMemberId);
        doNothing().when(memberPasswordService)
                .setPassword("jean.françois+test@église.com", "newPassword123!", "temp_token_123");

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(specialRequest)))
                .andExpect(status().isOk());

        verify(memberPasswordService).getMemberIdByEmail("jean.françois+test@église.com");
        verify(memberPasswordService).setPassword("jean.françois+test@église.com", "newPassword123!", "temp_token_123");
    }

    @Test
    void setPassword_WithMismatchedMemberId_ShouldReturnBadRequest() throws Exception {
        // Given
        UUID differentMemberId = UUID.randomUUID();
        when(memberPasswordService.getMemberIdByEmail(setPasswordRequest.getEmail()))
                .thenReturn(differentMemberId);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer temp_token_123")
                        .content(objectMapper.writeValueAsString(setPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'email ne correspond pas à l'ID du membre"));

        verify(memberPasswordService).getMemberIdByEmail(setPasswordRequest.getEmail());
        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithInvalidBearerTokenFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        when(memberPasswordService.getMemberIdByEmail(anyString())).thenReturn(testMemberId);

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Invalid Token") // Pas de 'Bearer '
                        .content(objectMapper.writeValueAsString(setPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Format du token invalide"));

        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }

    @Test
    void setPassword_WithMissingAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // Given
        // L'absence de header sera gérée par Spring qui renvoie un bad request

        // When & Then
        mockMvc.perform(post("/ecclesiaflow/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setPasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(memberPasswordService, never()).setPassword(anyString(), anyString(), anyString());
    }
}