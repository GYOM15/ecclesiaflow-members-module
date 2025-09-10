package com.ecclesiaflow.web.mappers.persistence;

import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.web.dto.UpdateMemberRequest;
import com.ecclesiaflow.web.mappers.web.UpdateRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour UpdateRequestMapper.
 * Vérifie la conversion des DTOs de mise à jour vers les objets métier.
 */
class UpdateRequestMapperTest {

    private UpdateRequestMapper mapper;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        mapper = new UpdateRequestMapper();
        testMemberId = UUID.randomUUID();
    }

    @Test
    void fromUpdateMemberRequest_WithValidData_ShouldMapCorrectly() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean.dupont@example.com");
        request.setAddress("123 Rue de la Paix");

        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("Jean", result.getFirstName());
        assertEquals("Dupont", result.getLastName());
        assertEquals("jean.dupont@example.com", result.getEmail());
        assertEquals("123 Rue de la Paix", result.getAddress());
    }

    @Test
    void fromUpdateMemberRequest_WithNullRequest_ShouldCreateObjectWithOnlyMemberId() {
        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, null);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
        assertNull(result.getAddress());
    }

    @Test
    void fromUpdateMemberRequest_WithPartialData_ShouldMapPartialData() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFirstName("Jean");
        request.setEmail("jean.nouveau@example.com");
        // lastName et address restent null

        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("Jean", result.getFirstName());
        assertNull(result.getLastName());
        assertEquals("jean.nouveau@example.com", result.getEmail());
        assertNull(result.getAddress());
    }

    @Test
    void fromUpdateMemberRequest_WithEmptyFields_ShouldMapEmptyValues() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFirstName("");
        request.setLastName("");
        request.setEmail("");
        request.setAddress("");

        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());
        assertEquals("", result.getEmail());
        assertEquals("", result.getAddress());
    }

    @Test
    void fromUpdateMemberRequest_WithOnlyFirstName_ShouldMapOnlyFirstName() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFirstName("Marie");

        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("Marie", result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
        assertNull(result.getAddress());
    }

    @Test
    void fromUpdateMemberRequest_WithSpecialCharacters_ShouldMapCorrectly() {
        // Given
        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFirstName("Jean-François");
        request.setLastName("O'Connor");
        request.setEmail("jean.françois@église.com");
        request.setAddress("123 Rue de l'Église, Montréal");

        // When
        MembershipUpdate result = mapper.fromUpdateMemberRequest(testMemberId, request);

        // Then
        assertNotNull(result);
        assertEquals(testMemberId, result.getMemberId());
        assertEquals("Jean-François", result.getFirstName());
        assertEquals("O'Connor", result.getLastName());
        assertEquals("jean.françois@église.com", result.getEmail());
        assertEquals("123 Rue de l'Église, Montréal", result.getAddress());
    }
}
