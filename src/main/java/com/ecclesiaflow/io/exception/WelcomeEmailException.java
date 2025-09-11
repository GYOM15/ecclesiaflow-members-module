package com.ecclesiaflow.io.exception;

import com.ecclesiaflow.io.communication.email.EmailServiceImpl;

/**
 * Exception spécialisée levée lors d'un échec d'envoi d'email de bienvenue.
 * <p>
 * Cette exception hérite de {@link EmailSendingException} et est spécifiquement utilisée
 * pour les erreurs d'envoi des emails de bienvenue envoyés aux membres après
 * confirmation réussie de leur inscription.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception technique spécialisée - Emails de bienvenue</p>
 * 
 * <p><strong>Contexte d'utilisation :</strong></p>
 * <ul>
 *   <li>Échec d'envoi de l'email de bienvenue après confirmation réussie</li>
 *   <li>Problèmes de template d'email de bienvenue</li>
 *   <li>Erreurs lors de la personnalisation du contenu de bienvenue</li>
 *   <li>Problèmes de formatage ou de contenu de l'email</li>
 * </ul>
 * 
 * <p><strong>Contenu de l'email concerné :</strong></p>
 * <ul>
 *   <li>Message de bienvenue personnalisé</li>
 *   <li>Informations sur les prochaines étapes</li>
 *   <li>Liens vers les ressources de l'application</li>
 *   <li>Informations de contact et support</li>
 * </ul>
 * 
 * <p><strong>Impact sur le flux utilisateur :</strong></p>
 * <ul>
 *   <li>La confirmation est réussie mais pas d'email de bienvenue</li>
 *   <li>L'utilisateur peut continuer normalement</li>
 *   <li>Impact moindre sur l'expérience utilisateur</li>
 * </ul>
 * 
 * <p><strong>Gestion :</strong> Hérite de la gestion de {@link EmailSendingException}
 * avec logging spécifique aux emails de bienvenue.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailSendingException
 * @see EmailServiceImpl#sendWelcomeEmail
 */
public class WelcomeEmailException extends EmailSendingException {
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant le problème d'envoi de bienvenue
     * @param cause la cause sous-jacente de l'exception (ex: MailException)
     */
    public WelcomeEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
