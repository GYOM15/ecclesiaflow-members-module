package com.ecclesiaflow.business.domain.confirmation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemberConfirmationTest {

    private MemberConfirmation createConfirmation(LocalDateTime expiresAt, String code) {
        return MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
    }

    @Test
    void testIsExpired_WhenNotExpired_ShouldReturnFalse() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertFalse(confirmation.isExpired());
    }

    @Test
    void testIsExpired_WhenExpired_ShouldReturnTrue() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusMinutes(1), "ABC123");
        assertTrue(confirmation.isExpired());
    }

    @Test
    void testIsValidCode_WhenCorrect_ShouldReturnTrue() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertTrue(confirmation.isValidCode("ABC123"));
    }

    @Test
    void testIsValidCode_WhenIncorrect_ShouldReturnFalse() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertFalse(confirmation.isValidCode("WRONG"));
    }

    @Test
    void testIsValidCode_WhenNull_ShouldThrowException() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertThrows(IllegalArgumentException.class, () -> confirmation.isValidCode(null));
    }

    @Test
    void testIsValid_WhenValidCodeAndNotExpired_ShouldReturnTrue() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertTrue(confirmation.isValid("ABC123"));
    }

    @Test
    void testIsValid_WhenExpired_ShouldReturnFalse() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusHours(1), "ABC123");
        assertFalse(confirmation.isValid("ABC123"));
    }

    @Test
    void testIsValid_WhenWrongCode_ShouldReturnFalse() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), "ABC123");
        assertFalse(confirmation.isValid("WRONG"));
    }

    @Test
    void testGetMinutesUntilExpiration_WhenNotExpired_ShouldReturnPositive() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusMinutes(10), "ABC123");
        long minutes = confirmation.getMinutesUntilExpiration();
        assertTrue(minutes > 0 && minutes <= 10);
    }

    @Test
    void testGetMinutesUntilExpiration_WhenExpired_ShouldReturnZero() {
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusMinutes(5), "ABC123");
        assertEquals(0, confirmation.getMinutesUntilExpiration());
    }
}
