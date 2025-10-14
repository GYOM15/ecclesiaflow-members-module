package com.ecclesiaflow.business.domain.communication;

import com.ecclesiaflow.io.exception.ConfirmationEmailException;
import com.ecclesiaflow.io.exception.WelcomeEmailException;

import java.util.concurrent.CompletableFuture;

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
     * Envoie un email contenant un lien de confirmation sécurisé de manière asynchrone.
     * <p>
     * Cette méthode génère et envoie un email personnalisé contenant
     * un lien cliquable avec token UUID pour activer le compte membre en 1 clic.
     * L'email utilise un template HTML moderne avec bouton CTA.
     * <strong>Exécution asynchrone :</strong> L'envoi se fait en arrière-plan pour
     * améliorer les performances et l'expérience utilisateur.
     * </p>
     * 
     * @param email l'adresse email du destinataire, non null et valide
     * @param confirmationUrl l'URL complète de confirmation avec token, non null
     * @param firstName le prénom du membre pour personnalisation, non null
     * @return CompletableFuture<Void> pour permettre le suivi asynchrone de l'envoi
     * @throws ConfirmationEmailException si l'envoi échoue (SMTP, réseau, etc.)
     * @throws IllegalArgumentException si un paramètre est null ou invalide
     * 
     * @implNote Utilise un template email avec lien cliquable et retry automatique.
     *           Exécution asynchrone via @Async avec pool de threads dédié.
     *           Le lien expire après 24 heures.
     */
    CompletableFuture<Void> sendConfirmationCode(String email, String confirmationUrl, String firstName) throws ConfirmationEmailException;
    
    /**
     * Envoie un email de bienvenue après confirmation réussie du compte de manière asynchrone.
     * <p>
     * Cette méthode envoie un email de bienvenue personnalisé pour féliciter
     * le nouveau membre et lui fournir les informations de première connexion.
     * Marque la fin du processus d'inscription.
     * <strong>Exécution asynchrone :</strong> L'envoi se fait en arrière-plan pour
     * améliorer les performances et l'expérience utilisateur.
     * </p>
     * 
     * @param email l'adresse email du nouveau membre confirmé, non null et valide
     * @param firstName le prénom du membre pour personnalisation, non null
     * @return CompletableFuture<Void> pour permettre le suivi asynchrone de l'envoi
     * @throws WelcomeEmailException si l'envoi échoue (SMTP, réseau, etc.)
     * @throws IllegalArgumentException si un paramètre est null ou invalide
     * 
     * @implNote Utilise le template 'welcome-email.html' avec informations de connexion.
     *           Exécution asynchrone via @Async avec pool de threads dédié.
     */
    CompletableFuture<Void> sendWelcomeEmail(String email, String firstName) throws WelcomeEmailException;
    
}
