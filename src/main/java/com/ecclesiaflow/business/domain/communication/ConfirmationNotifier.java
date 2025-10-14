package com.ecclesiaflow.business.domain.communication;

import com.ecclesiaflow.io.notification.email.EmailConfirmationNotifier;
import com.ecclesiaflow.io.exception.EmailSendingException;

import java.util.UUID;

/**
 * Interface définissant le contrat d'envoi de liens de confirmation EcclesiaFlow.
 * <p>
 * Cette interface respecte le principe SRP et le principe d'inversion de dépendance
 * en définissant un contrat abstrait pour l'envoi de liens de confirmation sécurisés,
 * indépendamment du canal de communication utilisé (email, SMS, push notification, etc.).
 * Permet une architecture extensible et testable.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Interface de service - Contrat d'envoi de notifications</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Définir le contrat d'envoi de liens de confirmation</li>
 *   <li>Abstraction du canal de communication</li>
 *   <li>Support de l'extensibilité multi-canaux</li>
 * </ul>
 * 
 * <p><strong>Avantages architecturaux :</strong></p>
 * <ul>
 *   <li>Inversion de dépendance - Le service principal dépend de l'abstraction</li>
 *   <li>Extensibilité - Nouveaux canaux sans modification du code existant</li>
 *   <li>Testabilité - Mocking facile pour les tests unitaires</li>
 *   <li>Séparation des préoccupations - Canal agnostique</li>
 * </ul>
 * 
 * <p><strong>Implémentations possibles :</strong></p>
 * <ul>
 *   <li>{@link EmailConfirmationNotifier} - Envoi via email</li>
 *   <li>SmsConfirmationNotifier - Envoi via SMS (future)</li>
 *   <li>PushConfirmationNotifier - Envoi via push notification (future)</li>
 * </ul>
 * 
 * <p><strong>Pattern Strategy :</strong> Permet de changer l'algorithme d'envoi
 * à l'exécution selon la configuration ou les préférences utilisateur.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see EmailConfirmationNotifier
 */
public interface ConfirmationNotifier {

    /**
     * Envoie un lien de confirmation sécurisé à un membre via le canal configuré.
     * <p>
     * Méthode permettant l'envoi d'un lien de confirmation cliquable
     * contenant un token UUID sécurisé. L'implémentation concrète
     * détermine le canal de communication utilisé et construit l'URL complète.
     * </p>
     * 
     * @param email l'adresse email du destinataire, non null
     * @param confirmationToken le token de confirmation UUID à inclure dans le lien, non null
     * @param firstName le prénom du membre pour personnalisation, non null
     * 
     * @throws EmailSendingException si l'envoi échoue pour des raisons techniques
     * 
     * @implNote Les implémentations doivent construire une URL complète du type:
     *           https://app.ecclesiaflow.com/ecclesiaflow/members/confirmation?token={confirmationToken}
     *           et gérer les erreurs de façon appropriée avec logs détaillés.
     */
    void sendConfirmationLink(String email, UUID confirmationToken, String firstName);
}
