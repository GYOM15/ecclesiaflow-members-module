package com.ecclesiaflow.io.communication.email;

import com.ecclesiaflow.business.domain.communication.EmailService;
import com.ecclesiaflow.web.exception.ConfirmationEmailException;
import com.ecclesiaflow.web.exception.WelcomeEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implémentation complète du service d'envoi d'emails pour EcclesiaFlow.
 * <p>
 * Cette classe implémente l'interface {@link EmailService} et fournit toutes les
 * fonctionnalités d'envoi d'emails : codes de confirmation, emails de bienvenue,
 * et réinitialisation de mot de passe. Utilise Spring Mail avec JavaMailSender
 * pour l'envoi effectif des emails.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Communication par email</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Envoi d'emails de confirmation avec codes à 6 chiffres</li>
 *   <li>Envoi d'emails de bienvenue après confirmation</li>
 *   <li>Envoi d'emails de réinitialisation de mot de passe</li>
 *   <li>Construction des contenus d'emails personnalisés</li>
 *   <li>Gestion des erreurs d'envoi avec exceptions spécifiques</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link JavaMailSender} - Service Spring Mail pour l'envoi effectif</li>
 *   <li>Configuration SMTP via application.properties</li>
 * </ul>
 * 
 * <p><strong>Configuration requise :</strong></p>
 * <ul>
 *   <li>ecclesiaflow.mail.from - Adresse email expéditeur</li>
 *   <li>ecclesiaflow.app.name - Nom de l'application dans les emails</li>
 *   <li>Configuration SMTP (host, port, auth, etc.)</li>
 * </ul>
 * 
 * <p><strong>Gestion d'erreurs :</strong> Toute erreur d'envoi est encapsulée dans
 * des exceptions métier spécifiques pour permettre un traitement approprié.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, gestion d'erreurs robuste, templates personnalisables.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailService
 * @see JavaMailSender
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
    public void sendConfirmationCode(String email, String code, String firstName) throws ConfirmationEmailException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Code de confirmation - " + appName);
            message.setText(buildConfirmationEmailText(code, firstName));
            mailSender.send(message);

        } catch (Exception e) {
            throw new ConfirmationEmailException("Impossible d'envoyer l'email de confirmation",e);
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String firstName) throws WelcomeEmailException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Bienvenue dans " + appName);
            message.setText(buildWelcomeEmailText(firstName));

            mailSender.send(message);
        } catch (Exception e) {
            throw new WelcomeEmailException("Impossible d'envoyer l'email de bienvenue", e);
        }
    }

    /**
     * Construit le contenu textuel de l'email de confirmation.
     * <p>
     * Génère un message personnalisé contenant le code de confirmation,
     * les instructions d'utilisation, et la durée de validité (24h).
     * Utilise les text blocks Java pour une meilleure lisibilité.
     * </p>
     * 
     * @param code le code de confirmation à 6 chiffres, non null
     * @param firstName le prénom du destinataire pour personnalisation, non null
     * @return le contenu complet de l'email formaté
     */
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

    /**
     * Construit le contenu textuel de l'email de bienvenue.
     * <p>
     * Génère un message de félicitations après confirmation réussie du compte.
     * Confirme la création du compte et encourage l'utilisation de l'application.
     * </p>
     * 
     * @param firstName le prénom du nouveau membre confirmé, non null
     * @return le contenu complet de l'email de bienvenue formaté
     */
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