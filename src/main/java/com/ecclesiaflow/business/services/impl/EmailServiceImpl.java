package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implémentation du service d'envoi d'emails
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${ecclesiaflow.mail.from:noreply@ecclesiaflow.com}")
    private String fromEmail;

    @Value("${ecclesiaflow.app.name:EcclesiaFlow}")
    private String appName;

    @Override
    public void sendConfirmationCode(String email, String code, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Code de confirmation - " + appName);
            message.setText(buildConfirmationEmailText(code, firstName));

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Impossible d'envoyer l'email de confirmation");
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Bienvenue dans " + appName);
            message.setText(buildWelcomeEmailText(firstName));

            mailSender.send(message);

        } catch (Exception e) {
            // Ne pas lancer d'exception pour l'email de bienvenue
        }
    }



    private String buildConfirmationEmailText(String code, String firstName) {
        return String.format("""
                Bonjour %s,
                
                Bienvenue dans %s !
                
                Pour confirmer votre inscription, veuillez utiliser le code de confirmation suivant :
                
                %s
                
                Ce code est valable pendant 24 heures.
                
                Si vous n'avez pas créé de compte, vous pouvez ignorer cet email.
                
                Cordialement,
                L'équipe %s
                """, firstName, appName, code, appName);
    }

    private String buildWelcomeEmailText(String firstName) {
        return String.format("""
                Bonjour %s,
                
                Votre compte %s a été créé avec succès !
                
                Vous pouvez maintenant vous connecter et profiter de toutes les fonctionnalités.
                
                Cordialement,
                L'équipe %s
                """, firstName, appName, appName);
    }


}