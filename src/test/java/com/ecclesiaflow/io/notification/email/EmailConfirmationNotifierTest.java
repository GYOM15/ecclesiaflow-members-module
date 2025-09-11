package com.ecclesiaflow.io.notification.email;

import com.ecclesiaflow.business.domain.communication.EmailService;
import com.ecclesiaflow.io.exception.ConfirmationEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void sendCode_shouldCallEmailServiceSendConfirmationCode() throws ConfirmationEmailException {
        // Arrange
        String email = "test@example.com";
        String code = "123456";
        String firstName = "John";

        // Configure the mock EmailService to do nothing when sendConfirmationCode is called
        doNothing().when(emailService).sendConfirmationCode(email, code, firstName);

        // Act
        emailConfirmationNotifier.sendCode(email, code, firstName);

        // Assert
        // Verify that emailService.sendConfirmationCode was called exactly once with the correct arguments
        verify(emailService, times(1)).sendConfirmationCode(email, code, firstName);
    }

    @Test
    void sendCode_shouldPropagateConfirmationEmailException() {
        // Arrange
        String email = "error@example.com";
        String code = "654321";
        String firstName = "Jane";
        // Create an instance of the specific exception
        ConfirmationEmailException expectedException = new ConfirmationEmailException("Email sending failed", new RuntimeException("Mock Mail Error"));

        // Configure the mock EmailService to throw this specific exception
        doThrow(expectedException).when(emailService).sendConfirmationCode(email, code, firstName);

        // Act & Assert
        // Verify that the exception thrown by EmailService is propagated by EmailConfirmationNotifier
        ConfirmationEmailException actualException = assertThrows(ConfirmationEmailException.class, () ->
                emailConfirmationNotifier.sendCode(email, code, firstName)
        );

        // Assert that the propagated exception is the same instance or has the same message and cause
        assertThat(actualException).isSameAs(expectedException); // Use isSameAs for instance equality


        // Verify that emailService.sendConfirmationCode was called exactly once
        verify(emailService, times(1)).sendConfirmationCode(email, code, firstName);
    }
}