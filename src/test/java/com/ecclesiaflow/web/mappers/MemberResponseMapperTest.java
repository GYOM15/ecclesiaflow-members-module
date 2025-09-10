package com.ecclesiaflow.web.mappers.web;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.Role;
import com.ecclesiaflow.web.dto.SignUpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*; // Utilisation d'AssertJ

/**
 * Tests unitaires pour MemberResponseMapper.
 * Vérifie la conversion des entités Member vers les DTOs de réponse SignUpResponse.
 */
@DisplayName("MemberResponseMapper - Tests Unitaires")
class MemberResponseMapperTest {

    private Member testMember;

    @BeforeEach
    void setUp() {
        // Initialisation de l'objet métier Member
        testMember = Member.builder()
                .id(UUID.randomUUID())
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .address("123 Rue de la Paix")
                .role(Role.MEMBER)
                .confirmed(true)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .confirmedAt(LocalDateTime.of(2024, 1, 15, 10, 35))
                .build();
    }

    @Nested
    @DisplayName("Méthode fromMember(Member member, String message, String token)")
    class FromMemberWithTokenAndMessageTests {

        @Test
        @DisplayName("Devrait mapper correctement avec un message et un token")
        void fromMember_WithTokenAndMessage_ShouldMapCorrectly() {
            // Given
            String message = "Connexion réussie";
            String token = "jwt_token_123";

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message, token);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getFirstName()).isEqualTo("Jean");
            assertThat(response.getLastName()).isEqualTo("Dupont");
            assertThat(response.getEmail()).isEqualTo("jean.dupont@example.com");
            assertThat(response.getAddress()).isEqualTo("123 Rue de la Paix");
            assertThat(response.getRole()).isEqualTo("MEMBER");
            assertThat(response.getToken()).isEqualTo(token);
            assertThat(response.isConfirmed()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo("2024-01-15T10:30");
            assertThat(response.getConfirmedAt()).isEqualTo("2024-01-15T10:35");
        }

        @Test
        @DisplayName("Devrait mapper correctement avec un token null")
        void fromMember_WithNullToken_ShouldMapCorrectly() {
            // Given
            String message = "Inscription réussie";
            String token = null;

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message, token);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getToken()).isNull();
            assertThat(response.getFirstName()).isEqualTo("Jean"); // Juste pour s'assurer que le reste est aussi mappé
        }

        @Test
        @DisplayName("Devrait lancer NullPointerException si le membre est null")
        void fromMember_WithNullMember_ShouldThrowNullPointerException() {
            // When & Then
            assertThatThrownBy(() -> MemberResponseMapper.fromMember(null, "Test message", "token"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Devrait lancer NullPointerException si le message est null")
        void fromMember_WithNullMessage_ShouldThrowNullPointerException() {
            // When & Then
            assertThatThrownBy(() -> MemberResponseMapper.fromMember(testMember, null, "token"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Méthode fromMember(Member member, String message)")
    class FromMemberWithMessageOnlyTests {

        @Test
        @DisplayName("Devrait mapper avec message seulement (token null)")
        void fromMember_WithMessageOnly_ShouldMapWithNullToken() {
            // Given
            String message = "Profil récupéré";

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getFirstName()).isEqualTo("Jean");
            assertThat(response.getLastName()).isEqualTo("Dupont");
            assertThat(response.getEmail()).isEqualTo("jean.dupont@example.com");
            assertThat(response.getAddress()).isEqualTo("123 Rue de la Paix");
            assertThat(response.getRole()).isEqualTo("MEMBER");
            assertThat(response.getToken()).isNull(); // Assurez-vous que le token est null
            assertThat(response.isConfirmed()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo("2024-01-15T10:30");
            assertThat(response.getConfirmedAt()).isEqualTo("2024-01-15T10:35");
        }

        @Test
        @DisplayName("Devrait lancer NullPointerException si le membre est null pour la surcharge message-seulement")
        void fromMember_WithMessageOnly_WithNullMember_ShouldThrowNullPointerException() {
            // When & Then
            assertThatThrownBy(() -> MemberResponseMapper.fromMember(null, "Test message"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Devrait lancer NullPointerException si le message est null pour la surcharge message-seulement")
        void fromMember_WithMessageOnly_WithNullMessage_ShouldThrowNullPointerException() {
            // When & Then
            assertThatThrownBy(() -> MemberResponseMapper.fromMember(testMember, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Tests des cas limites et valeurs par défaut")
    class EdgeCaseTests {

        @Test
        @DisplayName("Devrait mapper le rôle 'UNKNOWN' si le rôle du membre est null")
        void fromMember_WithNullRole_ShouldMapToUnknown() {
            // Given
            testMember = testMember.toBuilder()
                    .role(null)
                    .build();
            String message = "Test message";

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("Devrait mapper 'confirmed' à false si le membre n'est pas confirmé")
        void fromMember_WithUnconfirmedMember_ShouldMapConfirmedFalse() {
            // Given
            testMember = testMember.toBuilder()
                    .confirmed(false)
                    .confirmedAt(null) // confirmedAt devrait être null si non confirmé
                    .build();
            String message = "Membre non confirmé";

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isConfirmed()).isFalse();
            assertThat(response.getConfirmedAt()).isNull();
        }

        @Test
        @DisplayName("Devrait mapper les champs vides en valeurs vides")
        void fromMember_WithEmptyFields_ShouldMapEmptyValues() {
            // Given
            testMember = testMember.toBuilder()
                    .firstName("")
                    .lastName("")
                    .email("")
                    .address("")
                    .build();
            String message = "Test avec champs vides";

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, message);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getFirstName()).isEqualTo("");
            assertThat(response.getLastName()).isEqualTo("");
            assertThat(response.getEmail()).isEqualTo("");
            assertThat(response.getAddress()).isEqualTo("");
        }

        @Test
        @DisplayName("Devrait mapper null pour createdAt si la date de création est null")
        void fromMember_WithNullCreatedAt_ShouldMapNullCreatedAt() {
            // Given
            testMember = testMember.toBuilder()
                    .createdAt(null)
                    .build();

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, "Message");

            // Then
            assertThat(response.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Devrait mapper null pour confirmedAt si la date de confirmation est null")
        void fromMember_WithNullConfirmedAt_ShouldMapNullConfirmedAt() {
            // Given
            testMember = testMember.toBuilder()
                    .confirmedAt(null)
                    .build();

            // When
            SignUpResponse response = MemberResponseMapper.fromMember(testMember, "Message");

            // Then
            assertThat(response.getConfirmedAt()).isNull();
        }
    }
}