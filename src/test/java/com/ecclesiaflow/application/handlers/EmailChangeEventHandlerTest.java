package com.ecclesiaflow.application.handlers;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.events.EmailChangedEvent;
import com.ecclesiaflow.business.domain.events.PendingEmailChangeRequestedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChangeEventHandlerTest {

    @Mock private EmailClient emailClient;

    @InjectMocks
    private EmailChangeEventHandler handler;

    @Test
    void handleEmailChangeRequested_sendsConfirmationEmail() {
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", "https://app.ecclesiaflow.com");
        UUID token = UUID.randomUUID();
        PendingEmailChangeRequestedEvent event = new PendingEmailChangeRequestedEvent("new@example.com", token, "Jean");

        handler.handleEmailChangeRequested(event);

        String expectedUrl = "https://app.ecclesiaflow.com/email-change-confirmation?token=" + token;
        verify(emailClient).sendConfirmationEmail("new@example.com", expectedUrl, "Jean");
    }

    @Test
    void handleEmailChangeRequested_emailClientFailure_doesNotThrow() {
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", "https://app.ecclesiaflow.com");
        UUID token = UUID.randomUUID();
        PendingEmailChangeRequestedEvent event = new PendingEmailChangeRequestedEvent("new@example.com", token, "Jean");
        doThrow(new RuntimeException("gRPC down")).when(emailClient).sendConfirmationEmail(any(), any(), any());

        // Should not throw
        handler.handleEmailChangeRequested(event);
    }

    @Test
    void handleEmailChanged_sendsNotification() {
        EmailChangedEvent event = new EmailChangedEvent("old@example.com", "Jean");

        handler.handleEmailChanged(event);

        verify(emailClient).sendEmailChangedNotification("old@example.com", "Jean");
    }

    @Test
    void handleEmailChanged_emailClientFailure_doesNotThrow() {
        EmailChangedEvent event = new EmailChangedEvent("old@example.com", "Jean");
        doThrow(new RuntimeException("gRPC down")).when(emailClient).sendEmailChangedNotification(any(), any());

        // Should not throw
        handler.handleEmailChanged(event);
    }
}
