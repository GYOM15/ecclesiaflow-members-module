package com.ecclesiaflow.io.notification.email;

import com.ecclesiaflow.business.domain.communication.EmailService;
import com.ecclesiaflow.io.exception.ConfirmationEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EmailConfirmationNotifierTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailConfirmationNotifier emailConfirmationNotifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendConfirmationLink_shouldCallEmailServiceWithConfirmationUrl() throws ConfirmationEmailException {
        // Arrange
        String email = "test@example.com";
        UUID token = UUID.randomUUID();
        String firstName = "John";

        // Configure the mock EmailService to return a completed CompletableFuture when sendConfirmationCode is called
        when(emailService.sendConfirmationCode(eq(email), anyString(), eq(firstName)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        emailConfirmationNotifier.sendConfirmationLink(email, token, firstName);

        // Assert
        // Verify that emailService.sendConfirmationCode was called exactly once with a URL containing the token
        verify(emailService, times(1)).sendConfirmationCode(eq(email), contains(token.toString()), eq(firstName));
    }

    @Test
    void sendConfirmationLink_shouldPropagateConfirmationEmailException() {
        // Arrange
        String email = "error@example.com";
        UUID token = UUID.randomUUID();
        String firstName = "Jane";
        // Create an instance of the specific exception
        ConfirmationEmailException expectedException = new ConfirmationEmailException("Email sending failed", new RuntimeException("Mock Mail Error"));

        // Configure the mock EmailService to throw this specific exception
        when(emailService.sendConfirmationCode(eq(email), anyString(), eq(firstName)))
                .thenThrow(expectedException);

        // Act & Assert
        // Verify that the exception thrown by EmailService is propagated by EmailConfirmationNotifier
        ConfirmationEmailException actualException = assertThrows(ConfirmationEmailException.class, () ->
                emailConfirmationNotifier.sendConfirmationLink(email, token, firstName)
        );

        // Assert that the propagated exception is the same instance or has the same message and cause
        assertThat(actualException).isSameAs(expectedException); // Use isSameAs for instance equality

        // Verify that emailService.sendConfirmationCode was called exactly once
        verify(emailService, times(1)).sendConfirmationCode(eq(email), anyString(), eq(firstName));
    }
}