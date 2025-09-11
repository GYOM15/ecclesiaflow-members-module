package com.ecclesiaflow.application.code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils; // For injecting private fields

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConfirmationCodeGeneratorTest {

    @Mock
    private Random mockRandom;

    @InjectMocks
    private ConfirmationCodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(codeGenerator, "random", mockRandom);
    }

    @Test
    void generateCode_shouldReturn6DigitStringWithLeadingZeros() {
        // Arrange: Make mockRandom return a small number (e.g., 123)
        when(mockRandom.nextInt(1000000)).thenReturn(123);

        // Act
        String code = codeGenerator.generateCode();

        // Assert
        assertThat(code).isEqualTo("000123");
        assertThat(code).hasSize(6);
        verify(mockRandom, times(1)).nextInt(1000000);
    }

    @Test
    void generateCode_shouldReturn6DigitStringWithNoLeadingZerosNeeded() {
        // Arrange: Make mockRandom return a large number (e.g., 987654)
        when(mockRandom.nextInt(1000000)).thenReturn(987654);

        // Act
        String code = codeGenerator.generateCode();

        // Assert
        assertThat(code).isEqualTo("987654");
        assertThat(code).hasSize(6);
        verify(mockRandom, times(1)).nextInt(1000000);
    }

    @Test
    void generateCode_shouldReturn6DigitStringForZero() {
        // Arrange: Make mockRandom return 0
        when(mockRandom.nextInt(1000000)).thenReturn(0);

        // Act
        String code = codeGenerator.generateCode();

        // Assert
        assertThat(code).isEqualTo("000000");
        assertThat(code).hasSize(6);
        verify(mockRandom, times(1)).nextInt(1000000);
    }

    @Test
    void generateCode_shouldReturn6DigitStringForMaxValue() {
        // Arrange: Make mockRandom return the maximum possible value (999999)
        when(mockRandom.nextInt(1000000)).thenReturn(999999);

        // Act
        String code = codeGenerator.generateCode();

        // Assert
        assertThat(code).isEqualTo("999999");
        assertThat(code).hasSize(6);
        verify(mockRandom, times(1)).nextInt(1000000);
    }

    @Test
    void generateCode_shouldProduceDifferentCodesOverMultipleCalls_withRealRandom() {

        // Re-initialize codeGenerator with a real Random for this specific test
        ConfirmationCodeGenerator realCodeGenerator = new ConfirmationCodeGenerator();

        Set<String> generatedCodes = new HashSet<>();
        int numberOfCodesToGenerate = 1000; // Generate a good number of codes

        for (int i = 0; i < numberOfCodesToGenerate; i++) {
            String code = realCodeGenerator.generateCode();
            assertThat(code).matches("\\d{6}");
            generatedCodes.add(code);
        }

        assertThat(generatedCodes.size()).isGreaterThan(numberOfCodesToGenerate / 2);
        assertThat(generatedCodes.size()).isBetween(950, 1000);
    }
}