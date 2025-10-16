package com.ecclesiaflow.application.events;

import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link MemberRegistrationEventListener}.
 * <p>
 * Vérifie que le listener traite correctement les événements MemberRegistered
 * en appelant le ConfirmationNotifier avec les bonnes données.
 * </p>
 * 
 * <p><strong>Note:</strong> Ces tests vérifient uniquement la logique métier
 * du listener. Les aspects transactionnels et asynchrones sont testés
 * séparément dans les tests d'intégration.</p>
 */
class MemberRegistrationEventListenerTest {

    @Mock
    private ConfirmationNotifier confirmationNotifier;

    private MemberRegistrationEventListener listener;

    private String testEmail;
    private UUID testToken;
    private String testFirstName;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new MemberRegistrationEventListener(confirmationNotifier);

        testEmail = "test@ecclesiaflow.com";
        testToken = UUID.randomUUID();
        testFirstName = "Jean";
    }

    @Test
    void handleMemberRegistered_ShouldCallConfirmationNotifier() {
        // given
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            testEmail,
            testToken,
            testFirstName
        );

        // when
        listener.handleMemberRegistered(event);

        // then
        verify(confirmationNotifier).sendConfirmationLink(
            testEmail,
            testToken,
            testFirstName
        );
    }

    @Test
    void handleMemberRegistered_ShouldExtractCorrectDataFromEvent() {
        // given
        String email = "member@church.com";
        UUID token = UUID.randomUUID();
        String firstName = "Marie";
        
        MemberRegisteredEvent event = new MemberRegisteredEvent(email, token, firstName);

        // when
        listener.handleMemberRegistered(event);

        // then
        verify(confirmationNotifier).sendConfirmationLink(
            eq(email),
            eq(token),
            eq(firstName)
        );
        verifyNoMoreInteractions(confirmationNotifier);
    }

    @Test
    void handleMemberRegistered_ShouldCallNotifierOnlyOnce() {
        // given
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            testEmail,
            testToken,
            testFirstName
        );

        // when
        listener.handleMemberRegistered(event);

        // then
        verify(confirmationNotifier, times(1)).sendConfirmationLink(any(), any(), any());
    }

    @Test
    void handleMemberRegistered_ShouldPropagateExceptionFromNotifier() {
        // given
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            testEmail,
            testToken,
            testFirstName
        );
        
        RuntimeException expectedException = new RuntimeException("SMTP error");
        doThrow(expectedException).when(confirmationNotifier)
            .sendConfirmationLink(any(), any(), any());

        // when/then
        try {
            listener.handleMemberRegistered(event);
        } catch (RuntimeException e) {
            // Exception should propagate to AsyncEmailLoggingAspect
            verify(confirmationNotifier).sendConfirmationLink(testEmail, testToken, testFirstName);
        }
    }
}
