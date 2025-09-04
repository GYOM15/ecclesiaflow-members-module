package com.ecclesiaflow.web.mappers.web;

import com.ecclesiaflow.io.entities.MemberEntity;
import com.ecclesiaflow.io.entities.Role;
import com.ecclesiaflow.web.dto.MemberResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MemberResponseMapper.
 * Vérifie la conversion des entités MemberEntity vers les DTOs de réponse.
 */
class MemberEntityResponseMapperTest {

    private MemberEntity testMemberEntity;

    @BeforeEach
    void setUp() {
        testMemberEntity = new MemberEntity();
        testMemberEntity.setId(UUID.randomUUID());
        testMemberEntity.setMemberId(UUID.randomUUID());
        testMemberEntity.setFirstName("Jean");
        testMemberEntity.setLastName("Dupont");
        testMemberEntity.setEmail("jean.dupont@example.com");
        testMemberEntity.setAddress("123 Rue de la Paix");
        testMemberEntity.setRole(Role.MEMBER);
        testMemberEntity.setConfirmed(true);
        testMemberEntity.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    @Test
    void fromMember_WithTokenAndMessage_ShouldMapCorrectly() {
        // Given
        String message = "Connexion réussie";
        String token = "jwt_token_123";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message, token);

        // Then
        assertNotNull(response);
        assertEquals(message, response.getMessage());
        assertEquals("Jean", response.getFirstName());
        assertEquals("Dupont", response.getLastName());
        assertEquals("jean.dupont@example.com", response.getEmail());
        assertEquals("123 Rue de la Paix", response.getAddress());
        assertEquals("MEMBER", response.getRole());
        assertEquals("jean.dupont@example.com", response.getUsername());
        assertEquals(token, response.getToken());
        assertTrue(response.isConfirmed());
        assertEquals("2024-01-15T10:30", response.getCreatedAt());
    }

    @Test
    void fromMember_WithMessageOnly_ShouldMapWithNullToken() {
        // Given
        String message = "Profil récupéré";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message);

        // Then
        assertNotNull(response);
        assertEquals(message, response.getMessage());
        assertEquals("Jean", response.getFirstName());
        assertEquals("Dupont", response.getLastName());
        assertEquals("jean.dupont@example.com", response.getEmail());
        assertEquals("123 Rue de la Paix", response.getAddress());
        assertEquals("MEMBER", response.getRole());
        assertEquals("jean.dupont@example.com", response.getUsername());
        assertNull(response.getToken());
        assertTrue(response.isConfirmed());
        assertEquals("2024-01-15T10:30", response.getCreatedAt());
    }

    @Test
    void fromMember_WithNullRole_ShouldMapToUnknown() {
        // Given
        testMemberEntity.setRole(null);
        String message = "Test message";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message);

        // Then
        assertNotNull(response);
        assertEquals("UNKNOWN", response.getRole());
    }

    @Test
    void fromMember_WithUnconfirmedMember_ShouldMapConfirmedFalse() {
        // Given
        testMemberEntity.setConfirmed(false);
        String message = "Membre non confirmé";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message);

        // Then
        assertNotNull(response);
        assertFalse(response.isConfirmed());
    }

    @Test
    void fromMember_WithNullMember_ShouldThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            MemberResponseMapper.fromMember(null, "Test message"));
    }

    @Test
    void fromMember_WithAdminRole_ShouldMapAdminRole() {
        // Given
        testMemberEntity.setRole(Role.ADMIN);
        String message = "Admin connecté";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message);

        // Then
        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void fromMember_WithEmptyFields_ShouldMapEmptyValues() {
        // Given
        testMemberEntity.setFirstName("");
        testMemberEntity.setLastName("");
        testMemberEntity.setEmail("");
        testMemberEntity.setAddress("");
        String message = "Test avec champs vides";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMemberEntity, message);

        // Then
        assertNotNull(response);
        assertEquals("", response.getFirstName());
        assertEquals("", response.getLastName());
        assertEquals("", response.getEmail());
        assertEquals("", response.getAddress());
        assertEquals("", response.getUsername());
    }
}
