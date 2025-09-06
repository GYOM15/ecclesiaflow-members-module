package com.ecclesiaflow.web.mappers.persistence;

import com.ecclesiaflow.business.domain.MembershipRegistration;
import com.ecclesiaflow.web.dto.SignUpRequest;
import com.ecclesiaflow.web.mappers.web.SignUpRequestMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SignUpRequestMapper.
 * Vérifie la conversion correcte des DTOs vers les objets métier.
 */
class SignUpRequestMapperTest {

    @Test
    void fromSignUpRequest_WithValidData_ShouldMapCorrectly() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean.dupont@example.com");
        request.setAddress("123 Rue de la Paix, Paris");

        // When
        MembershipRegistration result = SignUpRequestMapper.fromSignUpRequest(request);

        // Then
        assertNotNull(result);
        assertEquals("Jean", result.firstName());
        assertEquals("Dupont", result.lastName());
        assertEquals("jean.dupont@example.com", result.email());
        assertEquals("123 Rue de la Paix, Paris", result.address());
    }

    @Test
    void fromSignUpRequest_WithNullRequest_ShouldThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            SignUpRequestMapper.fromSignUpRequest(null));
    }

    @Test
    void fromSignUpRequest_WithEmptyFields_ShouldMapEmptyValues() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setFirstName("");
        request.setLastName("");
        request.setEmail("");
        request.setAddress("");

        // When
        MembershipRegistration result = SignUpRequestMapper.fromSignUpRequest(request);

        // Then
        assertNotNull(result);
        assertEquals("", result.firstName());
        assertEquals("", result.lastName());
        assertEquals("", result.email());
        assertEquals("", result.address());
    }

    @Test
    void fromSignUpRequest_WithNullFields_ShouldMapNullValues() {
        // Given
        SignUpRequest request = new SignUpRequest();
        // Tous les champs restent null par défaut

        // When
        MembershipRegistration result = SignUpRequestMapper.fromSignUpRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.firstName());
        assertNull(result.lastName());
        assertNull(result.email());
        assertNull(result.address());
    }

    @Test
    void fromSignUpRequest_WithSpecialCharacters_ShouldMapCorrectly() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setFirstName("Jean-François");
        request.setLastName("O'Connor");
        request.setEmail("jean.françois@église.com");
        request.setAddress("123 Rue de l'Église, Montréal, QC");

        // When
        MembershipRegistration result = SignUpRequestMapper.fromSignUpRequest(request);

        // Then
        assertNotNull(result);
        assertEquals("Jean-François", result.firstName());
        assertEquals("O'Connor", result.lastName());
        assertEquals("jean.françois@église.com", result.email());
        assertEquals("123 Rue de l'Église, Montréal, QC", result.address());
    }
}
