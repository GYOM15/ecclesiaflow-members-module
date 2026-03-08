package com.ecclesiaflow.application.events;

import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import com.ecclesiaflow.business.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour la fonctionnalité @TransactionalEventListener.
 * <p>
 * Ces tests vérifient que:
 * 1. L'événement MemberRegisteredEvent est correctement construit
 * 2. Les données sont correctement encapsulées
 * 3. Les getters fonctionnent correctement
 * </p>
 * 
 * <p><strong>Note:</strong> Les tests du comportement transactionnel complet
 * (@TransactionalEventListener, @Async) sont effectués via les tests de
 * {@code MemberConfirmationServiceImpl} qui utilisent des mocks.</p>
 */
class MemberRegistrationEventIntegrationTest {

    private Member testMember;
    private UUID testToken;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(UUID.randomUUID())
            .memberId(UUID.randomUUID())
            .firstName("Jean")
            .email("jean@test.com")
            .build();
        
        testToken = UUID.randomUUID();
    }

    @Test
    void shouldExtractCorrectDataFromEvent() {
        // given
        String email = "member@church.com";
        UUID token = UUID.randomUUID();
        String firstName = "Marie";
        
        // when
        MemberRegisteredEvent event = new MemberRegisteredEvent(email, token, firstName);
        
        // then
        assertEquals(email, event.email());
        assertEquals(token, event.confirmationToken());
        assertEquals(firstName, event.firstName());
    }

    @Test
    void shouldCreateEventWithAllRequiredData() {
        // when
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            testMember.getEmail(),
            testToken,
            testMember.getFirstName()
        );
        
        // then
        assertNotNull(event);
        assertNotNull(event.email());
        assertNotNull(event.confirmationToken());
        assertNotNull(event.firstName());
    }

    @Test
    void shouldCreateEventFromMember() {
        // when
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            testMember.getEmail(),
            testToken,
            testMember.getFirstName()
        );
        
        // then
        assertEquals(testMember.getEmail(), event.email());
        assertEquals(testToken, event.confirmationToken());
        assertEquals(testMember.getFirstName(), event.firstName());
    }

    @Test
    void shouldHandleDifferentEmailFormats() {
        // given
        String[] testEmails = {
            "simple@example.com",
            "firstname.lastname@example.com",
            "email+tag@example.co.uk",
            "test_user@sub.domain.example.com"
        };
        
        // when/then
        for (String email : testEmails) {
            MemberRegisteredEvent event = new MemberRegisteredEvent(
                email,
                UUID.randomUUID(),
                "Test"
            );
            assertEquals(email, event.email());
        }
    }
}
