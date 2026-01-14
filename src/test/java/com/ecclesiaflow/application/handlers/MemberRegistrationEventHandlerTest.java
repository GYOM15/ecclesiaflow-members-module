package com.ecclesiaflow.application.handlers;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@DisplayName("MemberRegistrationEventHandler")
class MemberRegistrationEventHandlerTest {

    @Test
    @DisplayName("should send confirmation email with built confirmation URL")
    void shouldSendConfirmationEmail() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        MemberRegistrationEventHandler handler = new MemberRegistrationEventHandler(emailClient);
        ReflectionTestUtils.setField(handler, "baseUrl", "http://localhost:8080");

        UUID token = UUID.randomUUID();
        MemberRegisteredEvent event = new MemberRegisteredEvent("user@example.com", token, "John");

        assertThatNoException().isThrownBy(() -> handler.handleMemberRegistered(event));

        String expectedUrl = "http://localhost:8080/ecclesiaflow/members/confirmation?token=" + token;
        verify(emailClient).sendConfirmationEmail(eq("user@example.com"), eq(expectedUrl), eq("John"));
    }

    @Test
    @DisplayName("should swallow exceptions from EmailClient and not rethrow")
    void shouldSwallowEmailClientExceptions() {
        EmailClient emailClient = Mockito.mock(EmailClient.class);
        MemberRegistrationEventHandler handler = new MemberRegistrationEventHandler(emailClient);
        ReflectionTestUtils.setField(handler, "baseUrl", "http://localhost:8080");

        UUID token = UUID.randomUUID();
        MemberRegisteredEvent event = new MemberRegisteredEvent("user@example.com", token, "John");

        doThrow(new RuntimeException("service down")).when(emailClient)
                .sendConfirmationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        assertThatNoException().isThrownBy(() -> handler.handleMemberRegistered(event));
        // call attempted even though it fails
        verify(emailClient).sendConfirmationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
