package com.ecclesiaflow.io.communication.email;

import com.ecclesiaflow.web.exception.ConfirmationEmailException;
import com.ecclesiaflow.web.exception.WelcomeEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils; // For injecting @Value fields

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    // Values that would normally be injected by Spring @Value
    private String testFromEmail = "noreply@testapp.com";
    private String testAppName = "TestApp";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Manually inject @Value fields using ReflectionTestUtils for unit tests
        ReflectionTestUtils.setField(emailService, "fromEmail", testFromEmail);
        ReflectionTestUtils.setField(emailService, "appName", testAppName);
    }

    @Test
    void sendConfirmationCode_shouldSendCorrectEmail() throws ConfirmationEmailException {
        String recipientEmail = "test@example.com";
        String code = "123456";
        String firstName = "John";

        // Create an ArgumentCaptor to capture the SimpleMailMessage sent to mailSender.send()
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Configure the mock mailSender to do nothing when send is called (success scenario)
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendConfirmationCode(recipientEmail, code, firstName);

        // Verify that mailSender.send() was called exactly once
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Get the captured message
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Assert the properties of the sent message
        assertThat(sentMessage.getFrom()).isEqualTo(testFromEmail);
        assertThat(sentMessage.getTo()).containsExactly(recipientEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("Code de confirmation - " + testAppName);
        assertThat(sentMessage.getText())
                .contains("Bonjour John,")
                .contains("Bienvenue dans " + testAppName + " !")
                .contains(code)
                .contains("Ce code est valable pendant 24 heures.")
                .contains("L'équipe " + testAppName);
    }

    @Test
    void sendConfirmationCode_shouldThrowConfirmationEmailExceptionOnError() {
        String recipientEmail = "error@example.com";
        String code = "654321";
        String firstName = "Jane";

        // Configure the mock mailSender to throw an exception when send is called
        doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(SimpleMailMessage.class));

        // Verify that ConfirmationEmailException is thrown
        ConfirmationEmailException exception = assertThrows(ConfirmationEmailException.class, () ->
                emailService.sendConfirmationCode(recipientEmail, code, firstName)
        );

        // Assert the message of the thrown exception
        assertThat(exception.getMessage()).isEqualTo("Impossible d'envoyer l'email de confirmation");
        assertThat(exception.getCause()).isInstanceOf(MailSendException.class);

        // Verify that mailSender.send() was attempted
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendWelcomeEmail_shouldSendCorrectEmail() throws WelcomeEmailException {
        String recipientEmail = "welcome@example.com";
        String firstName = "Alice";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendWelcomeEmail(recipientEmail, firstName);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getFrom()).isEqualTo(testFromEmail);
        assertThat(sentMessage.getTo()).containsExactly(recipientEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("Bienvenue dans " + testAppName);
        assertThat(sentMessage.getText())
                .contains("Bonjour Alice,")
                .contains("Votre compte " + testAppName + " a été créé avec succès !")
                .contains("L'équipe " + testAppName);
    }

    @Test
    void sendWelcomeEmail_shouldThrowWelcomeEmailExceptionOnError() {
        String recipientEmail = "error_welcome@example.com";
        String firstName = "Bob";

        doThrow(new MailSendException("Failed to send welcome email")).when(mailSender).send(any(SimpleMailMessage.class));

        // Verify that WelcomeEmailException is thrown
        WelcomeEmailException exception = assertThrows(WelcomeEmailException.class, () ->
                emailService.sendWelcomeEmail(recipientEmail, firstName)
        );

        // Assert the message of the thrown exception
        assertThat(exception.getMessage()).isEqualTo("Impossible d'envoyer l'email de bienvenue");
        assertThat(exception.getCause()).isInstanceOf(MailSendException.class);

        // Verify that mailSender.send() was attempted
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}