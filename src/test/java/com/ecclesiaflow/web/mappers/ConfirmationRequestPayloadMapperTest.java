package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.web.payloads.ConfirmationRequestPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ConfirmationRequestMapper.
 * Vérifie la conversion des DTOs de confirmation vers les objets métier.
 */
class ConfirmationRequestPayloadMapperTest {

    private ConfirmationRequestMapper mapper;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        mapper = new ConfirmationRequestMapper();
        testMemberId = UUID.randomUUID();
    }

    @Test
    void fromConfirmationRequest_WithValidData_ShouldMapCorrectly() {
        // Given
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("123456");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("123456", result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithNullRequest_ShouldThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            mapper.fromConfirmationRequest(testMemberId, null);
        });
    }

    @Test
    void fromConfirmationRequest_WithEmptyCode_ShouldMapEmptyCode() {
        // Given
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("", result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithNullCode_ShouldMapNullCode() {
        // Given
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode(null);

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertNull(result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithSixDigitCode_ShouldMapCorrectly() {
        // Given
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("940155");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("940155", result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithAlphaNumericCode_ShouldMapCorrectly() {
        // Given
        ConfirmationRequestPayload request = new ConfirmationRequestPayload();
        request.setCode("ABC123");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("ABC123", result.getConfirmationCode());
    }
}
