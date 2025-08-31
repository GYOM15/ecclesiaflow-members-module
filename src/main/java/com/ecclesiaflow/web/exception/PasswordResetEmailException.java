package com.ecclesiaflow.web.exception;

/**
 * Exception spécialisée levée lors d'un échec d'envoi d'email de réinitialisation de mot de passe.
 * <p>
 * Cette exception hérite de {@link EmailSendingException} et est spécifiquement utilisée
 * pour les erreurs d'envoi des emails de réinitialisation de mot de passe dans le
 * contexte de récupération de compte ou de changement de mot de passe.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception technique spécialisée - Emails de réinitialisation</p>
 * 
 * <p><strong>Contexte d'utilisation :</strong></p>
 * <ul>
 *   <li>Échec d'envoi de l'email de réinitialisation de mot de passe</li>
 *   <li>Problèmes de template d'email de récupération</li>
 *   <li>Erreurs lors de la génération de liens de réinitialisation</li>
 *   <li>Problèmes de sécurité ou de tokens de réinitialisation</li>
 * </ul>
 * 
 * <p><strong>Contenu de l'email concerné :</strong></p>
 * <ul>
 *   <li>Lien sécurisé de réinitialisation de mot de passe</li>
 *   <li>Instructions de sécurité</li>
 *   <li>Durée de validité du lien</li>
 *   <li>Informations de contact pour assistance</li>
 * </ul>
 * 
 * <p><strong>Impact sur le flux utilisateur :</strong></p>
 * <ul>
 *   <li>L'utilisateur ne reçoit pas le lien de réinitialisation</li>
 *   <li>Possibilité de nouvelle tentative de réinitialisation</li>
 *   <li>Support manuel nécessaire en cas de problème persistant</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong> Les échecs d'envoi ne doivent pas révéler
 * l'existence ou non d'un compte utilisateur.</p>
 * 
 * <p><strong>Gestion :</strong> Hérite de la gestion de {@link EmailSendingException}
 * avec logging spécifique aux emails de réinitialisation.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailSendingException
 * @see com.ecclesiaflow.business.services.impl.EmailServiceImpl#sendPasswordResetEmail
 */
public class PasswordResetEmailException extends EmailSendingException {
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant le problème d'envoi de réinitialisation
     * @param cause la cause sous-jacente de l'exception (ex: MailException)
     */
    public PasswordResetEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
