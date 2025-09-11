package com.ecclesiaflow.business.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .address("123 Street")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testDefaultValues_ShouldHaveRoleMemberAndNotConfirmed() {
        assertEquals(Role.MEMBER, member.getRole());
        assertFalse(member.isConfirmed());
        assertFalse(member.isPasswordSet());
    }

    @Test
    void testWithUpdatedFields_ShouldUpdateProvidedValues() {
        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(member.getMemberId())
                .firstName("Jane")
                .email("jane.doe@test.com")
                .address("456 Avenue")
                .build();

        Member updated = member.withUpdatedFields(update);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Doe", updated.getLastName()); // resté identique
        assertEquals("jane.doe@test.com", updated.getEmail());
        assertEquals("456 Avenue", updated.getAddress());
        assertNotEquals(member.getUpdatedAt(), updated.getUpdatedAt());
    }

    @Test
    void testWithUpdatedFields_WhenAllNull_ShouldKeepOriginalValues() {
        MembershipUpdate update = MembershipUpdate.builder()
                .memberId(member.getMemberId())
                .build();

        Member updated = member.withUpdatedFields(update);

        assertEquals(member.getFirstName(), updated.getFirstName());
        assertEquals(member.getLastName(), updated.getLastName());
        assertEquals(member.getEmail(), updated.getEmail());
        assertEquals(member.getAddress(), updated.getAddress());
    }

    @Test
    void testConfirm_ShouldSetConfirmedAndConfirmedAt() {
        member.confirm();

        assertTrue(member.isConfirmed());
        assertNotNull(member.getConfirmedAt());
    }

    @Test
    void testConfirm_WhenAlreadyConfirmed_ShouldThrowException() {
        member.confirm();
        assertThrows(IllegalStateException.class, member::confirm);
    }
}
