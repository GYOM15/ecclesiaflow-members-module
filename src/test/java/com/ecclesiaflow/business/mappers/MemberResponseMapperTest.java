package com.ecclesiaflow.business.mappers;

import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.entities.Role;
import com.ecclesiaflow.web.dto.MemberResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MemberResponseMapper.
 * Vérifie la conversion des entités Member vers les DTOs de réponse.
 */
class MemberResponseMapperTest {

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(UUID.randomUUID());
        testMember.setMemberId(UUID.randomUUID());
        testMember.setFirstName("Jean");
        testMember.setLastName("Dupont");
        testMember.setEmail("jean.dupont@example.com");
        testMember.setAddress("123 Rue de la Paix");
        testMember.setRole(Role.MEMBER);
        testMember.setConfirmed(true);
        testMember.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    @Test
    void fromMember_WithTokenAndMessage_ShouldMapCorrectly() {
        // Given
        String message = "Connexion réussie";
        String token = "jwt_token_123";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message, token);

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
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message);

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
        testMember.setRole(null);
        String message = "Test message";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message);

        // Then
        assertNotNull(response);
        assertEquals("UNKNOWN", response.getRole());
    }

    @Test
    void fromMember_WithUnconfirmedMember_ShouldMapConfirmedFalse() {
        // Given
        testMember.setConfirmed(false);
        String message = "Membre non confirmé";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message);

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
        testMember.setRole(Role.ADMIN);
        String message = "Admin connecté";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message);

        // Then
        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void fromMember_WithEmptyFields_ShouldMapEmptyValues() {
        // Given
        testMember.setFirstName("");
        testMember.setLastName("");
        testMember.setEmail("");
        testMember.setAddress("");
        String message = "Test avec champs vides";

        // When
        MemberResponse response = MemberResponseMapper.fromMember(testMember, message);

        // Then
        assertNotNull(response);
        assertEquals("", response.getFirstName());
        assertEquals("", response.getLastName());
        assertEquals("", response.getEmail());
        assertEquals("", response.getAddress());
        assertEquals("", response.getUsername());
    }
}
