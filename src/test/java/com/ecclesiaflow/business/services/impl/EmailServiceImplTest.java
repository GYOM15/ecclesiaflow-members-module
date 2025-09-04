package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.io.email.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import com.ecclesiaflow.web.exception.ConfirmationEmailException;
import com.ecclesiaflow.web.exception.PasswordResetEmailException;
import com.ecclesiaflow.web.exception.WelcomeEmailException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl - Tests unitaires")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@ecclesiaflow.com");
        ReflectionTestUtils.setField(emailService, "appName", "EcclesiaFlow");
    }

    // Existing tests for sendConfirmationCode...
    @Test
    void sendConfirmationCode_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String confirmationCode = "123456";
        String firstName = "Jean";
        // When
        emailService.sendConfirmationCode(toEmail, confirmationCode, firstName);
        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals("noreply@ecclesiaflow.com", sentMessage.getFrom());
        assertArrayEquals(new String[]{toEmail}, sentMessage.getTo());
        assertEquals("Code de confirmation - EcclesiaFlow", sentMessage.getSubject());
        String emailText = sentMessage.getText();
        assertNotNull(emailText);
        assertTrue(emailText.contains("Bonjour Jean,"));
        assertTrue(emailText.contains("Bienvenue dans EcclesiaFlow !"));
        assertTrue(emailText.contains("Pour confirmer votre inscription, veuillez utiliser le code de confirmation suivant :"));
        assertTrue(emailText.contains("123456"));
        assertTrue(emailText.contains("Ce code est valable pendant 24 heures."));
        assertTrue(emailText.contains("L'équipe EcclesiaFlow"));
    }

    @Test
    void sendConfirmationCode_WhenMailSenderThrowsException_ShouldThrowConfirmationEmailException() {
        // Given
        String toEmail = "test@example.com";
        String confirmationCode = "123456";
        String firstName = "Jean";
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        // When & Then
        assertThrows(ConfirmationEmailException.class, () ->
                emailService.sendConfirmationCode(toEmail, confirmationCode, firstName));
    }

    // --- NOUVEAUX TESTS POUR sendWelcomeEmail ---
    @Test
    @DisplayName("Devrait envoyer un email de bienvenue avec des données valides")
    void sendWelcomeEmail_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "Pierre";

        // When
        emailService.sendWelcomeEmail(toEmail, firstName);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals("Bienvenue dans EcclesiaFlow", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Bonjour Pierre,"));
        assertTrue(sentMessage.getText().contains("Votre compte EcclesiaFlow a été créé avec succès !"));
    }

    @Test
    @DisplayName("Devrait lancer une WelcomeEmailException si l'envoi de l'email de bienvenue échoue")
    void sendWelcomeEmail_WhenMailSenderThrowsException_ShouldThrowWelcomeEmailException() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "Pierre";
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThrows(WelcomeEmailException.class, () ->
                emailService.sendWelcomeEmail(toEmail, firstName));
    }

    // --- NOUVEAUX TESTS POUR sendPasswordResetEmail ---
    @Test
    @DisplayName("Devrait envoyer un email de réinitialisation de mot de passe avec des données valides")
    void sendPasswordResetEmail_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String resetLink = "http://app.com/reset-password/token";
        String firstName = "Marie";

        // When
        emailService.sendPasswordResetEmail(toEmail, resetLink, firstName);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals("Réinitialisation de mot de passe - EcclesiaFlow", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Bonjour Marie,"));
        assertTrue(sentMessage.getText().contains("Une demande de réinitialisation de mot de passe a été effectuée pour votre compte."));
        assertTrue(sentMessage.getText().contains(resetLink));
    }

    @Test
    @DisplayName("Devrait lancer une PasswordResetEmailException si l'envoi de l'email de réinitialisation échoue")
    void sendPasswordResetEmail_WhenMailSenderThrowsException_ShouldThrowPasswordResetEmailException() {
        // Given
        String toEmail = "test@example.com";
        String resetLink = "http://app.com/reset-password/token";
        String firstName = "Marie";
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThrows(PasswordResetEmailException.class, () ->
                emailService.sendPasswordResetEmail(toEmail, resetLink, firstName));
    }
}