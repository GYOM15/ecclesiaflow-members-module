package com.ecclesiaflow.web.exception;

/**
 * Exception spécialisée levée lors d'un échec d'envoi d'email de confirmation d'inscription.
 * <p>
 * Cette exception hérite de {@link EmailSendingException} et est spécifiquement utilisée
 * pour les erreurs d'envoi des emails contenant les codes de confirmation lors du
 * processus d'inscription des nouveaux membres.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Exception technique spécialisée - Emails de confirmation</p>
 * 
 * <p><strong>Contexte d'utilisation :</strong></p>
 * <ul>
 *   <li>Échec d'envoi du premier email de confirmation après inscription</li>
 *   <li>Problèmes lors du renvoi de codes de confirmation</li>
 *   <li>Erreurs spécifiques au template d'email de confirmation</li>
 *   <li>Problèmes de génération du contenu de confirmation</li>
 * </ul>
 * 
 * <p><strong>Contenu de l'email concerné :</strong></p>
 * <ul>
 *   <li>Code de confirmation à 6 chiffres</li>
 *   <li>Instructions de confirmation</li>
 *   <li>Lien vers l'interface de confirmation</li>
 *   <li>Informations de contact et support</li>
 * </ul>
 * 
 * <p><strong>Impact sur le flux utilisateur :</strong></p>
 * <ul>
 *   <li>L'inscription est enregistrée mais le membre ne reçoit pas son code</li>
 *   <li>Possibilité de renvoi via l'endpoint dédié</li>
 *   <li>Support manuel possible via les logs détaillés</li>
 * </ul>
 * 
 * <p><strong>Gestion :</strong> Hérite de la gestion de {@link EmailSendingException}
 * avec logging spécifique aux emails de confirmation.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailSendingException
 * @see com.ecclesiaflow.business.services.impl.EmailServiceImpl
 */
public class ConfirmationEmailException extends EmailSendingException {
    
    /**
     * Construit une nouvelle exception avec le message et la cause spécifiés.
     * 
     * @param message le message d'erreur décrivant le problème d'envoi de confirmation
     * @param cause la cause sous-jacente de l'exception (ex: MailException)
     */
    public ConfirmationEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
