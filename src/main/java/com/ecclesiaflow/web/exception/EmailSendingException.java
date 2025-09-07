package com.ecclesiaflow.web.exception;

import com.ecclesiaflow.io.communication.email.EmailServiceImpl;

/**
 * Exception levée lors d'un échec d'envoi d'email par le service de messagerie.
 * <p>
 * Cette exception est utilisée par {@link EmailServiceImpl}
 * pour signaler les problèmes techniques liés à l'envoi d'emails via le serveur SMTP.
 * Elle encapsule les erreurs de configuration, de connectivité ou de transmission.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception technique - Gestion des erreurs d'envoi d'email</p>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Problèmes de connexion au serveur SMTP (Gmail)</li>
 *   <li>Erreurs d'authentification SMTP</li>
 *   <li>Adresses email destinataires invalides</li>
 *   <li>Quotas d'envoi dépassés</li>
 *   <li>Problèmes de configuration du serveur de mail</li>
 * </ul>
 * 
 * <p><strong>Types d'emails concernés :</strong></p>
 * <ul>
 *   <li>Emails de confirmation d'inscription</li>
 *   <li>Emails de bienvenue</li>
 *   <li>Emails de réinitialisation de mot de passe</li>
 * </ul>
 * 
 * <p><strong>Stratégie de gestion :</strong></p>
 * <ul>
 *   <li>L'inscription du membre continue même si l'email échoue</li>
 *   <li>Logging détaillé pour le diagnostic</li>
 *   <li>Possibilité de renvoi manuel des emails</li>
 * </ul>
 * 
 * <p><strong>Gestion :</strong> Capturée par {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}
 * et transformée en réponse HTTP 500 avec message d'erreur technique.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailServiceImpl
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public class EmailSendingException extends RuntimeException {
    
    /**
     * Construit une nouvelle exception avec le message spécifié.
     * 
     * @param message le message d'erreur décrivant le problème d'envoi
     */
    public EmailSendingException(String message) {
        super(message);
    }

    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant le problème d'envoi
     * @param cause la cause sous-jacente de l'exception (ex: MailException)
     */
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

