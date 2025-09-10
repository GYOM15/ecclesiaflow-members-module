package com.ecclesiaflow.web.mappers.web;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.web.dto.ConfirmationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ConfirmationRequestMapper.
 * Vérifie la conversion des DTOs de confirmation vers les objets métier.
 */
class ConfirmationRequestMapperTest {

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
        ConfirmationRequest request = new ConfirmationRequest();
        request.setCode("123456");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("123456", result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithNullRequest_ShouldCreateObjectWithEmptyCode() {
        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, null);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("", result.getConfirmationCode());
    }

    @Test
    void fromConfirmationRequest_WithEmptyCode_ShouldMapEmptyCode() {
        // Given
        ConfirmationRequest request = new ConfirmationRequest();
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
        ConfirmationRequest request = new ConfirmationRequest();
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
        ConfirmationRequest request = new ConfirmationRequest();
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
        ConfirmationRequest request = new ConfirmationRequest();
        request.setCode("ABC123");

        // When
        MembershipConfirmation result = mapper.fromConfirmationRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("ABC123", result.getConfirmationCode());
    }
}
