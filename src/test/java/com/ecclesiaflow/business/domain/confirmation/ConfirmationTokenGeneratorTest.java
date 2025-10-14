package com.ecclesiaflow.business.domain.confirmation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConfirmationTokenGeneratorTest {

    private ConfirmationTokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = new ConfirmationTokenGenerator();
    }

    @Test
    void generateToken_shouldReturnValidUUID() {
        // Act
        UUID token = tokenGenerator.generateToken();

        // Assert
        assertThat(token).isNotNull();
        assertThat(token.toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void generateToken_shouldGenerateRandomVersion4UUID() {
        // Act
        UUID token = tokenGenerator.generateToken();

        // Assert - UUID version 4 has specific characteristics
        assertThat(token).isNotNull();
        assertThat(token.version()).isEqualTo(4); // UUID v4 (random)
        assertThat(token.variant()).isEqualTo(2); // RFC 4122 variant
    }

    @Test
    void generateToken_shouldGenerateUniqueTokensOnMultipleCalls() {
        // Arrange
        Set<UUID> generatedTokens = new HashSet<>();
        int numberOfTokensToGenerate = 10000;

        // Act
        for (int i = 0; i < numberOfTokensToGenerate; i++) {
            UUID token = tokenGenerator.generateToken();
            generatedTokens.add(token);
        }

        // Assert - All tokens should be unique
        assertThat(generatedTokens).hasSize(numberOfTokensToGenerate);
    }

    @Test
    void generateToken_shouldNotGenerateNullToken() {
        // Act & Assert
        for (int i = 0; i < 100; i++) {
            UUID token = tokenGenerator.generateToken();
            assertThat(token).isNotNull();
        }
    }

    @Test
    void generateToken_shouldGenerateDifferentTokensConsecutively() {
        // Act
        UUID token1 = tokenGenerator.generateToken();
        UUID token2 = tokenGenerator.generateToken();
        UUID token3 = tokenGenerator.generateToken();

        // Assert
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token2).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(token3);
    }
}