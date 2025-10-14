package com.ecclesiaflow.business.domain.confirmation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemberConfirmationTest {

    private MemberConfirmation createConfirmation(LocalDateTime expiresAt, UUID token) {
        return MemberConfirmation.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
    }

    @Test
    void testIsExpired_WhenNotExpired_ShouldReturnFalse() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertFalse(confirmation.isExpired());
    }

    @Test
    void testIsExpired_WhenExpired_ShouldReturnTrue() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusMinutes(1), token);
        assertTrue(confirmation.isExpired());
    }

    @Test
    void testIsValidToken_WhenCorrect_ShouldReturnTrue() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertTrue(confirmation.isValidToken(token));
    }

    @Test
    void testIsValidToken_WhenIncorrect_ShouldReturnFalse() {
        UUID token = UUID.randomUUID();
        UUID wrongToken = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertFalse(confirmation.isValidToken(wrongToken));
    }

    @Test
    void testIsValidToken_WhenNull_ShouldThrowException() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertThrows(IllegalArgumentException.class, () -> confirmation.isValidToken(null));
    }

    @Test
    void testIsValid_WhenValidTokenAndNotExpired_ShouldReturnTrue() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertTrue(confirmation.isValid(token));
    }

    @Test
    void testIsValid_WhenExpired_ShouldReturnFalse() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusHours(1), token);
        assertFalse(confirmation.isValid(token));
    }

    @Test
    void testIsValid_WhenWrongToken_ShouldReturnFalse() {
        UUID token = UUID.randomUUID();
        UUID wrongToken = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusHours(1), token);
        assertFalse(confirmation.isValid(wrongToken));
    }

    @Test
    void testGetMinutesUntilExpiration_WhenNotExpired_ShouldReturnPositive() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().plusMinutes(10), token);
        long minutes = confirmation.getMinutesUntilExpiration();
        assertTrue(minutes > 0 && minutes <= 10);
    }

    @Test
    void testGetMinutesUntilExpiration_WhenExpired_ShouldReturnZero() {
        UUID token = UUID.randomUUID();
        MemberConfirmation confirmation = createConfirmation(LocalDateTime.now().minusMinutes(5), token);
        assertEquals(0, confirmation.getMinutesUntilExpiration());
    }
}
