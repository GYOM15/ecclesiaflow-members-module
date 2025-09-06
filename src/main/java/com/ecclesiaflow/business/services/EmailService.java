package com.ecclesiaflow.business.services;

import com.ecclesiaflow.web.exception.ConfirmationEmailException;
import com.ecclesiaflow.web.exception.WelcomeEmailException;

/**
 * Service de domaine pour la gestion des communications email dans EcclesiaFlow.
 * <p>
 * Cette interface définit l'ensemble des opérations d'envoi d'emails
 * liées au cycle de vie des membres : confirmation, bienvenue, et récupération
 * de mot de passe. Centralise toute la logique de communication email.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service de domaine - Communication email</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Envoi d'emails de confirmation d'inscription</li>
 *   <li>Envoi d'emails de bienvenue après confirmation</li>
 *   <li>Envoi d'emails de récupération de mot de passe</li>
 *   <li>Gestion des templates et du contenu des emails</li>
 *   <li>Gestion des erreurs d'envoi et retry logic</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>Configuration SMTP (Gmail App Password)</li>
 *   <li>Templates d'emails (HTML/Text)</li>
 *   <li>Service de logging pour traçabilité</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Envoi automatique lors de l'inscription d'un nouveau membre</li>
 *   <li>Envoi de bienvenue après confirmation réussie</li>
 *   <li>Processus de récupération de mot de passe oublié</li>
 *   <li>Notifications administratives aux membres</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, gestion d'erreurs robuste, logging complet.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface EmailService {
    
    /**
     * Envoie un email contenant le code de confirmation d'inscription.
     * <p>
     * Cette méthode génère et envoie un email personnalisé contenant
     * le code de confirmation nécessaire pour activer le compte membre.
     * L'email utilise un template HTML avec le nom du membre et le code.
     * </p>
     * 
     * @param email l'adresse email du destinataire, non null et valide
     * @param code le code de confirmation à 6 chiffres, non null
     * @param firstName le prénom du membre pour personnalisation, non null
     * @throws ConfirmationEmailException si l'envoi échoue (SMTP, réseau, etc.)
     * @throws IllegalArgumentException si un paramètre est null ou invalide
     * 
     * @implNote Utilise le template 'confirmation-email.html' avec retry automatique.
     */
    void sendConfirmationCode(String email, String code, String firstName) throws ConfirmationEmailException;
    
    /**
     * Envoie un email de bienvenue après confirmation réussie du compte.
     * <p>
     * Cette méthode envoie un email de bienvenue personnalisé pour féliciter
     * le nouveau membre et lui fournir les informations de première connexion.
     * Marque la fin du processus d'inscription.
     * </p>
     * 
     * @param email l'adresse email du nouveau membre confirmé, non null et valide
     * @param firstName le prénom du membre pour personnalisation, non null
     * @throws WelcomeEmailException si l'envoi échoue (SMTP, réseau, etc.)
     * @throws IllegalArgumentException si un paramètre est null ou invalide
     * 
     * @implNote Utilise le template 'welcome-email.html' avec informations de connexion.
     */
    void sendWelcomeEmail(String email, String firstName) throws WelcomeEmailException;
    
}
