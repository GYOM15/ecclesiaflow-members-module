package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ConfirmationResponseMapper.
 * Vérifie la conversion des objets métier vers les DTOs de réponse.
 */
class ConfirmationResponseMapperTest {

    private ConfirmationResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ConfirmationResponseMapper();
    }

    @Test
    void fromMemberConfirmationResult_WithValidData_ShouldMapCorrectly() {
        // Given
        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("Confirmation réussie")
                .temporaryToken("temp_token_123")
                .expiresInSeconds(3600)
                .build();

        // When
        ConfirmationResponse response = mapper.fromMemberConfirmationResult(result);

        // Then
        assertNotNull(response);
        assertEquals("Confirmation réussie", response.getMessage());
        assertEquals("temp_token_123", response.getTemporaryToken());
        assertEquals(3600, response.getExpiresIn());
    }

    @Test
    void fromMemberConfirmationResult_WithNullResult_ShouldThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            mapper.fromMemberConfirmationResult(null);
        });
    }

    @Test
    void fromMemberConfirmationResult_WithNullToken_ShouldMapNullToken() {
        // Given
        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("Code invalide")
                .temporaryToken(null)
                .expiresInSeconds(0)
                .build();

        // When
        ConfirmationResponse response = mapper.fromMemberConfirmationResult(result);

        // Then
        assertNotNull(response);
        assertEquals("Code invalide", response.getMessage());
        assertNull(response.getTemporaryToken());
        assertEquals(0, response.getExpiresIn());
    }

    @Test
    void fromMemberConfirmationResult_WithEmptyMessage_ShouldMapEmptyMessage() {
        // Given
        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("")
                .temporaryToken("temp_token_456")
                .expiresInSeconds(1800)
                .build();

        // When
        ConfirmationResponse response = mapper.fromMemberConfirmationResult(result);

        // Then
        assertNotNull(response);
        assertEquals("", response.getMessage());
        assertEquals("temp_token_456", response.getTemporaryToken());
        assertEquals(1800, response.getExpiresIn());
    }

    @Test
    void fromMemberConfirmationResult_WithZeroExpiration_ShouldMapZeroExpiration() {
        // Given
        MembershipConfirmationResult result = MembershipConfirmationResult.builder()
                .message("Token expiré")
                .temporaryToken("expired_token")
                .expiresInSeconds(0)
                .build();

        // When
        ConfirmationResponse response = mapper.fromMemberConfirmationResult(result);

        // Then
        assertNotNull(response);
        assertEquals("Token expiré", response.getMessage());
        assertEquals("expired_token", response.getTemporaryToken());
        assertEquals(0, response.getExpiresIn());
    }
}
